package common.protocol

/**
 * CarRepair Service Interaction Protocol.

 * @author dbolene
 */
object CarRepairServiceProtocol {

  case class CarRepairRequest(car: String)
  case class CarRepairFinished(car: String)
  case class CarCouldNotBeRepaired(car: String, reason: Option[String] = None)

}


object CarRepairService {
	val endpointName = "CarRepairService"
}