package com.bobwilsonsgarage.frontend.rest

import akka.actor.{Actor, ActorRef, Props}
import com.bobwilsonsgarage.frontend.rest.routes.OrdersRoutes
import spray.routing.{HttpService, PathMatchers}

object RestApiServiceActor {
  def props(serverFrontEnd: ActorRef) = Props(new RestApiServiceActor(serverFrontEnd))
}

/**
 * REST top level actor.
 *
 * @author dbolene
 */
class RestApiServiceActor(serverFrontEnd: ActorRef) extends Actor with HttpService with OrdersRoutes {

  def actorRefFactory = context

  def receive = runRoute(bobWilsonsGarageService)

  val dependenciesResolver = serverFrontEnd

  lazy val bobWilsonsGarageService = {
    pathPrefix("BobWilsonsGarage") {
      path("orders") {ordersRoute} ~
      path("orders" / PathMatchers.Segment) {ordersIdRoute}
    }
  }
}
