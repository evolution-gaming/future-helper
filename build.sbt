name := "future-helper"

organization := "com.evolutiongaming"

homepage := Some(new URL("http://github.com/evolution-gaming/future-helper"))

startYear := Some(2018)

organizationName := "Evolution"

organizationHomepage := Some(url("http://evolutiongaming.com"))

scalaVersion := crossScalaVersions.value.head

crossScalaVersions := Seq("2.13.8", "2.12.13")

publishTo := Some(Resolver.evolutionReleases)

libraryDependencies ++= Seq(
  "com.evolutiongaming" %% "executor-tools" % "1.0.2",
  "org.scalatest"       %% "scalatest"      % "3.0.8" % Test)

licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT")))

releaseCrossBuild := true