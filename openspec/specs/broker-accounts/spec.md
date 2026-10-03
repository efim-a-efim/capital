# broker-accounts Specification

## Purpose
TBD - created by archiving change add-broker-accounts. Update Purpose after archive.
## Requirements
### Requirement: Account holdings
A holding SHALL be trackable as a broker account: a broker from a fixed list (Interactive Brokers, OANDA, Trading 212, SnapTrade) plus an account or query id validated per broker. An account holding SHALL belong to one bucket, SHALL be unique per broker and id across the portfolio, SHALL carry no tokens, and SHALL have an unknown quantity until its first successful refresh.

#### Scenario: Add an OANDA account
- **WHEN** the user adds a holding of type Account with broker OANDA and id 001-001-1234567-001
- **THEN** the holding is saved with no balance, shown as "Balance unknown", and the bucket total is marked incomplete until refresh.

#### Scenario: Duplicate account
- **WHEN** the same broker id already belongs to another bucket
- **THEN** the editor refuses the save and names the bucket that holds it.

### Requirement: Read-only retrieval
On refresh the app SHALL read each account's total value (net asset value) and base currency through the broker's own HTTPS interface and SHALL store the value as the holding's quantity in that currency. Interactive Brokers SHALL be read through the Flex Web Service (SendRequest then GetStatement, version 3) with the Net Asset Value summary of an Activity Flex Query; the latest report date SHALL be used and SHALL be shown as the observation time. OANDA SHALL be read through the v20 REST account summary, Trading 212 through the account summary of its Public API, SnapTrade through the account detail's total balance. Every supported broker SHALL authenticate with a long-lived credential the user creates once (token, key pair or client id with consumer key); brokers that only issue short-lived OAuth tokens SHALL NOT be offered. The app SHALL send no request that creates, changes or cancels anything; the only POST is SnapTrade's Connection Portal login, which returns a URL for the user's browser.

#### Scenario: Interactive Brokers refresh
- **WHEN** the Flex query returns one statement whose NAV summary total is 12345.67 USD for 2026-10-02
- **THEN** the holding shows 12,345.67 USD observed on 2026-10-02 and the bucket converts it like any fiat amount.

#### Scenario: Statement still generating
- **WHEN** GetStatement answers "Statement generation in progress"
- **THEN** the app waits and retries a bounded number of times before reporting that the statement is not ready; the previous value stays.

#### Scenario: Several accounts in one query
- **WHEN** the Flex query returns more than one statement
- **THEN** the refresh fails for that holding with a message to make one query per account.

#### Scenario: SnapTrade account picker
- **WHEN** the user chooses SnapTrade in the holding editor and presses Fetch accounts
- **THEN** the connected accounts are listed by institution, name and number and choosing one fills the account id.

### Requirement: Tokens
Broker tokens SHALL be entered in Settings, stored encrypted on the device like provider keys, sent only to the broker that issued them, and excluded from snapshots, exports and OS backups. A missing token SHALL fail the holding's refresh with a message naming the broker; an expired or invalid token SHALL be reported as such without retrying.

#### Scenario: Expired Flex token
- **WHEN** Interactive Brokers answers error 1012
- **THEN** the holding keeps its previous value with the error "Token has expired" and no further request is made for it in that refresh.

### Requirement: Display and documentation
Account holdings SHALL be shown as read-only with the broker name and id, the value in the bucket currency, the observation and fetch times, and the same stale rule as wallets (24 hours). The site SHALL have a page describing, for each supported broker, how to create a read-only token and the required query, with links to the broker's documentation and a statement that the broker's screens may change. The page SHALL exist in every supported language and SHALL be linked from the holding editor and from Settings.

#### Scenario: Setup guide from the editor
- **WHEN** the user chooses the Account tracking type
- **THEN** the editor offers a link that opens the setup guide in the interface language.

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

### Requirement: Broker integrations are plugins
Every broker SHALL be one plugin object implementing `BrokerPlugin` and registered in `Brokers.all`. A plugin SHALL declare its name, site, credentials, id form with labels, and SHALL read one account's total value and currency through the host it is given; it MAY list accounts and MAY return an authorization URL for the browser. Plugins SHALL NOT create HTTP clients, store data or send requests that change anything at the broker. The app SHALL derive the broker list, the Credentials section, id validation, the editor's extra controls and the attribution links from the registry, so that adding a plugin needs no change elsewhere in the app. The contract SHALL be documented for developers in `BROKER-PLUGINS.md`.

#### Scenario: A plugin without extras
- **WHEN** a plugin declares no account list and no connect URL
- **THEN** the account editor shows only the broker's id field with the plugin's label.

#### Scenario: Registry invariants
- **WHEN** two plugins share a name or a credential key, or a plugin has no credentials
- **THEN** the registry test fails the build.

#### Scenario: Host checks the result
- **WHEN** a plugin returns a currency that is not a three-letter ISO code, a negative total or a timestamp in the future
- **THEN** the refresh of that account fails with a message and the previous value stays.

### Requirement: Additional broker plugins
The app SHALL offer Alpaca, Tradier, tastytrade, Public.com, eToro, Indexa Capital, T-Invest, ALOR, Capital.com and Akahu as broker plugins. Each SHALL authenticate only with credentials the user creates in the broker's own portal that stay valid until revoked, until a chosen expiry, or for at least three months; any short session the broker requires SHALL be created per read and not stored. Each SHALL list the user's accounts for the account picker and SHALL read one account's total value with its currency as documented by the broker.

#### Scenario: Pick an eToro account
- **WHEN** the user has entered both eToro keys and presses Fetch accounts in the account editor
- **THEN** the picker lists the eToro accounts with type and currency, and a refresh reads the chosen account's balance in its currency.

#### Scenario: Expired tastytrade grant
- **WHEN** tastytrade answers the token request with invalid_grant
- **THEN** the account shows "tastytrade rejected the refresh token or client secret; create a new grant" and keeps its previous value.

### Requirement: Support list by market
The site's broker page SHALL list the popular brokers of the markets of the app's languages and say for each whether it is read directly, through an aggregator, or why it cannot be read.

#### Scenario: Unsupported broker
- **WHEN** a user looks up Groww on the broker page
- **THEN** the table says it cannot be read because SEBI rules end every API session daily.

### Requirement: Scoped trust for the T-Invest API
The app SHALL trust the Russian Trusted Root CA for `invest-public-api.tbank.ru` only and SHALL keep the system trust store unchanged for every other host.

#### Scenario: Other host with a Russian certificate
- **WHEN** any other host presents a chain under the Russian Trusted Root CA
- **THEN** the connection fails as before.

