package com.bobwilsonsgarage.frontend.server

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import common.protocol.ClusterNodeRegistrationProtocol.BackendRegistration
import com.typesafe.config.ConfigFactory
import common.util.Logging

/**
 * FrontEnd Server main.
 *
 * @author dbolene
 */
object ServerFrontend extends Logging {

  def runMe(sys: ActorSystem): Unit = {

/*    sys.actorOf(ClusterSingletonManager.props(
      singletonProps = ServiceRegistry.props,
      singletonName = "registry",
      terminationMessage = End,
      role = None),
      name = "singleton")
*/
    val frontend = sys.actorOf(Props[ServerFrontend], name = "frontend")

    info("ServerFrontend runMe")

  }

  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
      withFallback(ConfigFactory.load())

    val portArg = if (args.isEmpty) "0" else args(0)

    val system = ActorSystem("ClusterSystem", config)

    runMe(system)

  }
}

class ServerFrontend extends Actor with Logging {

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  var backends = IndexedSeq.empty[ActorRef]

  def receive = {
    case BackendRegistration if !backends.contains(sender()) =>
      info(s"Received -> BackendRegistration")
      context watch sender()
      backends = backends :+ sender()
    case Terminated(a) =>
      info(s"Received -> Terminated($a)")
      backends = backends.filterNot(_ == a)
  }
}
