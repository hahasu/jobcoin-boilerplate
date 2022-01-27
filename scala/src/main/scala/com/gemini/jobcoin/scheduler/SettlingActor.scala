package com.gemini.jobcoin.scheduler

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.gemini.jobcoin.models.Models.Address
import com.gemini.jobcoin.scheduler.SettlingActor.{Msg, Result}
import com.gemini.jobcoin.service.MixingService

import scala.concurrent.Future

object SettlingActor {

  case class Msg(
      fromAddress: Address,
      toAddresses: List[Address],
      amount: Double
  )

  case class Result(msg: String)
}

class SettlingActor(mixingService: MixingService)
    extends Actor
    with ActorLogging {

  import context.dispatcher

  def receive: Receive = {

    case Result(msg) => log.info(msg)

    case Msg(fromAddress, toAddresses, amount) =>
      Future
        .sequence(
          toAddresses
            .map(
              mixingService
                .transfer(fromAddress, _, amount)
            )
        )
        .map(_ => Result(s"Settled the transaction"))
        .pipeTo(self)
  }

}
