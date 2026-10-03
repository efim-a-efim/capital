## Why

Broker accounts were holdings inside buckets, so their credentials sat in Settings, an account could only be created from a bucket, and the bucket editor mixed broker ids with wallet addresses. Accounts are connections to external services, not places where savings are counted: they deserve their own entity and screen, while buckets stay the way funds are tracked.

## What Changes

- New top-level entity **Account** (`Portfolio.accounts`): name, broker, account or query id, last value and currency, times, error, and an optional **Ignore balances less than** threshold in the default currency. Unique per broker and id.
- New bottom-bar tab **Brokers** (and rail item on wide screens): the list of accounts with value, link state and per-account Edit / Delete, a **+** editor for accounts, and the **Credentials** section moved here from Settings together with the setup-guide link.
- A bucket holding of type **Broker account** now only links an existing account (`Holding.accountId`) and mirrors its currency, value, times and error (`Portfolio.linked()`); one account can be in one bucket. Deleting the holding unlinks; deleting the account removes its holding.
- Snapshot schema 6: `accounts` list, `Holding.accountId`. Schema-5 broker holdings are migrated on open into an account plus a linked holding. Older builds refuse schema 6 as newer.
- Refresh reads every account on a full refresh and only the accounts linked into the bucket on a bucket refresh.
- Site: new screen page `screens/brokers` with a screenshot per language; `accounts`, manual, bucket and settings screen pages, landing page and store notes updated; all in 15 languages.

## Capabilities

### Modified Capabilities
- `broker-accounts`: accounts become a top-level entity with their own screen, linking replaces embedding, dust threshold, schema 6 migration.
- `android-interface`: a fifth bottom-bar tab.
