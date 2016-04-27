package com.bobwilsonsgarage.backend.server

import akka.actor._
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import akka.cluster.{Cluster, Member, MemberStatus}
import com.bobwilsonsgarage.backend.fulfillment.FulfillmentProcess
import com.comcast.csv.akka.serviceregistry.ServiceRegistry
import com.comcast.csv.akka.serviceregistry.ServiceRegistryInternalProtocol.End
import com.typesafe.config.ConfigFactory
import common.protocol.ClusterNodeRegistrationProtocol.BackendRegistration
import common.shardingfunctions.FulfillmentProcessShardingFunctions
import common.util.Logging

/**
 *
 * @author dbolene
 */
object ServerBackend extends Logging {

  def runMe(sys: ActorSystem): Unit = {

    info("ServerBackend runMe")

    sys.actorOf(Props[ServerBackend], name = "backend")

    val fulfillmentProcessRegion = ClusterSharding(sys).start(
      typeName = "FulfillmentProcess",
      entityProps = Props[FulfillmentProcess],
      settings = ClusterShardingSettings(sys),
      extractEntityId = FulfillmentProcessShardingFunctions.idExtractor,
      extractShardId = FulfillmentProcessShardingFunctions.shardResolver)

    sys.actorOf(ClusterSingletonManager.props(
      singletonProps = ServiceRegistry.props,
      terminationMessage = End,
      settings = ClusterSingletonManagerSettings(sys)),
      name = "singleton")

  }

  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)

    runMe(system)
  }
}

/**
 *
 * @author dbolene
 */
class ServerBackend extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up) foreach register
    case MemberUp(m) =>
      log.info(s"======= backend MemberUp")
      register(m)
  }

  def register(member: Member): Unit =
    if (member.hasRole("frontend"))
      context.actorSelection(RootActorPath(member.address) / "user" / "frontend") !
        BackendRegistration
}
