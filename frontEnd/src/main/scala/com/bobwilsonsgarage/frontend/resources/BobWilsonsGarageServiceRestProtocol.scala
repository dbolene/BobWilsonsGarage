package com.bobwilsonsgarage.frontend.resources

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

  case class BobWilsonsGarageServicePostResponse(orderId: String)

  object BobWilsonsGarageServicePostResponse extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(BobWilsonsGarageServicePostResponse.apply)

  }

}
