package common.protocol

/**
 * Bob Wilson's Garage Interaction Protocol.
 *
 * @author dbolene
 */
object BobWilsonsGarageProtocol {

  case class RequestService(car: String)
  case class ServiceResponse(car: String,
                             repairedYN: Boolean,
                             notRepairedReason: Option[String] = None,
                             detailedYN: Boolean,
                             notDetailedReason: Option[String])

}
