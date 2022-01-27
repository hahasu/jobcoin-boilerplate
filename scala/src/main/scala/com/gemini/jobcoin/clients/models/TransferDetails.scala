package com.gemini.jobcoin.clients.models

import com.gemini.jobcoin.models.Models.Address
import play.api.libs.json.{JsValue, Json}

import scala.language.implicitConversions

case class TransferDetails(
    fromAddress: Address,
    toAddress: Address,
    amount: Double
)

object TransferDetails {

  implicit def transferDetailsToJSValue(
      transferDetails: TransferDetails
  ): JsValue = Json
    .obj(
      "fromAddress" -> transferDetails.fromAddress.str,
      "toAddress" -> transferDetails.toAddress.str,
      "amount" -> transferDetails.amount.toString
    )
    .as[JsValue]
}
