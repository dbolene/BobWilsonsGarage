package com.bobwilsonsgarage.frontend.server

import akka.actor._
import akka.cluster.{Cluster, MemberStatus}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import akka.io.IO
import com.bobwilsonsgarage.frontend.rest.RestApiServiceActor
import com.bobwilsonsgarage.frontend.server.ServerFrontendProtocol._
import com.comcast.csv.akka.serviceregistry.ServiceRegistry
import com.comcast.csv.akka.serviceregistry.ServiceRegistryInternalProtocol.End
import com.comcast.csv.common.protocol.ServiceRegistryProtocol._
import com.typesafe.config.ConfigFactory
import common.protocol.ClusterNodeRegistrationProtocol.BackendRegistration
import common.protocol.{CarRepairService, DetailingService}
import common.util.Logging
import spray.can.Http
import spray.can.Http.Bound

/**
 * FrontEnd Server main.
 *
 * @author dbolene
 */
object ServerFrontend extends Logging {

  var sys: ActorSystem = null
  val restPort = 8080
  val restScheme = "http"

  def runMe(): Unit = {

 /*   sys.actorOf(ClusterSingletonManager.props(
      singletonProps = ServiceRegistry.props,
      terminationMessage = End,
      settings = ClusterSingletonManagerSettings(sys)),
      name = "singleton") */

    val frontend = sys.actorOf(Props[ServerFrontend], name = "frontend")
    System.out.println(s"println frontend: $frontend")

  }

  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.parseString("akka.cluster.roles = [frontend]").
      withFallback(ConfigFactory.load())

    sys = ActorSystem("ClusterSystem", config)

    runMe()

  }
}

class ServerFrontend extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  var carRepairServiceEndpoint: Option[ActorRef] = None
  var detailingServiceEndpoint: Option[ActorRef] = None

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  override def postStop(): Unit = cluster.unsubscribe(self)

  val handler = context.actorOf(RestApiServiceActor.props(self), name = "bobwilsonsgarage-rest-service")

  implicit val sys = context.system

  IO(Http) ! Http.Bind(handler, interface = "0.0.0.0", port = ServerFrontend.restPort)

  // finally we drop the main thread but hook the shutdown of
  // our IOBridge into the shutdown of the applications ActorSystem
  context.system.registerOnTermination {
    Http.Unbind
    log.info("Exiting...")
  }

  val registry = sys.actorOf(ClusterSingletonProxy.props(
    singletonManagerPath = "/user/registry",
    settings = ClusterSingletonProxySettings(sys)),
    name = "registryProxy")

  var backends = IndexedSeq.empty[ActorRef]

  def receive = {
    case BackendRegistration if !backends.contains(sender()) =>
      log.info(s"Received -> BackendRegistration")
      context watch sender()
      if (backends.isEmpty) {
        registry ! SubscribeToService(serviceName = CarRepairService.endpointName)
        registry ! SubscribeToService(serviceName = DetailingService.endpointName)
      }
      backends = backends :+ sender()

    case Terminated(a) =>
      log.info(s"Received -> Terminated($a)")
      backends = backends.filterNot(_ == a)

    case serviceAvailable: ServiceAvailable if serviceAvailable.serviceName == CarRepairService.endpointName =>
      log.info(s"Received -> ServiceAvailable: ${serviceAvailable.serviceName}")
      carRepairServiceEndpoint = Option(serviceAvailable.serviceEndpoint)

    case serviceUnAvailable: ServiceUnAvailable if serviceUnAvailable.serviceName == CarRepairService.endpointName =>
      log.info(s"Received -> ServiceUnAvailable: ${serviceUnAvailable.serviceName}")
      carRepairServiceEndpoint = None

    case serviceAvailable: ServiceAvailable if serviceAvailable.serviceName == DetailingService.endpointName =>
      log.info(s"Received -> ServiceAvailable: ${serviceAvailable.serviceName}")
      detailingServiceEndpoint = Option(serviceAvailable.serviceEndpoint)

    case serviceUnAvailable: ServiceUnAvailable if serviceUnAvailable.serviceName == DetailingService.endpointName =>
      log.info(s"Received -> ServiceUnAvailable: ${serviceUnAvailable.serviceName}")
      detailingServiceEndpoint = None

    case RequestDependentServices =>
      log.info(s"Received -> RequestDependentServices")
      sender() ! DependentServices(carRepairServiceEndpoint, detailingServiceEndpoint)

    case state: CurrentClusterState =>
      log.info(s"Received -> CurrentClusterState")
      //state.members
    case MemberUp(m) =>
      log.info(s"Received -> MemberUp: $m")
    case b: Bound =>
      log.info(s"Received -> Bound: $b")
    case unknown =>
      log.warning(s"Received unknown message: $unknown")

  }
}

object ServerFrontendProtocol {

  case object RequestDependentServices

  case class DependentServices(
                                carRepairServiceEndpoint: Option[ActorRef] = None,
                                detailingServiceEndpoint: Option[ActorRef] = None
                                )
}
