package com.bobwilsonsgarage.carrepair

import akka.actor.{ActorRef, ActorLogging, Actor, Props}
import com.bobwilsonsgarage.carrepair.CarRepairServiceEndpointInternalProtocol.InitializeCarRepairServiceEndpoint
import com.comcast.csv.common.protocol.ServiceRegistryProtocol.{RegistryHasRestarted, PublishService}
import common.protocol.CarRepairService
import common.protocol.CarRepairServiceProtocol._


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
class CarRepairServiceEndpoint extends Actor with ActorLogging {

  var registry: Option[ActorRef] = None

  override def receive = {

    case initialize: InitializeCarRepairServiceEndpoint =>
      registry = Option(initialize.registry)
      registry.foreach(r => r ! PublishService(serviceName = CarRepairService.endpointName, serviceEndpoint = self))

    case registryHasRestarted: RegistryHasRestarted =>
      registry = Option(registryHasRestarted.registry)
      registry.foreach(r => r ! PublishService(serviceName = CarRepairService.endpointName, serviceEndpoint = self))

    case sr: CarRepairRequest =>

    case msg =>
      log.info(s"received unknown message: $msg")
  }
}

object CarRepairServiceEndpointInternalProtocol {

  case class InitializeCarRepairServiceEndpoint(registry: ActorRef)

}
