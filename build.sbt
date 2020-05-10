name := "future-helper"

organization := "com.evolutiongaming"

homepage := Some(new URL("http://github.com/evolution-gaming/future-helper"))

startYear := Some(2018)

organizationName := "Evolution Gaming"

organizationHomepage := Some(url("http://evolutiongaming.com"))

bintrayOrganization := Some("evolutiongaming")

scalaVersion := crossScalaVersions.value.head

crossScalaVersions := Seq("2.13.1", "2.12.10")

resolvers += Resolver.bintrayRepo("evolutiongaming", "maven")

libraryDependencies ++= Seq(
  "com.evolutiongaming" %% "executor-tools" % "1.0.2",
  "org.scalatest"       %% "scalatest"      % "3.1.2" % Test)

licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT")))

releaseCrossBuild := true