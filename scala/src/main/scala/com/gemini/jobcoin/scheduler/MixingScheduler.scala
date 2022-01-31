package com.gemini.jobcoin.scheduler

import akka.actor.ActorSystem
import com.gemini.jobcoin.repository.TransactionRepositoryImpl
import com.gemini.jobcoin.scheduler.actors.{
  MixingActor,
  SettlingActor,
  TransactionsActor
}
import com.gemini.jobcoin.service.MixingService
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import java.util.concurrent.TimeUnit.SECONDS
import scala.concurrent.duration.FiniteDuration

class MixingScheduler(
    actorSystem: ActorSystem,
    config: Config,
    mixingService: MixingService
) extends StrictLogging {

  import actorSystem.dispatcher

  private val initialDelay =
    FiniteDuration(config.getInt("scheduler.initialDelay"), SECONDS)

  private val interval =
    FiniteDuration(config.getInt("scheduler.interval"), SECONDS)

  private val transactionRepository = new TransactionRepositoryImpl
  private val settlingActorRef =
    SettlingActor(actorSystem, mixingService, transactionRepository)
  private val mixingActorRef =
    MixingActor(actorSystem, config, mixingService, settlingActorRef)

  private val transactionsActorRef =
    TransactionsActor(
      actorSystem,
      mixingService,
      mixingActorRef,
      transactionRepository
    )

  def start(): Unit = actorSystem.scheduler.schedule(
    initialDelay,
    interval,
    transactionsActorRef,
    TransactionsActor.Msg
  )

}

object MixingScheduler {
  def apply(
      actorSystem: ActorSystem,
      config: Config,
      mixingService: MixingService
  ) = new MixingScheduler(actorSystem, config, mixingService)

}
