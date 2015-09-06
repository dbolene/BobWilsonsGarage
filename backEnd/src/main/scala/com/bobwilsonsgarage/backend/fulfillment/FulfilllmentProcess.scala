package com.bobwilsonsgarage.backend.fulfillment

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{ActorLogging, ActorRef, ReceiveTimeout}
import akka.contrib.pattern.ShardRegion.Passivate
import akka.persistence.{PersistentActor, SaveSnapshotSuccess, SnapshotOffer}
import common.protocol.BobWilsonsGarageProtocol.{BobWilsonsGarageServiceRequest, BobWilsonsGarageServiceResult, GetBobWilsonsGarageServiceResult}
import common.protocol.CarRepairServiceProtocol.{CarCouldNotBeRepaired, CarRepairFinished, CarRepairRequest, CarRepairResponse}
import common.protocol.DetailingServiceProtocol.{CarCouldNotBeDetailed, DetailingFinished, DetailingRequest, DetailingResponse}
import common.protocol.FulfillmentProcessProtocol.{DoNothing, InitiateFulfillmentProcess}
import common.statemachine.{PersistentStateMachine, PersistentStateMachineState, PersistentStateMachineStateRef}

import scala.concurrent.duration._

/**
 * An order fulfillment orchestrator.
 *
 * @author dbolene
 */
class FulfillmentProcess extends PersistentActor with PersistentStateMachine with ActorLogging {

  var data: FulfillmentProcessData = FulfillmentProcessData(id = Option(self.path.name))

  context.setReceiveTimeout(120.seconds)

  // self.path.parent.name is the type name (utf-8 URL-encoded)
  // self.path.name is the entry identifier (utf-8 URL-encoded)
  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  /*
   * Mutate the data state, persist a snapshot, and trigger side-effects.
   */
  def update(updateData: FulfillmentProcessData): Unit = {
    // save copy b4 mutate
    val oldData = data

    data = data.copy(
      id = updateData.id orElse data.id,
      requestor = updateData.requestor orElse data.requestor,
      carRepairServiceEndpoint = updateData.carRepairServiceEndpoint orElse data.carRepairServiceEndpoint,
      detailingServiceEndpoint = updateData.detailingServiceEndpoint orElse data.detailingServiceEndpoint,
      serviceRequest = updateData.serviceRequest orElse data.serviceRequest,
      carServiceResponse = updateData.carServiceResponse orElse data.carServiceResponse,
      detailingResponse = updateData.detailingResponse orElse data.detailingResponse,
      state = updateData.newState orElse data.state,
      newState = None
    )

    saveSnapshot(data)
    updateData.newState.foreach(ns => changeState(dereferenceState(ns)))
  }

  /*
   * Mutate to recovered state.
   */
  def restoreSnapshot(fulfillmentProcessData: FulfillmentProcessData): Unit = {
    data = fulfillmentProcessData
    data.state.foreach(st => changeState(dereferenceState(st)))
  }

