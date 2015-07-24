package com.bobwilsonsgarage.staffing

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import com.bobwilsonsgarage.staffing.CloudServiceEndpointInternalProtocol.InitializeStaffingServiceEndpoint
import common.protocol.StaffingService
import common.protocol.StaffingServiceProtocol._

import scala.concurrent.duration._

/**
 * Companion of StaffingServiceEndpointOverseer.
 */
object StaffingServiceEndpointOverseer {
  def props() = Props[StaffingServiceEndpointOverseer]
}

/**
 * Supervisor of StaffingServiceEndpoint
 *
 * @author dbolene
 */
class StaffingServiceEndpointOverseer extends Actor with ActorLogging {
  import StaffingServiceEndpointOverseerProtocol._

  override def receive = {

    case csse: CreateStaffingServiceEndpoint =>
      try {
        val staffingServiceActor = context.actorOf(StaffingServiceEndpoint.props, StaffingService.endpointName)
        staffingServiceActor ! InitializeStaffingServiceEndpoint(csse.registry)
        sender() ! StaffingServiceEndpointCreated(staffingServiceActor)
      } catch {
        case ex: InvalidActorNameException =>
          sender () ! StaffingServiceEndpointCreateFailed
          log.error("Attempt to create already instantiated StaffingServiceEndpoint", ex)
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

object StaffingServiceEndpointOverseerProtocol {
  case class CreateStaffingServiceEndpoint(registry: ActorRef)
  case class StaffingServiceEndpointCreated(actorRef: ActorRef)
  case object StaffingServiceEndpointCreateFailed
}