package com.bobwilsonsgarage.carrepair

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import com.bobwilsonsgarage.carrepair.CarRepairServiceEndpointInternalProtocol.InitializeCarRepairServiceEndpoint
import common.protocol.CarRepairService
import common.protocol.CarRepairServiceProtocol._

import scala.concurrent.duration._

/**
 * Companion of CarRepairServiceEndpointOverseer.
 */
object CarRepairServiceEndpointOverseer {
  def props() = Props[CarRepairServiceEndpointOverseer]
}

/**
 * Supervisor of CarRepairServiceEndpoint
 *
 * @author dbolene
 */
class CarRepairServiceEndpointOverseer extends Actor with ActorLogging {
  import CarRepairServiceEndpointOverseerProtocol._

  override def receive = {

    case csse: CreateCarRepairServiceEndpoint =>
      try {
        val staffingServiceActor = context.actorOf(CarRepairServiceEndpoint.props, CarRepairService.endpointName)
        staffingServiceActor ! InitializeCarRepairServiceEndpoint(csse.registry)
        sender() ! CarRepairServiceEndpointCreated(staffingServiceActor)
      } catch {
        case ex: InvalidActorNameException =>
          sender () ! CarRepairServiceEndpointCreateFailed
          log.error("Attempt to create already instantiated CarRepairServiceEndpoint", ex)
      }

    case msg =>
      log.info(s"received unknown message: $msg")
  }

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case e: InvalidActorNameException =>
        log.error("Stop after InvalidActorNameException: " + e)
        Stop
      case e: Exception =>
        log.error("initiating Restart after Exception: " + e)
        Restart
    }
}

object CarRepairServiceEndpointOverseerProtocol {
  case class CreateCarRepairServiceEndpoint(registry: ActorRef)
  case class CarRepairServiceEndpointCreated(actorRef: ActorRef)
  case object CarRepairServiceEndpointCreateFailed
}