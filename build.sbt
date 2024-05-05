name := "future-helper"

organization := "com.evolutiongaming"

homepage := Some(new URL("http://github.com/evolution-gaming/future-helper"))

startYear := Some(2018)

organizationName := "Evolution"

organizationHomepage := Some(url("https://evolution.com"))

scalaVersion := crossScalaVersions.value.head

crossScalaVersions := Seq("2.13.14", "2.12.17", "3.2.1")

publishTo := Some(Resolver.evolutionReleases)

libraryDependencies ++= Seq(
  "com.evolutiongaming" %% "executor-tools" % "1.0.4",
  "org.scalatest"       %% "scalatest"      % "3.2.14" % Test)

licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT")))

releaseCrossBuild := true