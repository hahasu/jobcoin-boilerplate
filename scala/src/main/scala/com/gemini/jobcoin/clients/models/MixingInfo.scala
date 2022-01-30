package com.gemini.jobcoin.clients.models

import com.gemini.jobcoin.models.Models.{Address, DepositInfo}
import org.joda.time.DateTime

case class MixingInfo(
    depositAddress: Address,
    depositInfo: DepositInfo,
    amount: Double,
    ts: DateTime
)
