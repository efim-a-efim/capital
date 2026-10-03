## Why

Capital speaks 15 languages, but only four brokers could be read. Research across the markets of those languages (October 2026) found ten more brokers and aggregators whose official APIs give an individual a long-lived, self-created credential and a documented total account value.

## What Changes

- New plugins: Alpaca, Tradier, tastytrade (personal OAuth grant), Public.com (Individual API), eToro (Public API), Indexa Capital, T-Invest (T-Bank), ALOR, Capital.com, Akahu (New Zealand aggregator). Each lists the user's accounts for the picker and reads one total.
- T-Invest is served under the Russian Trusted Root CA; the app trusts that root for `invest-public-api.tbank.ru` only, through the network security config.
- Site: setup section per broker, a generic "Connecting an account" section, a support list of popular brokers by market (direct, through SnapTrade or Akahu, or why not possible), new messages; privacy operators list; 15 languages.
- Not implemented, with reasons on the site: Indian brokers (SEBI ends API sessions daily), Schwab, E*TRADE, Questrade (short-lived OAuth), IG (password sessions), BCS, Bitpanda, Freedom24, DNSE (no total value), Finam (value currency undocumented), Futu, Tiger, Longbridge (key lifetime or response format undocumented), brokers without public APIs.

## Capabilities

### Modified Capabilities
- `broker-accounts`: ten more brokers; support list by market.
