# Acceptance — broker plugins (2026-10-03, emulator Medium_Phone, debug build)

| # | Check | Result |
|---|---|---|
| N1 | Account editor derives its controls from the plugin: Interactive Brokers and OANDA show only the id field with the plugin's label; SnapTrade shows Fetch accounts, Connect a brokerage through SnapTrade and its id field | PASS — `screenshots/N1-editor-snaptrade.png` |
| N2 | Credentials section lists every plugin's credentials with the plugin labels; attribution list includes the plugins' sites | PASS |
| N3 | `BrokersTest` registry invariants (unique names and keys, labels, id forms, defaults), `ProviderTest` broker reads unchanged, 102/102 unit tests, `I18nTest` with the 2 new strings in 15 languages, `site-check.py` OK for 14 languages | PASS |
| N4 | Instrumentation `AppTest` on the emulator | PASS |
