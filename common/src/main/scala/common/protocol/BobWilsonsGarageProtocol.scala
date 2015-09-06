package common.protocol

/**
 * Bob Wilson's Garage Interaction Protocol.
 *
 * @author dbolene
 */
object BobWilsonsGarageProtocol {

  case class BobWilsonsGarageServiceRequest(car: Option[String])

  case object GetBobWilsonsGarageServiceResult

  case class BobWilsonsGarageServiceResult(orderId: Option[String],
                                           car: Option[String],
                                           repairedYN: Boolean = false,
                                           notRepairedReason: Option[String] = None,
                                           detailedYN: Boolean = false,
                                           notDetailedReason: Option[String] = None,
                                           state: Option[String] = None)

}
