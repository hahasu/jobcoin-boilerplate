package com.gemini.jobcoin.scheduler

import akka.actor.{ActorSystem, Props}
import com.gemini.jobcoin.service.MixingService
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import java.util.concurrent.TimeUnit.SECONDS
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class MixingScheduler(
    actorSystem: ActorSystem,
    config: Config,
    mixingService: MixingService
)(implicit
    ec: ExecutionContext
) extends StrictLogging {

  private val initialDelay =
    FiniteDuration(config.getInt("scheduler.initialDelay"), SECONDS)

  private val interval =
    FiniteDuration(config.getInt("scheduler.interval"), SECONDS)

  private val settlingActorRef =
    actorSystem.actorOf(Props(new SettlingActor(mixingService)))

  private val mixingActorRef =
    actorSystem.actorOf(
      Props(
        new MixingActor(mixingService, actorSystem, config, settlingActorRef)
      )
    )

  private val transactionsActorRef =
    actorSystem.actorOf(
      Props(new TransactionsActor(mixingService, mixingActorRef))
    )

  actorSystem.scheduler.schedule(
    initialDelay,
    interval,
    transactionsActorRef,
    TransactionsActor.Msg
  )
}
