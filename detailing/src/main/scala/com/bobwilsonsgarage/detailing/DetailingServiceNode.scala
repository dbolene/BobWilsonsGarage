package com.bobwilsonsgarage.detailing

import akka.actor.ActorSystem
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import com.bobwilsonsgarage.detailing.DetailingServiceEndpointOverseerProtocol.CreateDetailingServiceEndpoint
import com.typesafe.config.ConfigFactory

/**
 * Detailing Service Hosting Akka Cluster Node.
 
 * @author dbolene
 */
object DetailingServiceNode {
  def main(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val config = ConfigFactory.parseString("akka.cluster.roles = [detailingservice]").
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)

    val registry = system.actorOf(ClusterSingletonProxy.props(
      singletonManagerPath = "/user/registry",
      settings = ClusterSingletonProxySettings(system)),
      name = "registryProxy")

    val detailingServiceEndpointOverseer = system.actorOf(DetailingServiceEndpointOverseer.props())

    detailingServiceEndpointOverseer ! CreateDetailingServiceEndpoint(registry = registry)
  }
}