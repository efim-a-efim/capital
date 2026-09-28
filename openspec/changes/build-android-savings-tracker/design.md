## Context

Greenfield Android app; SETUP.md is the source brief. No existing application or graphify database. Research and sources: [research.md](research.md). Screen preview: [ui-preview.html](ui-preview.html). This change plans implementation; the preview is not an Android build.

## Goals / Non-Goals

**Goals:** accurate savings valuation, understandable allocation, portable local records, usable offline, restrained Android UI.

**Non-goals:** custody, transactions, background polling, automatic tokens/staking discovery, historical charts without history, backend, custom sync engine.

## Decisions

### 1. Native, one module

Kotlin, Jetpack Compose Material 3, Android 8+ (API 26); compile/target current stable SDK at implementation. One app module with ui/domain/data packages; ViewModels, StateFlow, coroutines. Manual constructor wiring. OkHttp and kotlinx.serialization for HTTP/JSON. Gradle version catalog pins compatible stable versions.

Compose provides accessibility, native controls, lifecycle integration. Flutter adds a second runtime; XML adds UI boilerplate. No DI framework, Room, multi-module scaffolding, or generic plugin architecture initially.

### 2. Explicit data model and money precision

- Bucket: UUID, name, valuation currency, display order.
- Holding: UUID, bucket ID, label; manual quantity + asset OR chain + canonical address + last observed native balance.
- Goal: UUID, purpose, positive target, currency, local due date, integer priority (default 0; larger wins), active/archived.
- Connection: unique goal/bucket pair; mode Auto, Fixed amount, % of bucket, or % of goal; optional maximum % of goal on any mode.
- Quote: stable asset ID, USD price, source/as-of/fetched timestamps. Balance observation: base-unit integer, block metadata when available, source/timestamps, error.
- App data: schema version, revision ID, parent revision IDs, entities, quotes, provider selections, default currency.

Use BigInteger for chain units; BigDecimal for manual quantities, rates and amounts, serialized as strings. Validate nonnegative finite values; target > 0, percentages 0–100, valid currency/date/address, no duplicate wallet or dangling reference. Validate imported files and API responses with size limits. Do not use binary floating point for money. Currencies are fiat supported by selected sources plus the four native assets; native precision 8/18/9/6 decimals.

Canonical wallet key = mainnet chain + decoded address bytes (TON includes workchain). Accept valid address encodings/checksums, reject testnet; normalize equivalent TON and TRON forms. ETH comparison ignores checksum case after validation; preserve display form. Track one address, not an entire HD wallet. Moving a wallet updates its bucket; adding the same wallet elsewhere fails with a link to its existing bucket.

Manual entry records current quantity, not transactions. A manually valued investment can be a labeled fiat amount. Editing currency does not imply FX conversion: offer explicit “convert current amount” or “replace amount.” Bucket display currency changes never mutate holdings. Global currency changes affect overview and future defaults only.

### 3. Deterministic allocation

Allocation reserves value virtually; never transfers coins. Recalculate from one immutable balance/rate snapshot after any relevant edit or refresh. Use USD internally, independent of display currency; all user-entered limits remain in their declared units. Same-asset conversion is identity; cross-asset conversion uses one consistent USD quote set.

For each connection, compute an upper limit: Auto = goal target; Fixed = entered goal-currency amount; % bucket = percentage of pre-allocation bucket value; % goal = percentage of goal target. Intersect with optional goal-share maximum and goal target. Limits are ceilings, not guaranteed contributions; sums may exceed 100% without allocating beyond the target. Under-allocation stays visible. Never silently relax a limit.

Process priority groups highest first. For a group, all buckets act simultaneously:

1. Each bucket divides remaining value equally between eligible connected goals, capped by each connection's remaining limit; redistribute capped excess equally.
2. A goal whose combined offers exceed its unmet target accepts them proportionally; other goals accept all offers.
3. Commit accepted amounts together. Repeat for remaining capacity and unsatisfied connections until no value can move.
4. Before moving to lower priorities, reroute this group through residual bucket→goal paths when that can fund an unmet goal without reducing any already funded goal. Use breadth-first augmenting paths; choose least-funded eligible goal first (USD value), then UUID ties. Respect every connection cap. Repeat until no augmenting path exists. This prevents multi-bucket goals from stranding usable money. Lower priorities use only the remainder. No input-list ordering dependency. Equality means equal value from a shared bucket before different caps or goal saturation apply; not equal completion percentages.

Use 18-decimal USD allocation units, floor divisions so nothing is overdrawn. Distribute indivisible residual units by stable goal/bucket UUID order; never exceed caps. Show rounded amounts only at the UI boundary. Partial/missing quotes exclude unvalued holdings with an explicit incomplete result, never a fabricated zero; cached quotes keep allocations available but marked estimated/stale.

Examples: €900 shared by two uncapped peers → €450 each; targets €200 and €1,000 → €200/€700; priorities 2/1 with first target €600 → €600/€300. Two €100 buckets linked to the same €100 goal → €50 each. Test the mixed graph: A=€100 linked G1/G2; B=€100 linked G1; G1 target €50, G2 target €100 → after residual rerouting, G1 gets €50 from B, G2 gets €100 from A, B retains €50. Use a small domain-specific residual-flow routine, not an external optimization service.

### 4. Folder persistence and recovery

Onboarding chooses an existing or new dedicated local directory through SAF and persists URI permission. In-memory state reads/writes versioned JSON snapshots in that folder; no private database. Only URI grant/bootstrap metadata and device-bound API secrets live in app-private storage. Android Keystore protects secrets; exclude them from backups/exports; synced device re-enters keys. Disable Android automatic app-data backup.

