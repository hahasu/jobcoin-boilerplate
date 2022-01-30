package com.gemini.jobcoin.repository

import com.gemini.jobcoin.clients.models.ProcessedTransaction

import scala.concurrent.Future

trait TransactionRepository {

  def get(): Future[Option[ProcessedTransaction]]

  def save(processedTransaction: ProcessedTransaction): Future[Unit]
}

// dummy implementation
class TransactionRepositoryImpl extends TransactionRepository {

  var pt: Option[ProcessedTransaction] = None

  override def get(): Future[Option[ProcessedTransaction]] =
    Future.successful(pt)

  override def save(
      processedTransaction: ProcessedTransaction
  ): Future[Unit] = Future.successful {
    pt = Some(processedTransaction)
  }
}
