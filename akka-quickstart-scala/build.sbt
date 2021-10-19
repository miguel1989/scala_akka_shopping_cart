name := "akka-quickstart-scala"

version := "1.0"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.16"
val akkaHttpVersion = "10.2.6"
val akkaHttpJsonVersion = "1.38.2"
val circeVersion = "0.14.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,

  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpJsonVersion,
  "de.heikoseeberger" %% "akka-http-jackson" % akkaHttpJsonVersion,

  "ch.qos.logback" % "logback-classic" % "1.2.6",

//  "io.circe" %% "circe.core" % circeVersion,
//  "io.circe" %% "circe.generic" % circeVersion,
//  "io.circe" %% "circe.parser" % circeVersion,

  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,

  "org.scalatest" %% "scalatest" % "3.2.9" % Test
)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

