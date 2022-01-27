package com.gemini.jobcoin.repository

import com.gemini.jobcoin.models.Models.{Address, DepositInfo}

import scala.collection.mutable
import scala.concurrent.Future

trait DepositInfoRepository {

  def get(depositAddress: Address): Future[Option[DepositInfo]]

  def save(depositInfo: DepositInfo): Future[Address]
}

// dummy implementation
class DepositInfoRepositoryImpl extends DepositInfoRepository {

  val map = mutable.Map.empty[Address, DepositInfo]

  override def get(depositAddress: Address): Future[Option[DepositInfo]] =
    Future.successful(map.get(depositAddress))

  override def save(depositInfo: DepositInfo): Future[Address] = {
    map += (depositInfo.depositAddress -> depositInfo)
    Future.successful(depositInfo.depositAddress)
  }
}
