package com.bobwilsonsgarage.detailing

import akka.actor.ActorSystem
import akka.contrib.pattern.ClusterSingletonProxy
import com.bobwilsonsgarage.detailing.DetailingServiceEndpointOverseerProtocol.CreateDetailingServiceEndpoint
import com.typesafe.config.ConfigFactory

/**
 * Detailing Service Hosting Akka Cluster Node.
 
 * @author dbolene
 */
object DetailingServiceNode {
  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [detailingservice]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)

    val registry = system.actorOf(ClusterSingletonProxy.props(
      singletonPath = "/user/singleton/registry",
      role = None),
      name = "registryProxy")

    val detailingServiceEndpointOverseer = system.actorOf(DetailingServiceEndpointOverseer.props())

    detailingServiceEndpointOverseer ! CreateDetailingServiceEndpoint(registry = registry)
  }
}