package com.bobwilsonsgarage.carrepair

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import com.bobwilsonsgarage.carrepair.CarRepairServiceEndpointInternalProtocol.InitializeCarRepairServiceEndpoint
import com.comcast.csv.common.protocol.ServiceRegistryProtocol._
import common.fsm.{Offline, Online, State}
import common.protocol.CarRepairServiceProtocol._
import common.protocol.StaffingServiceProtocol.{StaffingRequest, StaffingResponse}
import common.protocol.{CarRepairService, StaffingService}

import scala.concurrent.duration._
import scala.util.Success


/**
 * Companion of CarRepairServiceEndpoint
 */
object CarRepairServiceEndpoint {

  def props = Props[CarRepairServiceEndpoint]

}

/**
 * Main call entry point into car repair service.
 *
 * @author dbolene
 */
class CarRepairServiceEndpoint extends LoggingFSM[State, Data] with ActorLogging {

  startWith(Offline, Data())

  when(Offline) {

    case Event(initialize: InitializeCarRepairServiceEndpoint, _) =>
      log.info("Received -> InitializeCarRepairServiceEndpoint")
      initialize.registry ! SubscribeToService(serviceName = StaffingService.endpointName)
      stay using Data(registry = Option(initialize.registry))

    case Event(registryHasRestarted: RegistryHasRestarted, _) =>
      log.info("Received -> RegistryHasRestarted")
      registryHasRestarted.registry ! SubscribeToService(serviceName = StaffingService.endpointName)
      stay using Data(registry = Option(registryHasRestarted.registry))

    case Event(serviceAvailable: ServiceAvailable, d @ Data(_, _)) if serviceAvailable.serviceName == StaffingService.endpointName =>
      log.info(s"Received -> ServiceAvailable: $serviceAvailable")
      d.registry.foreach(r => r ! PublishService(serviceName = CarRepairService.endpointName, serviceEndpoint = self))
      goto(Online) using d.copy(staffingService = Option(serviceAvailable.serviceEndpoint))

    case Event(e, s) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay()
  }

  when(Online) {

    case Event(registryHasRestarted: RegistryHasRestarted, _) =>
      log.info(s"Received -> RegistryHasRestarted")
      registryHasRestarted.registry ! UnPublishService(serviceName = CarRepairService.endpointName)
      registryHasRestarted.registry ! SubscribeToService(serviceName = StaffingService.endpointName)
      goto(Offline) using Data(registry = Option(registryHasRestarted.registry))

    case Event(serviceUnAvailable: ServiceUnAvailable, d @ Data(_, _)) =>
      log.info(s"Received -> ServiceUnAvailable: $serviceUnAvailable")
      d.registry.foreach(r => r ! UnPublishService(serviceName = CarRepairService.endpointName))
      goto(Offline) using d.copy(staffingService = None)

    case Event(carRepairRequest: CarRepairRequest, d @ Data(_, _)) =>
      log.info(s"Received -> CarRepairRequest: $carRepairRequest")
      val requestor = sender()
      implicit val timeout = Timeout(5 seconds)
      import context.dispatcher
      d.staffingService.foreach(ss => ss ? StaffingRequest(1) onComplete {
        case Success(sr: StaffingResponse) =>
          requestor ! CarRepairFinished(carRepairRequest.car)
        case _ =>
          requestor ! CarCouldNotBeRepaired(carRepairRequest.car, reason = Some("Staff is unavailable"))
      })
      stay()

    case Event(e, s) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay()
  }
}

object CarRepairServiceEndpointInternalProtocol {

  case class InitializeCarRepairServiceEndpoint(registry: ActorRef)

}

case class Data(registry: Option[ActorRef] = None, staffingService: Option[ActorRef] = None)
