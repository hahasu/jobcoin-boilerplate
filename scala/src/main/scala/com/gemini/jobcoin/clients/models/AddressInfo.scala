package com.gemini.jobcoin.clients.models

import play.api.libs.json.{Reads, __}

case class AddressInfo(
    balance: Double
)

object AddressInfo {

  implicit val reads: Reads[AddressInfo] =
    (__ \ "balance").read[String].map(_.toDouble).map(AddressInfo.apply)
}