Each save creates a uniquely named immutable snapshot containing parent revision(s), schema version, payload and SHA-256 checksum. Close, reopen, validate, then report success. Ignore partial/invalid snapshots; retain last good revision. Serialize saves within the process. Never truncate a valid snapshot. A valid complete revision is committed without reliance on atomic rename.

On startup/resume and before saving, inspect folder heads. A single descendant is safe to load; competing heads block editing until user compares and chooses a version. Preserve both; resolving creates a revision naming both parents. Recheck after write for races; unresolved branches remain visible. No timestamp winner and no silent field merge. Refresh results apply to current IDs/addresses and never resurrect deleted records. Keep snapshots initially; provide explicit backup/prune action only after conflict resolution. Entire-DAG scanning is acceptable for personal-scale data; add manifest/compaction when measured startup cost warrants it.

Revoke/move folder → preserve in-memory edits, offer reconnect or save copy, never show “saved.” Newer schema → read-only error; migrations create new snapshots, retaining originals. External sync is handled by the user's tools; the app cannot guarantee those tools support a chosen Android folder. Verify actual local-folder sync in acceptance testing.

### 5. Provider selection and refresh

Provider matrix and links: [research.md](research.md). Settings selects provider separately for each chain, crypto prices, and fiat; shows key requirement, coverage, attribution, last result. Ship two verified free options per service; Ethereum alternative is Alchemy Free with a user key; verify both endpoints before release. No silent fallback to a different operator. Manual tracking remains usable without keys.

Startup once per cold process launch and explicit Refresh all / Refresh bucket trigger network requests. Resume reloads local files only. No WorkManager, timers, notifications, or background polling. Backgrounding cancels outstanding calls; subsequent startup/manual action can retry. Deduplicate overlapping refreshes; one serial queue per provider with conservative pacing, 15-second request timeout, at most two retries for transient failures, capped backoff/jitter, honor Retry-After. Do not retry invalid credentials. A quota delay exceeding the active refresh budget ends with a retry-later status.

Batch crypto prices, fetch only required fiat pairs, query each address once. Bucket refresh updates its addresses and needed shared quotes; quote changes can revalue other buckets and are labeled accordingly. Successful observations persist together; failed ones retain prior values with per-item status. Unknown first-fetch values stay unknown. Display source observation time separately from fetch time. Stale after failed refresh or >24h wallet/crypto age; fiat warns after >7 calendar days to tolerate weekends/holidays. These are display rules, never polling triggers.

### 6. Interface

Use Material 3 controls, warm neutral surfaces, deep teal primary, amber warnings, red errors; system font with tabular amounts. 16dp content margins, 8dp spacing grid, 48dp targets, no gradients, decorative charts, or gratuitous animation. System light/dark, TalkBack labels, non-color status text, 200% font scaling, locale-aware numbers/dates. Compact bottom navigation: Overview, Buckets, Goals; Settings in app bar. Wide screens use navigation rail and list/detail panes.

| Screen | Required controls/content |
|---|---|
| Setup | Choose/reopen folder; default currency; provider/key setup (skippable); brief address-query disclosure. |
| Overview | Total valued savings, allocated/free amounts, goals funded/target (never added to savings), incomplete/stale banner, refresh, upcoming goals, bucket list. |
| Buckets | Totals and currency, source freshness, add; detail shows native + converted quantities, manual/wallet markers, allocated/free, linked goals, edit/move/delete, per-bucket refresh. |
| Holding editor | Manual/wallet mode, chain/currency, quantity or pasted address, label, validation, duplicate-owner link; Save/Cancel. QR scanning deferred; paste is sufficient. |
| Goals | Priority/due-date sorting, allocated/target, remaining, due date/overdue, add; detail explains each bucket's contribution, blocked-by-priority/limit status, priority editor, archive/delete. |
| Connection editor | Bucket selector, Auto/Fixed/% bucket/% goal, relevant amount/unit, optional goal-share cap, immediate allocation preview and shortfall explanation. |
| Settings | Default currency, theme, providers/free keys/test connection, storage location/reconnect/backup/restore, freshness/source details and attribution. |
| Recovery | Offline cached state, empty-state action, invalid/missing rates, failed writes, revoked folder access, conflict comparison with revision details and explicit keep choice. |

Delete requires confirmation when holdings/connections are affected; explain cascade. Archive goal releases allocation and preserves history of its definition. Edits have Save/Cancel and warn before discarding unsaved changes. Prototype demonstrates visual language and representative states; specs cover full behavior.

## Risks / Trade-offs

- Free endpoints change → verify contracts/coverage at implementation; selectable alternatives and cached observations.
- Shared files expose financial records → clear folder disclosure; use device encryption and trusted sync. Optional portable encryption deferred because unattended startup needs a key-unlock design.
- Full snapshots grow → explicit backup/pruning later; no automatic destructive cleanup.
- Multi-bucket fairness is subtle → equal proposals followed by non-stealing residual rerouting; permutation and conservation tests are mandatory.
- Rates and chains are not simultaneous → label estimates and observation age; never imply executable exchange prices.

## Migration Plan

Build in dependency order from tasks.md; validate domain/storage before integrations and UI. First release creates schema v1 or opens an existing valid folder. Each later migration preserves the source revision. Rollback uses saved copies; older apps reject newer schemas safely.

## Open Questions

None blocking planning. Provider smoke tests, current SDK/dependency versions, and device sync compatibility are implementation acceptance gates, not promises established by documentation research.

## Planning validation

OpenSpec strict validation passed; all four artifacts complete. HTML structure checked: six screens and valid anchor targets. Rendered visual inspection could not run: no browser is available through the UI tool. Native Android build, accessibility and device tests remain implementation tasks.
