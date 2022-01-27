package com.gemini.jobcoin.service

import akka.actor.ActorSystem
import com.gemini.jobcoin.clients.JobcoinClient
import com.gemini.jobcoin.clients.models.{Transaction, TransferDetails}
import com.gemini.jobcoin.models.Models.{Address, DepositInfo}
import com.gemini.jobcoin.repository.DepositInfoRepository
import org.joda.time.DateTime

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class MixingService(
    depositInfoRepository: DepositInfoRepository,
    jobcoinClient: JobcoinClient
)(implicit ec: ExecutionContext, actorSystem: ActorSystem) {

  def getDepositAddress(withdrawalAddresses: List[Address]): Future[Address] = {
    val depositAddress = Address(UUID.randomUUID().toString)
    depositInfoRepository
      .save(DepositInfo(depositAddress, withdrawalAddresses))
  }

  def getDepositInfo(depositAddress: Address): Future[Option[DepositInfo]] =
    depositInfoRepository.get(depositAddress)

  def getTransactions(
      from: Option[DateTime],
      until: DateTime
  ): Future[List[Transaction]] = {

    jobcoinClient.getTransactions.map(resp => {
      val filteredTnxs =
        resp.transactions.filter(_.timestamp.isBefore(until))
      from match {
        case Some(ts) =>
          filteredTnxs.filter(_.timestamp.isAfter(ts))
        case None => filteredTnxs
      }
    })
  }

  def transfer(
      fromAddress: Address,
      toAddress: Address,
      amount: Double
  ): Future[Unit] = {

    // validate if the fromAddress has the balance
    jobcoinClient
      .getAddressInfo(fromAddress)
      .flatMap(addressInfo => {
        if (addressInfo.balance >= amount) {
          val transferDetails = TransferDetails(fromAddress, toAddress, amount)
          jobcoinClient.initiateTransfer(transferDetails)
        } else {
          Future.failed(throw new Exception("Doesnt have enough balance."))
        }
      })
  }
}
