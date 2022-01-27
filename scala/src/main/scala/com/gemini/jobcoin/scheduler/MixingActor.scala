package com.gemini.jobcoin.scheduler

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import akka.pattern.pipe
import com.gemini.jobcoin.clients.models.Transaction
import com.gemini.jobcoin.models.Models.Address
import com.gemini.jobcoin.scheduler.MixingActor.Result
import com.gemini.jobcoin.service.MixingService
import com.typesafe.config.Config

import java.util.concurrent.TimeUnit.SECONDS
import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

object MixingActor {
  case class Result(toAddresses: List[Address], amount: Double)
}

class MixingActor(
    mixingService: MixingService,
    actorSystem: ActorSystem,
    config: Config,
    settlingActorRef: ActorRef
) extends Actor
    with ActorLogging {

  import context.dispatcher

  private val feePercent = config.getDouble("mixing.fee")
  private val houseAddress = Address(config.getString("mixing.houseAddress"))

  private val mixingDelay =
    FiniteDuration(config.getInt("mixing.delay"), SECONDS)

  def receive: Receive = {
    case Transaction(_, toAddress, amount, _) =>
      val depositedAddress = Address(toAddress)
      val fee = amount * feePercent

      mixingService
        .transfer(depositedAddress, houseAddress, amount)
        .flatMap(_ => {
          mixingService
            .getDepositInfo(depositedAddress)
            .collect { case Some(depositInfo) =>
              val addresses = depositInfo.withdrawalAddresses
              val netAmountForEachAddress = (amount - fee) / addresses.size
              Result(addresses, netAmountForEachAddress)
            }
        })
        .pipeTo(self)

    case Result(toAddresses, amount) =>
      actorSystem.scheduler.scheduleOnce(
        mixingDelay,
        settlingActorRef,
        SettlingActor.Msg(houseAddress, toAddresses, amount)
      )
  }

}
