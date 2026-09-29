# Roadmap

## Version 1 — released as `v1.0.0`

Buckets, manual holdings, read-only BTC/ETH/TON/TRX wallets with tokens, goals ranked by date, planned savings with closure projection, portable snapshot folder with conflict handling. Specs: `openspec/specs/`.

## Version 2

### Portfolio support

- New bucket setting **Portfolio mode**. It can be switched on and off at any time without losing the bucket's targets.
- In portfolio mode a bucket is a simple investment portfolio. The user sets a target percentage for each currency or asset in the bucket. The bucket always shows real and target percentage side by side.
- Percentages are calculated in the base currency, which is the default currency from Settings.
- **Rebalancing calculator.** The user enters an amount to invest in the base currency. The app calculates what to buy to come as close to the targets as possible.
- By default the app never recommends selling. The bucket setting **Allow sells during rebalance** lets it recommend selling some assets to buy others.

Proposal: `openspec/changes/add-portfolio-mode/`.

### Encryption and locking

- Data encryption with a password.
- App lock with a PIN and optional biometry.
- Encryption can be switched on or off by the user at any time. Data is decrypted when encryption is switched off.

Proposal: `openspec/changes/add-encryption-and-lock/`.

## Open items carried over from version 1

- SAF folder access and third-party folder sync verified on a physical device.
- TalkBack run of the goal "Move up" and "Move down" actions.
- More than 100 token types per wallet.
