## Why

Four brokers already live as branches inside `Providers`, `Model` and the editor, and a dozen more are planned. Each new broker would touch five places and nothing enforces the rules (read-only, HTTPS through the paced client, long-lived credential). A plugin contract puts one broker in one file, lets the app derive its UI from the registry, and gives outside developers a documented way to add one.

## What Changes

- New package `dev.capital.brokers`: `BrokerPlugin` (name, site, credentials, id form and labels, optional account list and connect URL, `read`), `BrokerHost` (secrets, paced HTTPS send/text/json, time), `Reading`, `Credential`, `Response`, registry `Brokers.all`.
- Interactive Brokers, OANDA, Trading 212 and SnapTrade become plugins; `Providers` keeps only the host and the common result checks (currency, sign, time). `brokerChoices`, `brokerCredentials`, `accountId`, the Credentials buttons, the editor's id field, picker and connect button, and the attribution list derive from the registry.
- Developer guide `BROKER-PLUGINS.md` (contract, host API, errors, checklist); `BrokersTest` enforces registry invariants.
- Behaviour unchanged for users; two generic strings replace SnapTrade-specific ones in the editor.

## Capabilities

### Modified Capabilities
- `broker-accounts`: integrations are plugins behind one contract; the interface derives broker specifics from the registry.
