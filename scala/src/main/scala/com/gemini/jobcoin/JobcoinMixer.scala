package com.gemini.jobcoin

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.gemini.jobcoin.clients.JobcoinClientImpl
import com.gemini.jobcoin.models.Models.{Address, DepositInfo}
import com.gemini.jobcoin.repository.DepositInfoRepositoryImpl
import com.gemini.jobcoin.scheduler.MixingScheduler
import com.gemini.jobcoin.service.MixingService
import com.typesafe.config.ConfigFactory
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration
import scala.io.StdIn

object JobcoinMixer {
  object CompletedException extends Exception {}

  def main(args: Array[String]): Unit = {
    // Create an actor system
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    // Load Config
    val config = ConfigFactory.load()

    // HTTP client
    val client = new JobcoinClientImpl(config, StandaloneAhcWSClient())

    val depositInfoRepository = new DepositInfoRepositoryImpl
    val mixingService = MixingService(depositInfoRepository, client)

    // starting the scheduler
    MixingScheduler(actorSystem, config, mixingService).start()

    try {
      while (true) {
        println(prompt)
        val line = StdIn.readLine()

        if (line == "quit") throw CompletedException

        val addresses = line.split(",")
        if (line == "") {
          println(s"You must specify empty addresses to mix into!\n$helpText")
        } else {
          val depositAddress = UUID.randomUUID()

          val l = depositInfoRepository.save(
            DepositInfo(
              Address(depositAddress.toString),
              addresses.map(Address.apply).toList
            )
          )

          Await.result(l, Duration.Inf)
          println(
            s"You may now send Jobcoins to address $depositAddress. They will be mixed and sent to your destination addresses."
          )
        }
      }
    } catch {
      case CompletedException => println("Quitting...")
    } finally {
      actorSystem.terminate()
    }
  }

  val prompt: String =
    "Please enter a comma-separated list of new, unused Jobcoin addresses where your mixed Jobcoins will be sent."
  val helpText: String =
    """
      |Jobcoin Mixer
      |
      |Takes in at least one return address as parameters (where to send coins after mixing). Returns a deposit address to send coins to.
      |
      |Usage:
      |    run return_addresses...
    """.stripMargin
}
