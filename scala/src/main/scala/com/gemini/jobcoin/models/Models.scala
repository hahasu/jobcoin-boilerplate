package com.gemini.jobcoin.models

object Models {

  case class Address(str: String)

  case class DepositInfo(
      depositAddress: Address,
      withdrawalAddresses: List[Address]
  )

}
