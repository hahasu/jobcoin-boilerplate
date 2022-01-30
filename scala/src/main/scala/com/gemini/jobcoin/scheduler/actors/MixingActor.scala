package com.gemini.jobcoin.scheduler.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.pipe
import com.gemini.jobcoin.clients.models.MixingInfo
import com.gemini.jobcoin.models.Models.Address
import com.gemini.jobcoin.service.MixingService
import com.typesafe.config.Config
import org.joda.time.DateTime

import java.util.concurrent.TimeUnit.SECONDS
import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

object MixingActor {
  case class Result(toAddresses: List[Address], amount: Double, ts: DateTime)

  def apply(
      actorSystem: ActorSystem,
      config: Config,
      mixingService: MixingService,
      settlingActorRef: ActorRef
  ): ActorRef = actorSystem.actorOf(
    Props(
      new MixingActor(actorSystem, config, mixingService, settlingActorRef)
    )
  )
}

class MixingActor(
    actorSystem: ActorSystem,
    config: Config,
    mixingService: MixingService,
    settlingActorRef: ActorRef
) extends Actor
    with ActorLogging {

  import context.dispatcher

  private val feePercent = config.getDouble("mixing.fee")
  private val houseAddress = Address(config.getString("mixing.houseAddress"))

  private val mixingDelay =
    FiniteDuration(config.getInt("mixing.delay"), SECONDS)

  def receive: Receive = {

    case MixingInfo(depositAddress, depositInfo, amount, ts) =>
      mixingService
        .transfer(depositAddress, houseAddress, amount)
        .map(_ => {
          val addresses = depositInfo.withdrawalAddresses
          val fee = amount * feePercent
          val netAmountForEachAddress = (amount - fee) / addresses.size
          MixingActor.Result(addresses, netAmountForEachAddress, ts)
        })
        .pipeTo(self)

    case MixingActor.Result(toAddresses, amount, ts) =>
      actorSystem.scheduler.scheduleOnce(
        mixingDelay,
        settlingActorRef,
        SettlingActor.Msg(houseAddress, toAddresses, amount, ts)
      )
  }

}