  /*
   * Field recovery from persistence.
   */
  override def receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: FulfillmentProcessData) =>
      log.info(s"ReceiveRecovered -> SnapshotOffer")
      restoreSnapshot(snapshot)
  }

  /*
   * Starting behavior.
   */
  override def initialStateCommandBehavior: PartialFunction[Any, Unit] = {
    case init: InitiateFulfillmentProcess =>
      log.info(s"Received -> InitiateFulfillmentProcess")

      val establishedNewState: Option[PersistentStateMachineState] =
        init.carRepairServiceEndpoint
          .map(cr => RepairingCarState)
          .orElse(Some(ServiceUnavailable))

      update(FulfillmentProcessData(
        id = Some(init.id),
        serviceRequest = Option(init.request),
        carRepairServiceEndpoint = init.carRepairServiceEndpoint,
        detailingServiceEndpoint = init.detailingServiceEndpoint,
        newState = establishedNewState.map(ns => PersistentStateMachineStateRef(ns))
      ))

      // edge actions
      establishedNewState match {
        case Some(RepairingCarState) =>

          log.info(s"edge action for RepairingCarState")
          log.info(s"data.carRepairServiceEndpoint: ${data.carRepairServiceEndpoint}")
          log.info(s"data.serviceRequest: ${data.serviceRequest}")

          data.carRepairServiceEndpoint.foreach(carRepairService => {
            data.serviceRequest.foreach(request => {
              carRepairService ! CarRepairRequest(request.car)
            })
          })

        case _ =>
      }
  }

  /*
   * commandReceive message handlers across all states.
   */
  override def extenderDefaultCommandBehavior: Option[PartialFunction[Any, Unit]] = Some({
    case ReceiveTimeout =>
      log.info(s"Received -> ReceiveTimeout")
      context.parent ! Passivate(stopMessage = Stop)

    case Stop =>
      log.info(s"Received -> Stop")
      context.stop(self)

    case sss: SaveSnapshotSuccess =>
      log.info(s"Received -> SaveSnapshotSuccess")

    case GetBobWilsonsGarageServiceResult =>
      log.info(s"Received -> GetBobWilsonsGarageServiceResult")
      val repaired = data.carServiceResponse.exists(sr => sr.repairedYN)
      val notRepairedReason = data.carServiceResponse.filter(sr => !sr.repairedYN).flatMap(csr => csr.reason)
      val detailed = data.detailingResponse.exists(sr => sr.detailedYN)
      val notDetailedReason = data.detailingResponse.filter(sr => !sr.detailedYN).flatMap(csr => csr.reason)
      sender() !  BobWilsonsGarageServiceResult(orderId = data.id,
                                                 car = data.serviceRequest.flatMap(sr => sr.car),
                                                 repairedYN = repaired,
                                                 notRepairedReason = notRepairedReason,
                                                 detailedYN = detailed,
                                                 notDetailedReason = notDetailedReason,
      state = data.state.map(sr => sr.name)
      )
  })

  /*
   * Map from PersistentStateMachineStateRef to the referenced state.
   */
  override def dereferenceState(reference: PersistentStateMachineStateRef): Option[PersistentStateMachineState] = {
    reference.name match {
      case "RepairingCarState" => Option(RepairingCarState)
      case "DetailingCar" => Option(DetailingCar)
      case "ServiceUnavailable" => Option(ServiceUnavailable)
      case "Fulfilled" => Option(Fulfilled)
      case unknown =>
        log.error(s"attempt to dereference unknown state of name: ${reference.name}")
        None
    }
  }

  ////////////////////
  // RepairingCar State
  ////////////////////
  object RepairingCarState extends PersistentStateMachineState {

    def name = "RepairingCarState"

    def commandBehavior: PartialFunction[Any, Unit] = {
      case finished: CarRepairFinished =>
        log.info(s"Received -> CarRepairFinished")

        update(FulfillmentProcessData(
          carServiceResponse = Option(finished),
          newState = Option(PersistentStateMachineStateRef(DetailingCar))
        ))

        // edge action
        data.detailingServiceEndpoint.foreach(dse => dse ! DetailingRequest(
          car = finished.car))

      case notRepaired: CarCouldNotBeRepaired =>
        log.info(s"Received -> CarCouldNotBeRepaired")

        update(FulfillmentProcessData(
          carServiceResponse = Option(notRepaired),
          newState = Option(PersistentStateMachineStateRef(ServiceUnavailable))
        ))

    }
  }

  ////////////////////
  // DetailingCar State
  ////////////////////s
  object DetailingCar extends PersistentStateMachineState {

    def name = "DetailingCar"

    def commandBehavior: PartialFunction[Any, Unit] = {
      case detailingFinished: DetailingFinished =>
        log.info(s"Received => DetailingFinished")

        update(FulfillmentProcessData(
          detailingResponse = Option(detailingFinished),
          newState = Option(PersistentStateMachineStateRef(Fulfilled))
        ))

      case notDetailed: CarCouldNotBeDetailed =>
        log.info(s"Received => CarCouldNotBeDetailed")

        update(FulfillmentProcessData(
          detailingResponse = Option(notDetailed),
          newState = Option(PersistentStateMachineStateRef(Fulfilled))
        ))

    }
  }

  ////////////////////
  // ServiceUnavailable State
  ////////////////////
  object ServiceUnavailable extends PersistentStateMachineState {

    def name = "ServiceUnavailable"

    def commandBehavior: PartialFunction[Any, Unit] = {
      case test: DoNothing =>
        log.info(s"Received => DoNothing(${test.testValue})")
    }
  }

  ////////////////////
  // Fulfilled State
  ////////////////////
  object Fulfilled extends PersistentStateMachineState {

    def name = "Fulfilled"

    def commandBehavior: PartialFunction[Any, Unit] = {
      case test: DoNothing =>
        log.info(s"Received => DoNothing(${test.testValue})")
    }
  }
}

case class FulfillmentProcessData(
                                   id: Option[String] = None,
                                   requestor: Option[ActorRef] = None,
                                   state: Option[PersistentStateMachineStateRef] = None,
                                   newState: Option[PersistentStateMachineStateRef] = None,
                                   carRepairServiceEndpoint: Option[ActorRef] = None,
                                   detailingServiceEndpoint: Option[ActorRef] = None,
                                   serviceRequest: Option[BobWilsonsGarageServiceRequest] = None,
                                   carServiceResponse: Option[CarRepairResponse] = None,
                                   detailingResponse: Option[DetailingResponse] = None
                                   )
