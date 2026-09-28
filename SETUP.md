# Personal savings tracker application

Android application that tracks personal finance:
* Investments/Savings
* Goals

## Functional requirements:
Savings and investments:
- Investments/savings are divided into "buckets" - separate places where money reside, like groups of wallets.
- Automatic tracking of BTC, ETH, TON/GRAM and TRX wallets with savings. A wallet can be added to only 1 bucket.
- Manual tracking - ability to add a saving with manual input.
- User selects default currency for each bucket. All currencies in the bucket are represented in 2 forms: the currency native form and a value converted to the default currency.

Goals:
- Goal is a purpose + amount of money + date. Semantics: "need to save X money for Y purpose at TTTTTTT date"
- Goal has a mandayory setting - currency. This currency is used to calculate everything related to the goal.

Goals and bucket connections.
- Goal can be connected to any number of saving buckets. In such case, connection is configured to take particular amount in percent or in goal's currency from the bucket into the goal. Default is to take as much as possible for goal's needs.
- If goal uses diffferent buckets, user can set percentage (relative to the goal value) to be consumed from the bucket. So that user can balance utilization between different buckets for each goal.
- Goal priorities. Goals may have priorities (numbers). If 2 or more goals are connected to the same bucket, behavior depends on priorities:
  * If priorities are the same, goals utilize bucket's savings equally.
  * If priorities are diffferent, the goal with higher priority utilizes as much as it needs from the bucket. The goal with lower priority utilizes only the money that were not utilized by higher-priority goals.

## Non-functional requirements
- All data is stored only locally on the user's device. Data is stored in a directory that can be synced via Rclone/Syncthing/etc, e.g. not in app's sandbox.
- Currency conversion is fully automated, currency conversion rates are taken from free public sources.
- All external services the application uses (currencies rates, ETH/TRX/BTC wallet info retrieval) must be free well-known stable services. If several options available, user must be able to select a particular service in app settings.
- Currencies and wallets state is not auto-updated in background. Update occurs only during app startup or by user request ("refresh" button) for each bucket or for everything.
- User sets default currency in app Settings and this currency is used by default for new entities + for showing summary of savings and goals.