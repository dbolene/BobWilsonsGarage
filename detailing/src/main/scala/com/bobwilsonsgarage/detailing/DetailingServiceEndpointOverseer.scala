package com.bobwilsonsgarage.detailing

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import com.bobwilsonsgarage.detailing.DetailingServiceEndpointInternalProtocol.InitializeDetailingServiceEndpoint
import common.protocol.DetailingService
import common.protocol.DetailingServiceProtocol._

import scala.concurrent.duration._

/**
 * Companion of DetailingServiceEndpointOverseer.
 */
object DetailingServiceEndpointOverseer {
  def props() = Props[DetailingServiceEndpointOverseer]
}

/**
 * Supervisor of DetailingServiceEndpoint
 *
 * @author dbolene
 */
class DetailingServiceEndpointOverseer extends Actor with ActorLogging {
  import DetailingServiceEndpointOverseerProtocol._

  override def receive = {

    case csse: CreateDetailingServiceEndpoint =>
      try {
        val staffingServiceActor = context.actorOf(DetailingServiceEndpoint.props, DetailingService.endpointName)
        staffingServiceActor ! InitializeDetailingServiceEndpoint(csse.registry)
        sender() ! DetailingServiceEndpointCreated(staffingServiceActor)
      } catch {
        case ex: InvalidActorNameException =>
          sender () ! DetailingServiceEndpointCreateFailed
          log.error("Attempt to create already instantiated DetailingServiceEndpoint", ex)
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

object DetailingServiceEndpointOverseerProtocol {
  case class CreateDetailingServiceEndpoint(registry: ActorRef)
  case class DetailingServiceEndpointCreated(actorRef: ActorRef)
  case object DetailingServiceEndpointCreateFailed
}