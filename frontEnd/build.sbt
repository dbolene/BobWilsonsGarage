import AssemblyKeys._
import sbt._
import sbt.ExclusionRule
import sbt.Keys._
import java.io.File

assemblySettings

jarName in assembly := "FrontEnd.jar"

test in assembly := {}

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
    case PathList("META-INF", "maven", "com.googlecode.concurrentlinkedhashmap","concurrentlinkedhashmap-lru", "pom.properties" ) => MergeStrategy.discard
    case PathList("META-INF", "maven", "com.googlecode.concurrentlinkedhashmap","concurrentlinkedhashmap-lru", "pom.xml" ) => MergeStrategy.discard
    case PathList("javax", "ws", xs @ _*) => MergeStrategy.first
    case x => old(x)
  }
}

mainClass in assembly := Some("com.bobwilsonsgarage.frontend.server.ServerFrontend")

javacOptions ++= Seq("-source", "1.6")

organization := "com.bobwilsonsgarage"

name := "FrontEnd"

version := "1.0-Snapshot"

scalaVersion := "2.11.6"

traceLevel in Test := 0

testOptions in Test += Tests.Argument("-oF")

persistTraceLevel := 0

parallelExecution in Test := false

val akkaVersion = "2.3.11"

val sprayVersion = "1.3.3"

net.virtualvoid.sbt.graph.Plugin.graphSettings

//libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" excludeAll (ExclusionRule(organization = "org.slf4j"))

libraryDependencies += "junit" % "junit" % "4.5" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.1.5" % "test"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion

libraryDependencies += "com.typesafe.akka" % "akka-agent_2.11" % akkaVersion

libraryDependencies += "com.typesafe.akka" % "akka-remote_2.11" % akkaVersion

libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % akkaVersion excludeAll (ExclusionRule(organization = "io.dropwizard.metrics"))

libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % akkaVersion excludeAll (ExclusionRule(organization = "io.dropwizard.metrics"))

libraryDependencies += "io.spray" %% "spray-can" % sprayVersion

libraryDependencies += "io.spray" %% "spray-httpx" % sprayVersion

libraryDependencies += "io.spray" %% "spray-http" % sprayVersion

libraryDependencies += "io.spray" %% "spray-util" % sprayVersion

libraryDependencies += "io.spray" %% "spray-client" % sprayVersion

libraryDependencies += "io.spray" %% "spray-routing-shapeless2" % sprayVersion

libraryDependencies += "io.spray" %% "spray-json" % "1.3.1"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.9"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.9"

libraryDependencies += "org.json4s" %% "json4s-ext" % "3.2.9"




