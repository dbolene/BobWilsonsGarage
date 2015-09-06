package common.protocol

/**
 * Detailing Service Interaction Protocol.

 * @author dbolene
 */
object DetailingServiceProtocol {

  trait DetailingResponse {
    def car: Option[String]
    def detailedYN: Boolean
    def reason: Option[String]
  }

  case class DetailingRequest(car: Option[String])
  case class DetailingFinished(car: Option[String], detailedYN: Boolean = true, reason: Option[String] = None) extends DetailingResponse
  case class CarCouldNotBeDetailed(car: Option[String], detailedYN: Boolean = false, reason: Option[String] = None) extends DetailingResponse

}

object DetailingService {
	val endpointName = "DetailingService"
}