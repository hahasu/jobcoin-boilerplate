package com.gemini.jobcoin.service

import com.gemini.jobcoin.clients.JobcoinClient
import com.gemini.jobcoin.clients.models.{
  MixingInfo,
  Transaction,
  TransferDetails
}
import com.gemini.jobcoin.models.Models.{Address, DepositInfo}
import com.gemini.jobcoin.repository.DepositInfoRepository
import org.joda.time.DateTime

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class MixingService(
    depositInfoRepository: DepositInfoRepository,
    jobcoinClient: JobcoinClient
)(implicit ec: ExecutionContext) {

  def getDepositAddress(withdrawalAddresses: List[Address]): Future[Address] = {
    val depositAddress = Address(UUID.randomUUID().toString)
    depositInfoRepository
      .save(DepositInfo(depositAddress, withdrawalAddresses))
  }

  def getDepositInfo(depositAddress: Address): Future[Option[DepositInfo]] =
    depositInfoRepository.get(depositAddress)

  def getTransactions(
      from: Option[DateTime]
  ): Future[List[Transaction]] = jobcoinClient.getTransactions.map(resp => {
    val tnxs = resp.transactions
      .drop(805) // for testing

    from match {
      case Some(ts) => tnxs.filter(_.timestamp.isAfter(ts))
      case _        => tnxs
    }

  })

  def getMixingInfoes(
      from: Option[DateTime]
  ): Future[List[MixingInfo]] = getTransactions(from).flatMap(tnxs => {
    Future
      .sequence(tnxs.map(tnx => {
        val depositedAddress = Address(tnx.toAddress)
        getDepositInfo(depositedAddress)
          .map(
            _.map(depositInfo =>
              MixingInfo(
                depositedAddress,
                depositInfo,
                tnx.amount,
                tnx.timestamp
              )
            )
          )
      }))
      .map(_.flatten)
  })

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

object MixingService {
  def apply(
      depositInfoRepository: DepositInfoRepository,
      jobcoinClient: JobcoinClient
  )(implicit ec: ExecutionContext): MixingService =
    new MixingService(depositInfoRepository, jobcoinClient)
}
