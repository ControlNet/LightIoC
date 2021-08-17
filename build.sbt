enablePlugins(CiReleasePlugin)
enablePlugins(Sonatype)

inThisBuild(List(
  organization := "space.controlnet",
  homepage := Some(url("https://github.com/ControlNet/LightIoC")),
  licenses := List("MIT" -> url("https://opensource.org/licenses/MIT")),
  developers := List(
    Developer("ControlNet", "ControlNet", "smczx@hotmail.com", url("https://controlnet.space/"))
  ),
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
))

lazy val scala211 = "2.11.12"
lazy val scala212 = "2.12.14"
lazy val scala213 = "2.13.5"

lazy val base = project.in(file("."))
  .settings(sonatypeCredentialHost := "s01.oss.sonatype.org")
  .settings(sonatypeRepository := "https://s01.oss.sonatype.org/service/local")
  .settings(parallelExecution := false)
  .settings(crossScalaVersions := List(scala213, scala212, scala211))
  .settings(scalaVersion := scala211)


name := "LightIoC"

ThisBuild / scalacOptions += "-target:jvm-1.8"

ThisBuild / libraryDependencies += "com.google.guava" % "guava" % "21.0"
ThisBuild / libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test
