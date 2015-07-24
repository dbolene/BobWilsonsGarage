package com.bobwilsonsgarage.staffing

import akka.actor.ActorSystem
import akka.contrib.pattern.ClusterSingletonProxy
import com.bobwilsonsgarage.staffing.StaffingServiceEndpointOverseerProtocol.CreateStaffingServiceEndpoint
import com.typesafe.config.ConfigFactory

/**
 * Staffing Service Hosting Akka Cluster Node.
 
 * @author dbolene
 */
object StaffingServiceNode {
  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [staffingservice]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)

    val registry = system.actorOf(ClusterSingletonProxy.props(
      singletonPath = "/user/singleton/registry",
      role = None),
      name = "registryProxy")

    val staffingServiceEndpointOverseer = system.actorOf(StaffingServiceEndpointOverseer.props())

    staffingServiceEndpointOverseer ! CreateStaffingServiceEndpoint(registry = registry)
  }
}