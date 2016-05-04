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

val akkaVersion = "2.4.4"

val sprayVersion = "1.3.3"

resolvers ++= Seq(
  "Typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases" at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies += "junit" % "junit" % "4.5" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.1.5" % "test"

val slf4jOrg = "org.slf4j"

val slf4jVersion = "1.7.10"

libraryDependencies +=  slf4jOrg % "slf4j-api" % slf4jVersion

libraryDependencies +=  slf4jOrg % "log4j-over-slf4j" % slf4jVersion

libraryDependencies +=  slf4jOrg % "jcl-over-slf4j" % slf4jVersion % "test"

libraryDependencies +=  slf4jOrg % "jul-to-slf4j" % slf4jVersion % "test"

libraryDependencies +=  slf4jOrg % "slf4j-simple" % slf4jVersion



