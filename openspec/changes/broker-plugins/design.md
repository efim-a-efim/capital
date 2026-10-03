## Design

- **One object per broker**, stateless, registered in a list; the list order is the editor order. The name is the storage key in snapshots and must never change.
- **Host, not client.** Plugins get a `BrokerHost` that wraps `Providers.send` (pacing, retries, HTTP error mapping, 8 MB cap, HTTPS only) and `Secrets`; they cannot reach OkHttp, storage or the model. Tests stub the host or route the OkHttp interceptor as before.
- **Result checking stays in the host.** `Reading` is raw text from the broker; `Providers.account` validates the currency, rejects negatives and future timestamps, then writes the `Account`.
- **Labels are functions** (`() -> String`) so the language switch re-renders them; names and keys are plain English constants.
- **Nothing broker-specific in the UI**: the editor asks the plugin for `idLabel`, `listsAccounts`, `connectLabel`, `noAccounts`; the Credentials section lists every plugin's credentials; Settings attribution appends `Brokers.all`.
- **Package cycle accepted**: `domain.Model` reads `brokers.Brokers` for choices and id validation while plugins use `domain.tr`; same module, no runtime cost. Splitting modules is not worth it for one app.
