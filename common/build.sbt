import sbt._
import Keys._

organization := "com.bobwilsonsgarage"

name := "akka-service-registry-example-common"

version := "1.0-Snapshot"

scalaVersion := "2.11.7"

val akkaVersion = "2.4.4"

net.virtualvoid.sbt.graph.Plugin.graphSettings

libraryDependencies += "junit" % "junit" % "4.5" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.1.5" % "test"

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % "test"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion

libraryDependencies += "com.typesafe.akka" % "akka-agent_2.11" % akkaVersion

libraryDependencies += "com.typesafe.akka" % "akka-remote_2.11" % akkaVersion

libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.11" % akkaVersion % "test"

libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % akkaVersion

resolvers += "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"

resolvers += "comcast oss" at "https://oss.sonatype.org/content/repositories/releases/com/comcast/"

libraryDependencies += "com.github.krasserm" %% "akka-persistence-cassandra-3x" % "0.6"

libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % akkaVersion excludeAll (ExclusionRule(organization = "io.dropwizard.metrics"))

libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % akkaVersion excludeAll (ExclusionRule(organization = "io.dropwizard.metrics"))

libraryDependencies += "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion

libraryDependencies += "nl.grons" %% "metrics-scala" % "3.3.0_a2.3" excludeAll (ExclusionRule(organization = "com.typesafe.akka"),
  ExclusionRule(organization = "io.dropwizard.metrics"))

libraryDependencies +=  "joda-time" % "joda-time" % "2.7"

libraryDependencies +=  "com.comcast" %% "actor-service-registry-common" % "1.0-SNAPSHOT" excludeAll (ExclusionRule(organization = "ch.qos.logback"), ExclusionRule(organization = "log4j"))

libraryDependencies +=  "com.comcast" %% "actor-service-registry" % "1.0-SNAPSHOT" excludeAll (ExclusionRule(organization = "ch.qos.logback"), ExclusionRule(organization = "log4j"))


