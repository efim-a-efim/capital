## 1. Foundation and feasibility

- [x] 1.1 Create one Kotlin/Compose app module, pin compatible stable toolchain/dependencies, minSdk 26; verify debug build.
- [ ] 1.2 Prototype SAF directory selection, persistent grant and immutable write/read validation on emulator and device; verify chosen sync tool can access the folder. _(Emulator verified 2026-09-29, see acceptance.md; physical device + sync tool pending user.)_
- [x] 1.3 Smoke-test two documented free providers per data category using public test addresses; record endpoints, response contracts, asset IDs, quotas/key requirements and attribution. Replace unavailable candidates before integrating them.

## 2. Domain and allocation

- [x] 2.1 Implement entities, exact money serialization, field/import validation and versioned snapshot schema; verify precision round-trip.
- [x] 2.2 Implement chain-aware address validation/canonicalization and unique ownership/move; check equivalent TON encodings and invalid checksums.
- [x] 2.3 Implement USD quote conversion, native/converted valuations, missing/stale propagation, global/default currency behavior.
- [x] 2.4 Implement connection ceilings and simultaneous capped peer offers by descending priority; test equal sharing, percentage bases, saturation and priority examples.
- [x] 2.5 Implement residual rerouting without stealing funded totals; test the mixed graph, input-order invariance, caps/conservation and rounding residuals.
- [x] 2.6 Implement bucket/holding/goal CRUD, currency conversion versus replacement, archive/release, dependency-aware deletion and allocation explanations.

## 3. Durable local data

- [x] 3.1 Implement SAF snapshot repository, serialized saves, checksum/readback validation and revision loading; test interrupted write and corrupt file recovery.
- [x] 3.2 Implement revision ancestry/conflict detection and explicit resolution; test concurrent siblings, delayed ancestors and save races.
- [x] 3.3 Implement reconnect/save-copy, validated backup/restore, and safe migration/newer-schema rejection; verify originals survive failure.
- [x] 3.4 Store API secrets with Android Keystore protection, disable app-data backup, redact logs and enforce HTTPS; verify exports contain no credentials.

## 4. External observations

- [x] 4.1 Implement confirmed BTC and latest ETH adapters using selected providers; verify unit conversion, pending exclusion and chain validation against captured responses.
- [x] 4.2 Implement TON and TRX native adapters, including valid uninitialized account semantics; verify absent/error is distinguished from valid zero.
- [x] 4.3 Implement crypto and fiat quote adapters, alternate selections, coverage checks and attribution; verify stable asset mapping and inverse/cross conversions.
- [x] 4.4 Implement startup/manual refresh, deduplication, pacing, bounded retries/cancellation and partial-success persistence; test timeout, 429, bad key and malformed response.
- [x] 4.5 Guard refresh commits against edited/moved/deleted holdings and storage conflicts; test no resurrection and no false saved status.

## 5. Android interface

- [x] 5.1 Implement theme, navigation, folder/currency onboarding and optional provider/key setup following ui-preview.html and design.md.
- [x] 5.2 Implement overview, bucket list/detail and holding editor with native/converted values, add/edit/move/delete and refresh states.
- [x] 5.3 Implement goal list/detail/editor and connection editor, priorities, due dates, allocation previews, explanations and archive.
- [x] 5.4 Implement Settings: currency/theme, provider/key/test-connection, storage/recovery/backup/restore and attribution.
- [x] 5.5 Implement empty/loading/offline/incomplete/failed-write/conflict states, unsaved-edit protection and destructive confirmations.
- [x] 5.6 Verify TalkBack, contrast, 48dp targets, 200% fonts, decimal-comma locale, dark theme, compact phone and tablet layouts.

## 6. Acceptance and release

- [x] 6.1 Run domain/storage/provider checks and Android lint/debug build; fix failures before release.
- [x] 6.2 Exercise first launch → manual holding → wallet → mixed-currency goal → priority change → offline reopen → refresh end to end.
- [ ] 6.3 Exercise process death during save, revoked grant, offline concurrent edits and actual external-folder sync on device; verify data recovery and absence of background requests. _(All emulator scenarios pass, see acceptance.md; external sync-tool check pending user.)_
- [x] 6.4 Document install/build, folder setup, free-provider keys, supported native-balance scope, backups and recovery; produce installable debug APK for review.
