package com.bobwilsonsgarage.detailing

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import com.bobwilsonsgarage.detailing.DetailingServiceEndpointInternalProtocol.InitializeDetailingServiceEndpoint
import com.comcast.csv.common.protocol.ServiceRegistryProtocol._
import common.fsm.{Offline, Online, State}
import common.protocol.DetailingServiceProtocol._
import common.protocol.StaffingServiceProtocol.{StaffingRequest, StaffingResponse}
import common.protocol.{DetailingService, StaffingService}

import scala.concurrent.duration._
import scala.util.Success

/**
 * Companion of DetailingServiceEndpoint
 */
object DetailingServiceEndpoint {

  def props = Props[DetailingServiceEndpoint]

}
/**
 * Main call entry point into detailing service.
 *
 * @author dbolene
 */
class DetailingServiceEndpoint extends LoggingFSM[State, Data] with ActorLogging {

  startWith(Offline, Data())

  when(Offline) {

    case Event(initialize: InitializeDetailingServiceEndpoint, _) =>
      log.info("Received -> InitializeDetailingServiceEndpoint")
      initialize.registry ! SubscribeToService(serviceName = StaffingService.endpointName)
      stay using Data(registry = Option(initialize.registry))

    case Event(registryHasRestarted: RegistryHasRestarted, _) =>
      log.info("Received -> RegistryHasRestarted")
      registryHasRestarted.registry ! SubscribeToService(serviceName = StaffingService.endpointName)
      stay using Data(registry = Option(registryHasRestarted.registry))

    case Event(serviceAvailable: ServiceAvailable, d @ Data(_, _)) if serviceAvailable.serviceName == StaffingService.endpointName =>
      log.info(s"Received -> ServiceAvailable: $serviceAvailable")
      d.registry.foreach(r => r ! PublishService(serviceName = DetailingService.endpointName, serviceEndpoint = self))
      goto(Online) using d.copy(staffingService = Option(serviceAvailable.serviceEndpoint))

    case Event(e, s) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay()
  }

  when(Online) {

    case Event(registryHasRestarted: RegistryHasRestarted, _) =>
      log.info(s"Received -> RegistryHasRestarted")
      registryHasRestarted.registry ! UnPublishService(serviceName = DetailingService.endpointName)
      registryHasRestarted.registry ! SubscribeToService(serviceName = StaffingService.endpointName)
      goto(Offline) using Data(registry = Option(registryHasRestarted.registry))

    case Event(serviceUnAvailable: ServiceUnAvailable, d@Data(_, _)) =>
      log.info(s"Received -> ServiceUnAvailable: $serviceUnAvailable")
      d.registry.foreach(r => r ! UnPublishService(serviceName = DetailingService.endpointName))
      goto(Offline) using d.copy(staffingService = None)

    case Event(detailingRequest: DetailingRequest, d @ Data(_, _)) =>
      log.info(s"Received -> DetailingRequest: $detailingRequest")
      val requestor = sender()
      implicit val timeout = Timeout(5 seconds)
      import context.dispatcher
      d.staffingService.foreach(ss => ss ? StaffingRequest(1) onComplete {
        case Success(sr: StaffingResponse) =>
          if (sr.staffProvided >= 1)
            requestor ! DetailingFinished(detailingRequest.car)
          else
            requestor ! CarCouldNotBeDetailed(detailingRequest.car, reason = Some("no staff available"))
        case _ =>
          requestor ! CarCouldNotBeDetailed(detailingRequest.car)
      })
      stay()

    case Event(e, s) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay()
  }
}

object DetailingServiceEndpointInternalProtocol {

  case class InitializeDetailingServiceEndpoint(registry: ActorRef)

}

case class Data(registry: Option[ActorRef] = None, staffingService: Option[ActorRef] = None)




