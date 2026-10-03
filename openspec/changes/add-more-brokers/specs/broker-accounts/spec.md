## ADDED Requirements

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
