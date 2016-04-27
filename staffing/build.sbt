import sbt._
import Keys._
import sbtassembly.Plugin.AssemblyKeys._

assemblySettings

jarName in assembly := "StaffingService.jar"

test in assembly := {}

maintainer in Docker := "David Bolene <dbolene@yahoo.com>"

dockerBaseImage := "java:8"

packageName in Docker := "bobwilsonsgaragestaffing"

organization := "com.bobwilsonsgarage"

name := "bob-wilsons-staffingservice"

version := "1.0"

scalaVersion := "2.11.7"

val akkaVersion = "2.4.0"

val sprayVersion = "1.3.3"

resolvers ++= Seq(
  "Typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases" at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies += "junit" % "junit" % "4.5" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.1.5" % "test"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion

libraryDependencies += "com.typesafe.akka" % "akka-agent_2.11" % akkaVersion

libraryDependencies += "com.typesafe.akka" % "akka-remote_2.11" % akkaVersion

libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.11" % akkaVersion % "test"

libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion





