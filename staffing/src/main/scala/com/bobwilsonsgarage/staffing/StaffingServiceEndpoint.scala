package com.bobwilsonsgarage.staffing

import akka.actor.{ActorRef, ActorLogging, Actor, Props}
import com.bobwilsonsgarage.staffing.StaffingServiceEndpointInternalProtocol.InitializeStaffingServiceEndpoint
import com.comcast.csv.common.protocol.ServiceRegistryProtocol.{RegistryHasRestarted, PublishService}
import common.protocol.StaffingService
import common.protocol.StaffingServiceProtocol._


/**
 * Companion of StaffingServiceEndpoint
 */
object StaffingServiceEndpoint {

  def props = Props[StaffingServiceEndpoint]

}
/**
 * Main call entry point into staffing service.
 *
 * @author dbolene
 */
class StaffingServiceEndpoint extends Actor with ActorLogging {

  var registry: Option[ActorRef] = None

  override def receive = {

    case initialize: InitializeStaffingServiceEndpoint =>
      log.info(s"Received -> InitializeStaffingServiceEndpoint")
      registry = Option(initialize.registry)
      registry.foreach(r => r ! PublishService(serviceName = StaffingService.endpointName, serviceEndpoint = self))

    case registryHasRestarted: RegistryHasRestarted =>
      log.info(s"Received -> RegistryHasRestarted")
      registry = Option(registryHasRestarted.registry)
      registry.foreach(r => r ! PublishService(serviceName = StaffingService.endpointName, serviceEndpoint = self))

    case sr: StaffingRequest =>
      log.info(s"Received -> StaffingRequest")
      // when we are open, we got all kinds of people sitting around
      sender() ! StaffingResponse(sr.requestedStaff)

    case msg =>
      log.info(s"received unknown message: $msg")
  }
}

object StaffingServiceEndpointInternalProtocol {

  case class InitializeStaffingServiceEndpoint(registry: ActorRef)

}
