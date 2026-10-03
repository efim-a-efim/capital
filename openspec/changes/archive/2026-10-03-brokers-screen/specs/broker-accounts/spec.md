## ADDED Requirements

### Requirement: Broker accounts are a top-level entity
A broker account SHALL exist independently of buckets: a name, a broker from the supported list, an account or query id validated per broker, and the last value read with its currency. Accounts SHALL be unique per broker and id. A bucket SHALL count an account only through a holding of type Broker account that links to it; one account SHALL be linked into at most one bucket at a time. The linked holding SHALL mirror the account's currency, value, observation and fetch times and error. Deleting the holding SHALL unlink the account; deleting the account SHALL remove its linked holding.

#### Scenario: Link an account into a bucket
- **WHEN** the user adds a holding of type Broker account in a bucket and chooses an existing account
- **THEN** the holding takes the account's name when left blank, shows the account's value in the account's currency, and the Brokers screen shows *Linked to* that bucket.

#### Scenario: Account already linked
- **WHEN** the chosen account is already linked into another bucket
- **THEN** the editor refuses the save and names that bucket.

#### Scenario: Delete an account
- **WHEN** the user deletes an account that is linked into a bucket
- **THEN** the confirmation says the holding leaves its bucket, and after confirming both the account and the holding are gone.

### Requirement: Brokers screen
The app SHALL have a Brokers tab in the bottom bar (a rail item on wide screens) listing every broker account with its value in its own currency and in the default currency, its times, its error and its link state, with Edit and Delete per account and a + button to add one. The brokers' credentials and the setup-guide link SHALL be entered on this screen under Credentials, not in Settings. A refresh from this screen SHALL read every account; a refresh of a bucket SHALL read only the accounts linked into it.

#### Scenario: Missing credentials
- **WHEN** an account is refreshed without its broker's credential
- **THEN** the account and its linked holding show "<broker> needs its credentials on the Brokers screen" and keep their last value.

### Requirement: Ignore small balances
An account MAY carry a threshold in the default currency. When the account's last value, converted with the cached rates, is below the threshold, the linked holding SHALL count as 0 while the Brokers screen keeps showing the real value. Without a rate for the account's currency nothing SHALL be ignored. The threshold defaults to 1 when switched on.

#### Scenario: Dust ignored
- **WHEN** an account holds 0.40 USD, the threshold is 1 EUR and a USD rate is cached
- **THEN** the bucket counts 0 for it and the Brokers screen says *Counted as 0: below €1.00* under the account.

### Requirement: Schema 6 migration
Snapshots of schema 5 SHALL open: each holding with a broker SHALL become a broker account plus a holding linked to it, keeping the value, currency (only if it had been fetched), times and error. Snapshots of schema 6 SHALL be refused by older builds as newer.

#### Scenario: Open a schema-5 folder
- **WHEN** a folder saved by version 2.6 with an Interactive Brokers holding is opened
- **THEN** the Brokers tab lists that account, the bucket still shows the holding with the same value, and the next save writes schema 6.
