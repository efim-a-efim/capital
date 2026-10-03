## Design

- **Mirror, not lookup.** `Holding.accountId` links; the linked holding keeps a copy of the account's asset, quantity, times, source and error so that every existing computation (bucket value, allocation, staleness, rows) is untouched. `Portfolio.linked()` recomputes the copies and is applied on every edit, on refresh merge and when a snapshot is decoded; `validate()` rejects a drifted copy (quantity must equal the account's, or 0 when the account is ignored).
- **Threshold.** `Account.ignoreBelow` is an amount in `settings.currency`. `Portfolio.ignored(a)` converts the account's value with cached quotes; an unknown rate never ignores. The Brokers screen shows the real value and a *Counted as 0* / *Counted unless below* note.
- **Refresh.** `Providers.account(Account)` returns the updated account; `Observations.accounts` carries them; `mergeObservations` applies them only when the broker and id did not change meanwhile, then re-links.
- **Migration.** `Portfolio.withAccounts()` turns each schema-5 holding with `broker` into an `Account` (currency kept only when the holding had been fetched) and a holding with `accountId`; `Holding.broker` stays in the model for decoding and is rejected by `validate()`.
- **Name.** "Connections" was taken by goal↔bucket links, so the tab is *Brokers* and the entity *broker account*, the term the app already used.
