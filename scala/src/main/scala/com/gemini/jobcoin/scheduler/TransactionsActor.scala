package com.gemini.jobcoin.scheduler

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.pipe
import com.gemini.jobcoin.clients.models.Transaction
import com.gemini.jobcoin.service.MixingService
import org.joda.time.{DateTime, DateTimeZone}
object TransactionsActor {

  case object Msg

  case class Result(transactions: List[Transaction])
}

class TransactionsActor(
    mixingService: MixingService,
    mixingActorRef: ActorRef
) extends Actor
    with ActorLogging {

  import context.dispatcher

  var from: Option[DateTime] =
    None // for testing, need to get this from last processed transactions date

  def receive: Receive = {

    case TransactionsActor.Msg =>
      val until = DateTime.now(DateTimeZone.UTC)
      mixingService
        .getTransactions(from, until)
        .map(tnxs => {
          from = Some(until)
          TransactionsActor.Result(tnxs)
        })
        .pipeTo(self)

    case TransactionsActor.Result(transactions) =>
      transactions.foreach(mixingActorRef ! _)
  }

}
