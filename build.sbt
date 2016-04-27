import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.universal.UniversalDeployPlugin
import sbt._

organization := "com.bobwilsonsgarage"

name := "akka-service-registry-example"

version := "1.0-Snapshot"

scalaVersion := "2.11.7"

lazy val common = Project(id = "common",
                            base = file("common"))

lazy val backEnd = Project(id = "backEnd",
                            base = file("backEnd"))
                            .dependsOn(common)
                            .enablePlugins(DockerPlugin, SbtNativePackager, JavaAppPackaging, UniversalDeployPlugin)

lazy val frontEnd = Project(id = "frontEnd",
                            base = file("frontEnd"))
                            .dependsOn(common)
                            .enablePlugins(DockerPlugin, SbtNativePackager, JavaAppPackaging, UniversalDeployPlugin)

lazy val staffing = Project(id = "staffing",
                            base = file("staffing"))
                            .dependsOn(common)
                            .enablePlugins(DockerPlugin, SbtNativePackager, JavaAppPackaging, UniversalDeployPlugin)

lazy val carRepair = Project(id = "carRepair",
                            base = file("carRepair"))
                            .dependsOn(common)
                            .enablePlugins(DockerPlugin, SbtNativePackager, JavaAppPackaging, UniversalDeployPlugin)

lazy val detailing = Project(id = "detailing",
                            base = file("detailing"))
                            .dependsOn(common)
                            .enablePlugins(DockerPlugin, SbtNativePackager, JavaAppPackaging, UniversalDeployPlugin)

lazy val root = Project(id = "root",
                            base = file("."))
                            .aggregate(common, frontEnd, backEnd, staffing, detailing, carRepair)
