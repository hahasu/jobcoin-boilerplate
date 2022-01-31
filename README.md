# Scala Jobcoin
Simple base project for the Jobcoin project using Scala and SBT. It accepts return address as arguments and prints out a deposit address to the user for them to send their funds to. The rest of the application is left unimplemented.

### Run
`sbt run`


### Test
`sbt test`


## Implementation details
### Components
#### Service
* Mixing Service - this has all the helper methods to interact with the jobcoin api

#### Scheduler
* MixingScheduler - this is the main entry point to the app. it runs at a specific interval mentioned in the config and starts the mixing process

#### Actors
* Transactions Actor - this gets all the transactions eligible for mixing and sends transaction and deposit information needed for mixing to Mixing Actor 
* Mixing Actor - this sends the coins from house address to deposit address after deducting a fee and schedules the transfers to the withdrawal addresses with delay
* Settling Actor - this transfers the coins to the unused withdrawal addresses and settles the transaction

#### Repositories
* DepositInfoRepository - stores and retrieves the deposit info associated with the withdrawal addresses given by the user
* TransactionRepository - stores and retrieves the last processed transaction's time stamp 
