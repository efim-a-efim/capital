# Broker plugins

Every broker integration in Capital is a plugin: one Kotlin object in `app/src/main/java/dev/capital/brokers/` that implements `BrokerPlugin` and is listed in `Brokers.all` (`Plugin.kt`). The app never knows a broker by name anywhere else: the editor, the Credentials section, validation, refresh and the attribution list all read the registry. This page is the contract for adding one.

## What a plugin is allowed to do

- **Read one account's total value** in its base currency with a credential the user creates in the broker's own portal. Nothing else: no orders, no transfers, no changes at the broker. The only POST allowed besides reads is one that returns an authorization URL for the user's browser (see `connect`).
- **Long-lived credentials only.** The broker must officially issue a token, key pair or personal access token that stays valid until the user revokes it, or at least 30 days, and that the user can create alone. OAuth flows with short-lived tokens, logins with the account password or OTP, and unofficial APIs are not accepted; such brokers go into the *Other brokers* list of `docs/accounts.md` instead.
- **HTTPS through the host.** All traffic goes through `BrokerHost`, which paces requests, retries, maps common HTTP errors and bounds response sizes. Plugins never create their own HTTP client, threads or storage.

## The interface (`Plugin.kt`)

```kotlin
object Example: BrokerPlugin {
    override val name = "Example Broker"                         // unique, English, stored in snapshots: never rename
    override val site = "https://www.example.com"                 // attribution link in Settings
    override val credentials = listOf(                            // Secrets keys, unique across plugins; main token first
        Credential("Example Broker") { tr("Access token: {0}", "Example Broker") })
    override val idForm = Regex("[0-9]{1,20}")                    // form of the account id after normalizeId
    override fun idLabel() = tr("Example account number")        // label of the id field
    override fun idError() = tr("Enter the numeric Example account number")
    override suspend fun read(host: BrokerHost, id: String): Reading {
        val token = host.required("Example Broker", name)        // KeyArgument with the standard message when blank
        val a = host.json("https://api.example.com/v1/accounts/$id", headers = mapOf("Authorization" to "Bearer $token"))
        require(a.string("id") == id) { tr("Account mismatch") }
        return Reading(a.string("currency"), a.string("equity"), host.now)
    }
}
```

Optional members:

- `normalizeId(raw)`: trim, lower-case or reformat what the user typed before `idForm` is checked.
- `listsAccounts = true` + `accounts(host)`: return `id to label` pairs; the editor shows **Fetch accounts** and a picker. `noAccounts()` is the message when the list is empty.
- `connectLabel()` + `connect(host)`: a button that opens the returned HTTPS URL in the browser (aggregators' connection portals, consent pages). Keep it to one POST that changes nothing at the broker.

`Reading` is the result: ISO 4217 currency code, the total as decimal text (strings from JSON are fine, no rounding needed), and the time the broker observed it (epoch milliseconds; `host.now` when the broker gives no timestamp). The host checks the currency, rejects negative totals and stores the value; the plugin does not touch `Account` or `Holding`.

### Host API (`BrokerHost`)

| Member | Use |
|---|---|
| `secret(key)` | Stored credential, blank when the user has not entered it |
| `required(key, broker)` | Same, but throws the standard "needs its credentials" error naming the broker |
| `send(url, body?, headers, soft)` | GET when `body` is null, POST (JSON content type) otherwise; returns status, body and lower-cased headers; non-2xx fails unless the status is in `soft` |
| `text(...)`, `json(...)` | `send` returning the body, or the body parsed as a JSON object |
| `now` | Epoch milliseconds |

Only `https://` URLs are accepted. `send` already handles 401/403 ("Access denied; check provider key or quota"), 402, 429 and 5xx with retries and `Retry-After`; map broker-specific error payloads yourself when the broker answers 200 with an error body.

### Errors

Throw `dev.capital.data.ProviderFailure(tr("…"))` for anything the broker said or returned (the message is shown under the account and in the refresh summary), `dev.capital.data.KeyArgument(tr("…"))` for a credential the user must fix, and plain `require(...) { tr("…") }` for a response that does not describe the requested account. Messages are user-facing: short, translated with `tr`, never containing the token. Use `String.clean()` from the package before echoing provider text.

## Checklist for a new plugin

1. `brokers/<Name>.kt` with the object above; register it in `Brokers.all` (`Plugin.kt`). Order in that list is the order in the editor.
2. Unit test in `app/src/test/java/dev/capital/ProviderTest.kt` with `routeWith(secrets(...)) { request -> ... }`: assert the URL, auth headers, signing if any, the parsed currency and total, and one error path. Signing code gets a test with a documented vector when the broker publishes one. `BrokersTest` checks the registry invariants automatically.
3. Strings: every `tr("…")` in the plugin is a key. Run `python3 .tools/i18n-extract.py .tools/i18n/en.json` and add the new keys to all 14 other files under `app/src/main/assets/i18n/` (`I18nTest` fails until they exist).
4. Site: a section in `docs/accounts.md` (steps to create the credential, where to find the id, documentation links, the "menus may change" note) plus the row in its table, and the broker in the operator lists of `docs/privacy.md`, `docs/screens/brokers.md` and the landing page; all in 15 languages (`python3 .tools/site-check.py <code>` must print OK).
5. Reachability check in `scripts/check-providers.py` with a bogus credential, so a renamed endpoint is noticed.
6. Record it as an OpenSpec change (`openspec/changes/<name>/`), with the spec delta under `specs/broker-accounts/`.

## What stays out of a plugin

Credential storage (Android Keystore-backed `Secrets`), the account entity and linking, refresh scheduling, currency conversion, snapshots, the Brokers screen. If a broker needs something the host does not offer (a signing primitive, a second request type), extend `BrokerHost` in one place rather than working around it in the plugin.
