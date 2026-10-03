## Design

- **Every plugin follows its broker's API reference**; unit tests replay the documented response examples (Alpaca, Tradier, tastytrade, Public, eToro, Indexa, T-Invest, Capital.com, Akahu) or schema-shaped responses where the reference has none. Live checks only confirm reachability and the error for a wrong credential (`scripts/check-providers.py`); no test accounts were created.
- **Short sessions are internal.** tastytrade (15-minute access token), Public.com (5-minute token), ALOR (30-minute JWT) and Capital.com (10-minute session) trade the stored long-lived credential for a session on every read; nothing short-lived is stored.
- **Currency.** Taken from the response where documented (Alpaca, tastytrade, eToro, T-Invest, Capital.com, Akahu, Indexa via the account record). Tradier and Public.com document USD-only accounts without a currency field; ALOR's Moscow Exchange valuation is in roubles.
- **Trading-capable keys.** ALOR and Capital.com offer no read-only credential; the guide says so and the app still only reads. eToro and T-Invest guides ask for read-only keys.
- **Russian root CA.** Added to `res/raw` from the official source and checked against the chain the server presents (SHA-256 D2:6D:2D:02…CF:31); scoped by `domain-config` to the single T-Invest host, with system anchors kept everywhere.
