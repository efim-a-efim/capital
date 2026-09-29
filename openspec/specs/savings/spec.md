# savings Specification

## Purpose
Buckets, manual holdings, wallet ownership and currency valuation.
## Requirements
### Requirement: Buckets and holdings
The app SHALL create, edit, and delete named buckets containing manual nonnegative quantities or read-only native mainnet BTC, ETH, TON/GRAM, TRX addresses. Each bucket SHALL have a valuation currency. Deleting a populated bucket SHALL require confirmation listing affected holdings/connections.

#### Scenario: Mixed currencies
- **WHEN** a EUR bucket contains USD cash and BTC
- **THEN** each holding shows native quantity and EUR value, and the bucket totals converted values once.

#### Scenario: Invalid manual quantity
- **WHEN** a user enters a negative or malformed quantity
- **THEN** Save is blocked with a field error and existing data is preserved.

### Requirement: Unique canonical wallet ownership
A canonical chain/address pair SHALL belong to exactly one bucket. The app SHALL validate mainnet encoding/checksum, normalize equivalent encodings, identify the existing owner on duplicates, and support moving the wallet.

#### Scenario: Equivalent TON addresses
- **WHEN** the same TON account is added with another valid friendly encoding
- **THEN** duplication is rejected and the existing bucket is identified.

### Requirement: Accurate and transparent valuation
The app SHALL preserve integer chain units and decimal money precision. Unknown values SHALL remain unknown; cached values SHALL retain timestamps and stale status. Summary savings SHALL count each holding once and never add goal allocations to savings.

#### Scenario: Missing price
- **WHEN** a holding has no usable quote
- **THEN** its native quantity remains visible, totals say incomplete, and unpriced value does not fund goals.

### Requirement: Currency defaults and edits
Global currency SHALL default new buckets/goals and value the overview without changing existing entity currencies. Changing a bucket currency SHALL only change valuation. Changing a manual holding or goal currency SHALL require explicit conversion of its amount or entry of a replacement amount; fixed connection limits SHALL be converted or explicitly replaced with the goal currency.

#### Scenario: Global default changes
- **WHEN** EUR is changed to USD in Settings
- **THEN** existing EUR goals keep their amounts/currency and new goals default to USD.

