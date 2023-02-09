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
lazy val scala213 = "2.13.8"

lazy val core = (project in file("."))
  .settings(
    name := "LightIoC",
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    exportJars := true,
    parallelExecution := false,
    fork := false,
    crossScalaVersions := List(scala211, scala212, scala213),
    libraryDependencies ++= Seq(
      "com.google.guava" % "guava" % "[21.0, 23.0]",
      "org.scalatest" %% "scalatest" % "3.2.+" % Test
    )
  )

lazy val api = project
  .dependsOn(core)
  .settings(
    name := "LightIoC API",
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    autoScalaLibrary := false,
    parallelExecution := false,
    fork := false,
    libraryDependencies ++= Seq(
      "org.junit.jupiter" % "junit-jupiter" % "5.7.2" % Test,
      "net.aichler" % "jupiter-interface" % "0.9.0" % Test
    )
  )

ThisBuild / scalaVersion := scala213
ThisBuild / scalacOptions += "-target:jvm-1.8"

Test / testOptions := Seq(Tests.Argument(TestFrameworks.JUnit, "-a"))
