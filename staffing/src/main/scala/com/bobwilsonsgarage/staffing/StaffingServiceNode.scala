package com.bobwilsonsgarage.staffing

import akka.actor.ActorSystem
import akka.cluster.singleton.{ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import com.bobwilsonsgarage.staffing.StaffingServiceEndpointOverseerProtocol.CreateStaffingServiceEndpoint
import com.typesafe.config.ConfigFactory
import common.util.Logging

/**
 * Staffing Service Hosting Akka Cluster Node.
 
 * @author dbolene
 */
object StaffingServiceNode extends Logging {
  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [staffingservice]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)

    val registry = system.actorOf(ClusterSingletonProxy.props(
      singletonManagerPath = "/user/registry",
      settings = ClusterSingletonProxySettings(system)),
      name = "registryProxy")

    val staffingServiceEndpointOverseer = system.actorOf(StaffingServiceEndpointOverseer.props())

    loggr.info(s"port used: $port")
    System.out.println(s"println port used: $port")

    staffingServiceEndpointOverseer ! CreateStaffingServiceEndpoint(registry = registry)
  }
}