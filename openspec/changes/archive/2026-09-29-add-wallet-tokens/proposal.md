## Why

Wallet holdings track native coins only, so stablecoins and other tokens on tracked addresses are invisible unless re-entered manually. Anyone can mint a token named "USDT", so discovery must never trust names.

## What Changes

- Discover ERC-20 (ETH), jetton (TON) and TRC-20 (TRX) balances for every tracked wallet address on refresh. BTC has no tokens in scope.
- Identify each token by chain plus canonical smart-contract address. Symbol and name are display text only.
- Price tokens from the selected crypto price provider by contract address. A token the provider does not list for that contract is shown as "Unknown token" and excluded from every total, allocation and completeness check.
- New selectable token sources per chain, each with an "Off" choice.
- Snapshot schema 2: holdings carry a token list; schema 1 folders open and upgrade on next save.
- Supersedes the earlier non-goal "automatic token discovery". Staking, DeFi positions, NFTs and BTC-based tokens stay out of scope.

## Capabilities

### New Capabilities
- `wallet-tokens`: token discovery on tracked wallets, contract-address identity, known/unknown classification, valuation rules and display.

### Modified Capabilities

None. `openspec/specs/` is empty; the first change is not archived yet.

## Impact

`domain/Model.kt` (Token, valuation, validation), `domain/Addresses.kt` (provider address encoding), `data/Providers.kt` (token balances, contract quotes), `data/Snapshots.kt` (schema 2), `ui/CapitalApp.kt` (token rows, settings), tests, README. New operators receive wallet addresses when selected: Blockscout or Ethplorer for ETH tokens. No new dependencies.
