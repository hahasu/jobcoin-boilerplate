package com.gemini.jobcoin.clients

import akka.stream.Materializer
import com.gemini.jobcoin.clients.models.{
  AddressInfo,
  Transaction,
  TransactionResponse,
  TransferDetails
}
import com.gemini.jobcoin.models.Models.Address
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.ahc._

import scala.async.Async._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

trait JobcoinClient {

  def getAddressInfo(address: Address): Future[AddressInfo]

  def getTransactions: Future[TransactionResponse]

  def initiateTransfer(transferDetails: TransferDetails): Future[Unit]
}

class JobcoinClientImpl(config: Config, wsClient: StandaloneAhcWSClient)(
    implicit materializer: Materializer
) extends JobcoinClient
    with StrictLogging {

  private val apiAddressesUrl =
    config.getString("jobcoin.apiAddressesUrl")

  private val apiTransactionsUrl =
    config.getString("jobcoin.apiTransactionsUrl")

  override def getAddressInfo(address: Address): Future[AddressInfo] = async {

    logger.info(s"calling the apiAddressesUrl - $apiAddressesUrl")

    val response = await {
      wsClient
        .url(s"${apiAddressesUrl}/${address.str}")
        .get()
    }

    response
      .body[JsValue]
      .validate[AddressInfo]
      .get
  }

  // Can be optimized to get transactions based on time stamp
  override def getTransactions: Future[TransactionResponse] = async {

    logger.info(s"calling the apiTransactionsUrl - $apiTransactionsUrl")
    val response = await {
      wsClient
        .url(apiTransactionsUrl)
        .get()
    }

    val transactions = response
      .body[JsValue]
      .validate[List[Transaction]]
      .get

    TransactionResponse(transactions)
  }

  override def initiateTransfer(
      transferDetails: TransferDetails
  ): Future[Unit] = async {

    logger.info(s"calling the apiTransactionsUrl - $apiTransactionsUrl")
    val jsValue: JsValue = transferDetails
    val response = await {
      wsClient
        .url(apiTransactionsUrl)
        .post(jsValue)
    }

    if (response.status != 200) {
      logger.error("transfer failed")
    }

  }

}

object JobcoinClient {

  def apply(config: Config, wsClient: StandaloneAhcWSClient)(implicit
      materializer: Materializer
  ): JobcoinClient =
    new JobcoinClientImpl(config, wsClient)

}
