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

lazy val base = project.in(file("."))
  .settings(sonatypeCredentialHost := "s01.oss.sonatype.org")
  .settings(sonatypeRepository := "https://s01.oss.sonatype.org/service/local")


name := "LightIoC"

ThisBuild / scalaVersion := "2.13.5"
ThisBuild / scalacOptions += "-target:jvm-1.8"

ThisBuild / libraryDependencies += "com.google.guava" % "guava" % "21.0"
ThisBuild / libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test
