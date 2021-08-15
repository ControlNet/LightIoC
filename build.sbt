enablePlugins(CiReleasePlugin)

inThisBuild(List(
  organization := "space.controlnet",
  homepage := Some(url("https://github.com/ControlNet/LightIoC")),
  licenses := List("MIT" -> url("https://opensource.org/licenses/MIT")),
  developers := List(
    Developer("ControlNet", "ControlNet", "smczx@hotmail.com", url("https://controlnet.space/"))
  )
))


name := "LightIoC"

ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.5"

ThisBuild / libraryDependencies += "com.google.guava" % "guava" % "21.0"
ThisBuild / libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test

ThisBuild / publishMavenStyle := true
ThisBuild / Test / publishArtifact := false

publish / skip := true
//publishTo :=