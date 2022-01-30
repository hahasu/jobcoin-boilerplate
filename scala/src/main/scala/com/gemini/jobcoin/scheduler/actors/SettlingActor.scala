package com.gemini.jobcoin.scheduler.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.pipe
import com.gemini.jobcoin.clients.models.ProcessedTransaction
import com.gemini.jobcoin.models.Models.Address
import com.gemini.jobcoin.repository.TransactionRepository
import com.gemini.jobcoin.service.MixingService
import org.joda.time.DateTime

import scala.concurrent.Future

object SettlingActor {

  case class Msg(
      fromAddress: Address,
      toAddresses: List[Address],
      amount: Double,
      ts: DateTime
  )

  case class Result(msg: String)

  def apply(
      actorSystem: ActorSystem,
      mixingService: MixingService,
      transactionRepository: TransactionRepository
  ): ActorRef =
    actorSystem.actorOf(
      Props(new SettlingActor(mixingService, transactionRepository))
    )
}

class SettlingActor(
    mixingService: MixingService,
    transactionRepository: TransactionRepository
) extends Actor
    with ActorLogging {

  import context.dispatcher

  def receive: Receive = {

    case SettlingActor.Result(msg) => log.info(msg)

    case SettlingActor.Msg(fromAddress, toAddresses, amount, ts) =>
      Future
        .sequence(
          toAddresses.map(toAddress =>
            mixingService.transfer(fromAddress, toAddress, amount)
          )
        )
        .flatMap(_ => transactionRepository.save(ProcessedTransaction(ts)))
        .map(_ => SettlingActor.Result(s"Settled the transaction"))
        .pipeTo(self)
  }

}
