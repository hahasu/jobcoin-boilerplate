name := "gemini-jobcoin-scala"

version := "0.1"

scalaVersion := "2.12.5"

trapExit := false

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"
libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.0.0-M1"
libraryDependencies += "com.typesafe.play" %% "play-ws-standalone-json" % "2.0.0-M1"
libraryDependencies += "org.scala-lang.modules" %% "scala-async" % "0.9.7"

// https://mvnrepository.com/artifact/com.typesafe.play/play-json-joda
libraryDependencies += "com.typesafe.play" %% "play-json-joda" % "2.9.2"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.12",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test
)
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.typesafe.akka" %% "akka-actor" % "2.5.32",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.12" % Test
)
