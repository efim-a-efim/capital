## ADDED Requirements

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
