package common.protocol

import akka.actor.ActorRef
import common.protocol.BobWilsonsGarageProtocol.BobWilsonsGarageServiceRequest

/**
 *
 * @author dbolene
 */
object FulfillmentProcessProtocol {

  case class InitiateFulfillmentProcess(
                                           id: String,
                                           request: BobWilsonsGarageServiceRequest,
                                           carRepairServiceEndpoint: Option[ActorRef],
                                           detailingServiceEndpoint: Option[ActorRef]
                                           )

  case class DoNothing(testValue: String)
}
