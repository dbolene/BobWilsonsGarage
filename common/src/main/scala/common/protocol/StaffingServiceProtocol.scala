package common.protocol

/**
 * Staffing Service Interaction Protocol.

 * @author dbolene
 */
object StaffingServiceProtocol {

  case class StaffingRequest(requestedStaff: Int)
  case class StaffingResponse(staffProvided: Int)

}


object StaffingService {
	val endpointName = "StaffingService"
}