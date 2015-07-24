package common.protocol

/**
 * CarRepair Service Interaction Protocol.

 * @author dbolene
 */
object CarRepairServiceProtocol {

  case class CarRepairRequest(car: String, detailYN: Boolean)
  case class CarRepairFinished(car: String, detailedYN: Boolean, detailedReason: Option[String])

}


object CarRepairService {
	val endpointName = "CarRepairService"
}