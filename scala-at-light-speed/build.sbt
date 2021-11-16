name := "scala-at-light-speed"

version := "0.1"

scalaVersion := "2.13.1"

val circeVersion = "0.14.1"
val akkaVersion = "2.6.17"
val catsVersion = "2.6.1"
val effectsVersion = "3.2.8"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-literal",
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.2.10",
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % effectsVersion
)
