package common.protocol

/**
 * Detailing Service Interaction Protocol.

 * @author dbolene
 */
object DetailingServiceProtocol {

  case class DetailingRequest(car: String)
  case class DetailingFinished(car: String)

}


object DetailingService {
	val endpointName = "DetailingService"
}