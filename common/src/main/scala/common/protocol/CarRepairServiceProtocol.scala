package common.protocol

/**
 * CarRepair Service Interaction Protocol.

 * @author dbolene
 */
object CarRepairServiceProtocol {

  trait CarRepairResponse {
    def car: Option[String]
    def repairedYN: Boolean
    def reason: Option[String]
  }

  case class CarRepairRequest(car: Option[String])

  case class CarRepairFinished(car: Option[String], repairedYN: Boolean = true, reason: Option[String] = None) extends CarRepairResponse

  case class CarCouldNotBeRepaired(car: Option[String], repairedYN: Boolean = false, reason: Option[String] = None) extends CarRepairResponse

}


object CarRepairService {
	val endpointName = "CarRepairService"
}