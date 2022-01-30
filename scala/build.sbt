name := "gemini-jobcoin-scala"

version := "0.1"

scalaVersion := "2.12.5"

trapExit := false

libraryDependencies ++= Seq(
  // scala
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.scala-lang.modules" %% "scala-async" % "0.9.7",
  // typesafe
  "com.typesafe" % "config" % "1.3.2",
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.0.0-M1",
  "com.typesafe.play" %% "play-ws-standalone-json" % "2.0.0-M1",
  "com.typesafe.play" %% "play-json-joda" % "2.9.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  // akka
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.typesafe.akka" %% "akka-actor" % "2.5.32",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.12" % Test,
  "com.typesafe.akka" %% "akka-actor" % "2.5.12",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test
)
