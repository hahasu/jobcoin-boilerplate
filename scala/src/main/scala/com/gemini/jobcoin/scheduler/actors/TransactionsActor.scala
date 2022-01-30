package com.gemini.jobcoin.scheduler.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.pipe
import com.gemini.jobcoin.clients.models.MixingInfo
import com.gemini.jobcoin.repository.TransactionRepository
import com.gemini.jobcoin.service.MixingService
object TransactionsActor {

  case object Msg

  case class Result(mixingInfoes: List[MixingInfo])

  def apply(
      actorSystem: ActorSystem,
      mixingService: MixingService,
      mixingActorRef: ActorRef,
      transactionRepository: TransactionRepository
  ): ActorRef = actorSystem.actorOf(
    Props(
      new TransactionsActor(
        mixingService,
        mixingActorRef,
        transactionRepository
      )
    )
  )
}

class TransactionsActor(
    mixingService: MixingService,
    mixingActorRef: ActorRef,
    transactionRepository: TransactionRepository
) extends Actor
    with ActorLogging {

  import context.dispatcher

  def receive: Receive = {

    case TransactionsActor.Msg =>
      transactionRepository
        .get()
        .flatMap(pt => {
          mixingService
            .getMixingInfoes(pt.map(_.ts))
            .map(mixingInfoes => {
              TransactionsActor.Result(mixingInfoes)
            })
            .pipeTo(self)
        })

    case TransactionsActor.Result(mixingInfoes) =>
      mixingInfoes.foreach(mixingActorRef ! _)
  }

}
