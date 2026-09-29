## Context

A bucket has an id, name and display currency. Holdings carry an asset id and quantity; wallets also carry tokens. All rates are stored against USD. Schema is 3.

## Goals / Non-Goals

**Goals:** weights and drift at a glance; a calculator that answers "what do I buy with this money"; no effect on goals or stored balances.

**Non-Goals:** executing trades, price history, performance or return figures, fees and taxes, minimum order sizes, tolerance bands, scheduled rebalancing, portfolios spanning several buckets, investment advice.

## Decisions

### 1. Data model
`Bucket` gains `portfolio: Boolean = false`, `targets: Map<String, String> = emptyMap()` (asset id to percentage as decimal string) and `allowSells: Boolean = false`. Targets stay stored when the mode is off. Validation runs only when `portfolio` is true: each value 0 to 100 with at most two decimals, valid asset ids, total exactly 100.
Alternative rejected: a separate portfolio entity linked to a bucket. One portfolio per bucket needs no second identity.

### 2. Weights
`Portfolio.weights(bucketId)` groups native quantities and known, non-excluded tokens by asset id, converts each to the base currency and returns value, real share and target. Any unconvertible asset makes the whole result unavailable, because a share of a partial total is misleading.
Percentages are calculated in the base currency as requested. Shares are ratios, so they are the same in any currency as long as one consistent rate set is used.

### 3. Buy-only calculation
Let `c_i` be current values, `t_i` target fractions, `A` the amount and `T = sum(c) + A`. Minimise `sum((c_i + b_i - t_i*T)^2)` subject to `b_i >= 0` and `sum(b) = A`.
The solution is `b_i = max(0, t_i*T - c_i - m)` with `m` chosen so the buys total `A`. Sort assets by deficit `t_i*T - c_i` descending and widen the active set until the next deficit is below the level `m`. This is exact, needs no iteration to convergence, and is deterministic.
Alternative rejected: distributing new money in proportion to targets. It ignores existing drift.
Alternative rejected: proportional to deficits. It does not minimise the remaining distance when money is short.

### 4. With sells
`b_i = t_i*T - c_i`, negative values are sells. The sum equals `A` by construction.

### 5. Rounding
Amounts use 18-decimal arithmetic rounded down; the indivisible remainder goes to the asset with the largest deficit, ties by asset id. Display rounds to the currency's fraction digits. Quantities use the asset's own precision.

### 6. Interface
Bucket editor: switches "Portfolio mode" and "Allow sells during rebalance", target rows with add and remove, a running total with the missing or excess percentage. Bucket detail in portfolio mode: table of asset, value, real, target, difference in text with sign; button "Rebalance" opens the calculator with the amount field and result list. Buy and sell are words, not colors.

### 7. Schema 4
New fields have defaults, so older payloads decode unchanged. `SCHEMA = 4`; older builds reject schema 4.

## Risks / Trade-offs

- Cached rates make the result an estimate → show rate age and the estimate label in the result.
- Read-only wallets cannot be traded in the app → stated next to the result.
- Tokens change classification between refreshes → targets for an asset that became unknown or excluded are kept but flagged, and weights are unavailable until the target is removed or the asset is priced again.

## Migration Plan

Open old folder, defaults apply, first save writes schema 4. Rollback by restoring a backup exported before upgrading.

## Open Questions

- Should a target for an asset the bucket cannot hold through any holding create a manual holding automatically after the user buys? Proposed: no, the user adds the holding.
