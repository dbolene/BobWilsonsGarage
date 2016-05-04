import AssemblyKeys._
import sbt._
import sbt.ExclusionRule
import sbt.Keys._
import java.io.File

import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._

assemblySettings

jarName in assembly := "BackEnd.jar"

test in assembly := {}

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
    case PathList("META-INF", "maven", "com.googlecode.concurrentlinkedhashmap","concurrentlinkedhashmap-lru", "pom.properties" ) => MergeStrategy.discard
    case PathList("META-INF", "maven", "com.googlecode.concurrentlinkedhashmap","concurrentlinkedhashmap-lru", "pom.xml" ) => MergeStrategy.discard
    case PathList("javax", "ws", xs @ _*) => MergeStrategy.first
    case x => old(x)
  }
}

mainClass in assembly := Some("com.bobwilsonsgarage.backend.server.ServerBackend")

maintainer in Docker := "David Bolene <dbolene@yahoo.com>"

dockerBaseImage := "java:8"

packageName in Docker := "bobwilsonsgaragebackend"

organization := "com.bobwilsonsgarage"

name := "BackEnd"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-unchecked", "-deprecation")

traceLevel in Test := 0

testOptions in Test += Tests.Argument("-oF")

persistTraceLevel := 0

parallelExecution in Test := false

val akkaVersion = "2.4.4"

net.virtualvoid.sbt.graph.Plugin.graphSettings


resolvers += "dnvriend at bintray" at "http://dl.bintray.com/dnvriend/maven"

libraryDependencies += "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.0.3" % "test"

libraryDependencies += "org.json4s" % "json4s-native_2.11" % "3.2.10"

libraryDependencies += "org.json4s" % "json4s-jackson_2.11" % "3.2.10"

libraryDependencies += "org.json4s" % "json4s-ext_2.11" % "3.2.10"

