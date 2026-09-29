## ADDED Requirements

### Requirement: Token discovery
On refresh the app SHALL fetch fungible token balances (ERC-20, jetton, TRC-20) for each tracked ETH, TON and TRX wallet address from the token source selected for that chain. Zero balances and non-fungible tokens SHALL be ignored. Each chain's token source SHALL be selectable and SHALL include "Off". A token-source failure SHALL keep the previously stored tokens and record an error without affecting the native balance.

#### Scenario: Wallet holds tokens
- **WHEN** a tracked ETH address holds USDT and refresh succeeds
- **THEN** the holding lists USDT with its quantity next to the native ETH balance.

#### Scenario: Token source off
- **WHEN** the token source for a chain is "Off"
- **THEN** no token request is sent for that chain and stored tokens for its wallets are removed on the next refresh.

#### Scenario: Token source fails
- **WHEN** the token source times out while the native balance succeeds
- **THEN** the new native balance is stored, prior tokens remain, and the holding shows a token error.

### Requirement: Contract-address identity
A token SHALL be identified only by chain plus canonical contract address. Symbol and name from any provider SHALL be treated as untrusted display text and SHALL never be used to match a token to a price or to another asset. Contract addresses SHALL be validated with the same rules as wallet addresses; tokens with invalid contracts SHALL be dropped.

#### Scenario: Fake USDT
- **WHEN** a wallet holds a token named "USDT" whose contract differs from the contract the price provider lists
- **THEN** it is shown as an unknown token with its contract address and adds nothing to any total.

#### Scenario: Same contract, different encodings
- **WHEN** providers report one TON jetton master in raw and user-friendly form
- **THEN** both resolve to the same token and the same quote.

### Requirement: Known and unknown tokens
A token SHALL be known only when the selected crypto price provider returns a positive USD price for its contract address on its chain. All other tokens SHALL be labelled "Unknown token" in the UI with a non-color text marker. Unknown tokens SHALL be excluded from bucket values, totals, allocation and goal funding, and SHALL NOT mark a valuation incomplete or stale. A provider answer of "not listed" for a previously known token SHALL make it unknown; a request failure SHALL keep the prior quote marked with the error.

#### Scenario: Unlisted token
- **WHEN** the price provider has no entry for a token contract
- **THEN** the token displays "Unknown token · not counted" and bucket totals are unchanged by it.

#### Scenario: Price request fails
- **WHEN** a known token's price request fails
- **THEN** the cached quote keeps valuing it and the valuation is marked stale.

### Requirement: Known token valuation
Known tokens SHALL be valued as quantity times contract quote using exact decimals, and SHALL count toward bucket value, totals and allocation exactly like native balances. Quantities SHALL use the token's on-chain decimals; a known token whose decimals cannot be determined SHALL be treated as unknown.

#### Scenario: Stablecoin counted
- **WHEN** a wallet holds 250 known USDT at 1.00 USD
- **THEN** its bucket value increases by 250 USD converted to the bucket currency.

### Requirement: Bounded token work
The app SHALL store at most 100 tokens per wallet and SHALL bound price lookups per refresh. The bound SHALL respect the selected price provider's free quota. Part of the bound SHALL refresh tokens that already have a quote and part SHALL classify unchecked tokens, oldest check first and shared between wallets, so every token is eventually classified. After a provider failure or quota response no further lookups SHALL be sent in that refresh. Tokens not yet checked SHALL be displayed as unknown.

#### Scenario: Spam-filled wallet
- **WHEN** a wallet holds thousands of airdropped tokens
- **THEN** refresh completes within the lookup budget and known tokens keep current prices.

### Requirement: Snapshot compatibility
Snapshots SHALL use schema 2 with tokens stored inside holdings. Schema 1 snapshots SHALL open without modification of the original file, with token sources defaulted. Apps that only know schema 1 SHALL reject schema 2 as a newer schema.

#### Scenario: Open old folder
- **WHEN** a folder containing only schema 1 snapshots is opened
- **THEN** records load, token sources take defaults, and the next save writes a schema 2 snapshot while the originals remain.

#### Scenario: Price provider out of quota
- **WHEN** the price provider answers that the request quota is exhausted
- **THEN** lookups stop for that refresh, cached token prices remain, and one quota message is shown.

### Requirement: Token exclusion
The holding editor for an ETH, TON or TRX wallet SHALL offer "Fetch tokens", which loads the address's tokens and amounts from the selected token source without saving, and a switch per token. A switched-off token SHALL be excluded: not listed with the wallet, not priced, not counted in any total or allocation. Exclusions SHALL be stored by contract address with the holding, SHALL survive refreshes, and SHALL be reversible in the editor. Editing a wallet without changing its address SHALL keep its stored tokens.

#### Scenario: Exclude a priced token
- **WHEN** the user switches off a known token and saves
- **THEN** the wallet value drops by that token's value and the token is no longer listed.

#### Scenario: Re-enable
- **WHEN** the token is switched on again and saved
- **THEN** it is listed and counted after the next refresh, or immediately if its price is cached.

#### Scenario: Rename wallet
- **WHEN** only the name changes
- **THEN** tokens and exclusions remain.

### Requirement: Wallet summary
A wallet row SHALL show the combined value of its native balance and its known, non-excluded tokens. The native balance SHALL be the first entry of the wallet's token list.

#### Scenario: Wallet with tokens
- **WHEN** a wallet holds 1 ETH and 250 known USDT
- **THEN** the row shows their combined value and the list starts with ETH followed by USDT.
