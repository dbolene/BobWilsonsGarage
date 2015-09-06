package com.bobwilsonsgarage.frontend.resources

import common.protocol.BobWilsonsGarageProtocol.BobWilsonsGarageServiceResult
import spray.json.DefaultJsonProtocol

/**
 * Rest level Protocol for BobWilsonsGarage.
 *
 * @author dbolene
 */
object BobWilsonsGarageServiceRestProtocol {

  import spray.json._

  case class BobWilsonsGarageServiceRequestPost(car: Option[String])

  object BobWilsonsGarageServiceRequestPost extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(BobWilsonsGarageServiceRequestPost.apply)

  }

  case class BobWilsonsGarageServiceGetResponse(orderId: Option[String],
                                             car: Option[String],
                                             repairedYN: Boolean = false,
                                             notRepairedReason: Option[String] = None,
                                             detailedYN: Boolean = false,
                                             notDetailedReason: Option[String] = None,
                                             state: Option[String] = None)

  object BobWilsonsGarageServiceGetResponse extends DefaultJsonProtocol {
    implicit val format = jsonFormat7(BobWilsonsGarageServiceGetResponse.apply)

  }

  def mapFromBobWilsonsGarageServiceResult(result: BobWilsonsGarageServiceResult) = {
    BobWilsonsGarageServiceGetResponse(orderId = result.orderId,
      car = result.car,
      repairedYN = result.repairedYN,
      notRepairedReason = result.notRepairedReason,
      detailedYN = result.detailedYN,
      notDetailedReason = result.notDetailedReason,
      state = result.state
    )
  }

}
