## Why

Track savings and competing goals without accounts, custody, or a backend. Preserve portable local data while automating public wallet balances and exchange rates.

## What Changes

- Native Android app: overview, buckets, goals, settings.
- Manual balances and read-only BTC, ETH, TON/GRAM, TRX mainnet addresses.
- Currency conversion, configurable free providers, startup/manual refresh.
- Capped goal allocations with priorities and equal sharing between peers.
- User-selected storage folder, recoverable writes, explicit sync conflict resolution.
- Accessible Android UI; accompanying screen prototype and research-backed decisions.

## Capabilities

### New Capabilities

- `savings`: buckets, manual holdings, unique wallet ownership, currency valuation.
- `goal-allocation`: goals, connection limits, priorities, deterministic allocation.
- `local-storage`: portable persistence, recovery, schema migration, sync conflicts.
- `external-data`: providers, refresh orchestration, precision, failures and privacy.
- `android-interface`: navigation, editing, allocation transparency, accessibility.

### Modified Capabilities

None; repository contains requirements and OpenSpec scaffolding, no application.

## Impact

New single-module Kotlin Android application; Jetpack Compose/Material 3, lifecycle ViewModels, coroutines, kotlinx.serialization, OkHttp. No backend, database server, analytics, or custody. Public providers receive queried addresses and IP addresses. External sync remains user-managed.

## Non-goals

Transfers, signing, seed phrases, trading, transaction history, background polling, automatic token discovery, xpub wallet discovery, investment advice, custom sync service. Tokens/staking/DeFi are manually valued holdings initially; automatic tracking covers native spendable balances only.
