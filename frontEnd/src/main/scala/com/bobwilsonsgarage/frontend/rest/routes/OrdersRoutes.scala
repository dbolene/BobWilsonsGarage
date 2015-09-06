package com.bobwilsonsgarage.frontend.rest.routes

import akka.actor.ActorRef
import akka.contrib.pattern.ClusterSharding
import akka.pattern.ask
import akka.util.Timeout
import com.bobwilsonsgarage.frontend.rest.{HostAndPath, RestApiServiceActor}
import com.bobwilsonsgarage.frontend.server.ServerFrontendProtocol.{DependentServices, RequestDependentServices}
import com.bobwilsonsgarage.frontend.util.{UrlEstablisher, UuidUtil}
import common.protocol.BobWilsonsGarageProtocol.BobWilsonsGarageServiceRequest
import common.protocol.FulfillmentProcessProtocol.InitiateFulfillmentProcess
import common.protocol.ShardingProtocol.EntryEnvelope
import common.shardingfunctions.FulfillmentProcessShardingFunctions
import common.util.Logging
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes
import spray.routing.{HttpService, RequestContext}

import scala.util.{Failure, Success}
import scala.concurrent.duration._

/**
 * BobWilsonsGarage Order rest routes.
 *
 * @author dbolene
 */
trait OrdersRoutes extends HttpService with Logging {

  this: RestApiServiceActor =>

  def hostAndPath(req: RequestContext): String = {
    HostAndPath.hostAndPath(req)
  }

  val fulfillmentProcessRegion: ActorRef = ClusterSharding(context.system).start(
    typeName = "FulfillmentProcess",
    entryProps = None,
    idExtractor = FulfillmentProcessShardingFunctions.idExtractor,
    shardResolver = FulfillmentProcessShardingFunctions.shardResolver)

  import com.bobwilsonsgarage.frontend.resources.BobWilsonsGarageServiceRestProtocol._
  import context.dispatcher
  import spray.httpx.SprayJsonSupport._
  implicit val timeout = Timeout(130 seconds)

  lazy val ordersRoute = {
    post {
      entity(as[BobWilsonsGarageServiceRequestPost]) {
        bobWilsonsGarageServicePostRequest => {
          ctx =>
            info(ctx.request.method + " - " + ctx.request.uri)
            info(s"service request POST payload: $bobWilsonsGarageServicePostRequest")

            (dependenciesResolver ? RequestDependentServices).mapTo[DependentServices] onComplete {

              case Failure(e) =>
                ctx.complete(e)
              case Success(dependentServicesResponse) =>

                info(s"dependentServicesResponse: $dependentServicesResponse")

                val fulfillmentProcessId = UuidUtil.generateNewUuid()

                fulfillmentProcessRegion ! EntryEnvelope(fulfillmentProcessId, InitiateFulfillmentProcess(
                  id = fulfillmentProcessId,
                  request = BobWilsonsGarageServiceRequest(car = bobWilsonsGarageServicePostRequest.car),
                  carRepairServiceEndpoint = dependentServicesResponse.carRepairServiceEndpoint,
                  detailingServiceEndpoint = dependentServicesResponse.detailingServiceEndpoint
                ))

                val fulfillmentProcessUrl = UrlEstablisher.orderUrl(fulfillmentProcessId, hostAndPath(ctx))
                info(s"returned fulfillmentProcessUrl: $fulfillmentProcessUrl")
                ctx.complete(StatusCodes.Accepted, RawHeader("Location", fulfillmentProcessUrl) :: Nil, "")
            }
        }
      }
    }
  }
}
