## ADDED Requirements

### Requirement: Broker integrations are plugins
Every broker SHALL be one plugin object implementing `BrokerPlugin` and registered in `Brokers.all`. A plugin SHALL declare its name, site, credentials, id form with labels, and SHALL read one account's total value and currency through the host it is given; it MAY list accounts and MAY return an authorization URL for the browser. Plugins SHALL NOT create HTTP clients, store data or send requests that change anything at the broker. The app SHALL derive the broker list, the Credentials section, id validation, the editor's extra controls and the attribution links from the registry, so that adding a plugin needs no change elsewhere in the app. The contract SHALL be documented for developers in `BROKER-PLUGINS.md`.

#### Scenario: A plugin without extras
- **WHEN** a plugin declares no account list and no connect URL
- **THEN** the account editor shows only the broker's id field with the plugin's label.

#### Scenario: Registry invariants
- **WHEN** two plugins share a name or a credential key, or a plugin has no credentials
- **THEN** the registry test fails the build.

#### Scenario: Host checks the result
- **WHEN** a plugin returns a currency that is not a three-letter ISO code, a negative total or a timestamp in the future
- **THEN** the refresh of that account fails with a message and the previous value stays.
