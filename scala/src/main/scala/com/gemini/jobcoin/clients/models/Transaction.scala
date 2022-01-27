package com.gemini.jobcoin.clients.models

import org.joda.time.DateTime
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Reads, __}

case class Transaction(
    fromAddress: Option[String],
    toAddress: String,
    amount: Double,
    timestamp: DateTime
)

object Transaction {
  import play.api.libs.json.JodaReads
  implicit val dateTimeJsReader: Reads[DateTime] =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  implicit val transactionReads: Reads[Transaction] = (
    (__ \ "fromAddress").readNullable[String] and
      (__ \ "toAddress").read[String] and
      (__ \ "amount").read[String].map(_.toDouble) and
      (__ \ "timestamp").read[DateTime]
  )(Transaction.apply _)

}
