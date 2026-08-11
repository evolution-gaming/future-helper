import sbtversionpolicy.Compatibility.BinaryCompatible

name := "future-helper"

organization := "com.evolutiongaming"

homepage := Some(url("https://github.com/evolution-gaming/future-helper"))

startYear := Some(2018)

organizationName := "Evolution"

organizationHomepage := Some(url("https://evolution.com"))

scalaVersion := crossScalaVersions.value.head

crossScalaVersions := Seq("2.13.18", "3.2.1")

publishTo := Some(Resolver.evolutionReleases)

versionPolicyIntention := BinaryCompatible

libraryDependencies ++= Seq(
  "com.evolutiongaming" %% "executor-tools" % "1.0.5",
  "org.scalatest"       %% "scalatest"      % "3.2.20" % Test)

licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT")))

addCommandAlias("check", "all versionPolicyCheck Compile/doc")
addCommandAlias("build", "+all compile test")
