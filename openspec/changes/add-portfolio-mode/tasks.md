## 1. Domain

- [ ] 1.1 Add bucket fields `portfolio`, `targets`, `allowSells` with validation; test totals, precision, asset ids, targets kept when the mode is off.
- [ ] 1.2 Implement `weights(bucketId)` with combined assets, base currency and unavailable handling; test the drift example, tokens, excluded and unknown tokens, missing rate.
- [ ] 1.3 Implement buy-only rebalancing; test the two spec examples, zero amount, single asset, asset not held, rounding remainder, sum equals amount, no negative buys, 100 random portfolios against a brute-force check.
- [ ] 1.4 Implement rebalancing with sells; test the spec example and that buys minus sells equal the amount.
- [ ] 1.5 Schema 4 conversion; test schema 1 to 3 inputs and schema 5 rejection.

## 2. Interface

- [ ] 2.1 Bucket editor: portfolio switch, sell permission, target rows with running total and validation messages.
- [ ] 2.2 Bucket detail: weights table with real, target and difference; unavailable state naming the asset.
- [ ] 2.3 Rebalancing calculator: amount input, buy and sell list with amounts and quantities, resulting weights, estimate note.

## 3. Acceptance

- [ ] 3.1 Unit tests, lint, debug build, instrumentation test.
- [ ] 3.2 Emulator run: switch mode on and off, targets persist, weights match a hand calculation, calculator examples, sells toggle, goals unchanged.
- [ ] 3.3 README and roadmap updated.
