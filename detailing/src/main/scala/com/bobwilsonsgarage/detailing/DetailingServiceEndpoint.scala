package com.bobwilsonsgarage.detailing

import akka.actor.{ActorRef, ActorLogging, Actor, Props}
import com.bobwilsonsgarage.detailing.DetailingServiceEndpointInternalProtocol.InitializeDetailingServiceEndpoint
import com.comcast.csv.common.protocol.ServiceRegistryProtocol.{RegistryHasRestarted, PublishService}
import common.protocol.DetailingService
import common.protocol.DetailingServiceProtocol._


/**
 * Companion of DetailingServiceEndpoint
 */
object DetailingServiceEndpoint {

  def props = Props[DetailingServiceEndpoint]

}
/**
 * Main call entry point into staffing service.
 *
 * @author dbolene
 */
class DetailingServiceEndpoint extends Actor with ActorLogging {

  var registry: Option[ActorRef] = None

  override def receive = {

    case initialize: InitializeDetailingServiceEndpoint =>
      registry = Option(initialize.registry)
      registry.foreach(r => r ! PublishService(serviceName = DetailingService.endpointName, serviceEndpoint = self))

    case registryHasRestarted: RegistryHasRestarted =>
      registry = Option(registryHasRestarted.registry)
      registry.foreach(r => r ! PublishService(serviceName = DetailingService.endpointName, serviceEndpoint = self))

    case sr: DetailingRequest =>

    case msg =>
      log.info(s"received unknown message: $msg")
  }
}

object DetailingServiceEndpointInternalProtocol {

  case class InitializeDetailingServiceEndpoint(registry: ActorRef)

}
