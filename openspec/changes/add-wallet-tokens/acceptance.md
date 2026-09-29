# Acceptance: wallet tokens (emulator-5554)

| # | Result | Observation | Shot |
|---|--------|-------------|------|
| T1 | PASS | Schema-1 folder opens, Overview shows ReserveX1 total $4,772,556.64, goals Emergency ($600/$600 Funded), Trip (EUR 500/500 Funded). No lockout. Cold refresh already finished ("Refresh" shown; notice "Refreshed. Shared rates may revalue other buckets."). | T1.png |
| T2 | PASS | Selectors: ETH tokens=Blockscout (also Ethplorer, Off), TON tokens=TON Center, TRX tokens=TronGrid, Crypto=CoinPaprika. TRX native set TronGrid -> PublicNode (TronGrid showed "Required free key"). Disclosure: "Token sources and price providers also receive wallet or token contract addresses; choose Off to stop token lookups for a chain." | T2.png, T2b.png |
| T3 | PASS | EthTokens (ETH), TonTokens (TON), TrxTokens (TRX) added to ReserveX1, saved ("Saved on device"). | T3a.png |
| T4 | PASS | Refresh finished in ~90 s. Notice: "Refreshed. Shared rates may revalue other buckets." (no errors). Bucket total -> $2,190,132,352.74. | T4.png |
| T5 | PASS (see notes) | See table below. Unknown rows: title "Unknown token", subtitle "Not counted · reported as "SYM" · 0xabc…def · qty". None show money. | T5_1..T5_8.png |
| T6 | PASS | schema=2. Expected bucket USD = 2,190,132,352.735950 == UI $2,190,132,352.74. Tokens part 2,182,939,545.99. 14 quotes with ':' (all ETH:). Tokens w/o quote add nothing. | tokens-snapshot.json |
| T7 | PASS | 2 look-alikes, both "USDT" on ETH: 0xd7cb56a28c5f6241c1f9091388d242ffc64fd8dd and 0x28a016d92fedcffebf843125ebcc4e2f166d3847. Neither has a quote; both shown as Unknown token. Canonical USDT and USDC (0xa0b8...) are quoted. | |
| T8 | PASS (after env glitch, see Bugs) | ETH tokens=Off + Refresh: EthTokens tokens=0, total $7,191,307.35 (expected 7,191,307.349 from snapshot). Drop 2,182,941,045 vs known-token value 2,182,939,546; residual ~1.5k = BTC price move on Genesis (57.48 BTC). Back to Blockscout + refresh: 100 tokens back, total $2,189,977,270.13 == snapshot-computed 2,189,977,270.126. | T8e.png, T8f.png |
| T9 | PASS with note | checkedAt set/null per wallet, see table. | tokens-snapshot-final.json |

## T5 per wallet
| Wallet | Native | Known tokens (value USD) | Unknown rows | "Show N more" |
|---|---|---|---|---|
| EthTokens | ETH 0.23 ($614.51) | USDT 1,690,708,240.26; XAUT 489,711,129.86; EURT 2,520,174.30; INUINU 0.79; SAITO 0.38; POL 0.23; MTV 0.10; ZIK 0.06; USDC 0.01 (0.007171); TSUKA 0.00; HEX, XAU, XEN, TUSD (~0.00) = 14 known | 5 shown + 81 hidden = 86 | "Show 81 more unknown tokens" |
| TonTokens | TON 1,592,540.63 ($2,410,283.95) | none (after T4) | 5 shown + 95 hidden = 100 | "Show 95 more unknown tokens" |
| TrxTokens | TRX 14,488.84 ($4,850.69) | none | 5 shown + 72 hidden = 77 | "Show 72 more unknown tokens" |

Wallet row value = native only; token values appear in the token rows and in bucket total.
Some tokens show "raw units N" instead of quantity (TON/TRX with null decimals).

## T9 rotation (checkedAt set / null)
| Wallet | T6 (after 1st refresh) | Final (after Off->Blockscout + refresh) |
|---|---|---|
| EthTokens | 29 / 71 | 14 / 86 |
| TonTokens | 0 / 100 | 29 / 71 |
| TrxTokens | 0 / 77 | 0 / 77 |
Total checked 29 -> 43 in the whole run, but per-wallet ETH decreased because Off wiped tokens (checkedAt reset) and the ~30 lookup budget then went to TON.
Also: the 1st refresh spent the whole budget on ETH; TON/TRX got nothing. TRX tokens never checked: all 77 have decimals=null (TronGrid tokens carry no decimals in stored data, symbol/name empty) so they can never become known.

## Bugs / observations
1. (Env, but data-loss-adjacent) After settings visit at ~19:12 the emulator ExternalStorageProvider threw `NoSuchFileException: /storage/emulated` in `isRestrictedPath` and logged `Queried directory "primary:Documents/CapitalTest-copy" is hidden`, so SAF listing returned EMPTY. App effects: (a) refresh save -> "Folder changed. Reload before saving; your edits are retained." (Retry save failed identically); (b) "Discard and reload" -> app showed empty portfolio, EUR default; (c) on next cold start app silently created NEW ROOT snapshots (parents=[], empty portfolio): capital-943c1460..., capital-ebbd4d18... Fix of env: `adb shell am force-stop com.android.externalstorage`, restart app -> app showed "Sync conflict" with revision 6352b956 (full data) vs empty roots; I chose "Keep this version" (6352b956). No data lost, originals kept. Robustness suggestion: an empty folder listing when the app previously had data should not auto-create a new root.
2. TRX tokens never valued: decimals null for all 77 TrxTokens tokens, symbol/name empty ("reported as unnamed"). Verify TronGrid token decimals parsing (may be legit for this address; TronGrid list normally has token_id + balance only).
3. Lookup budget starvation: chains are not rotated fairly; ETH consumed all 29-30 lookups on first refresh, TON/TRX got 0.
4. Settings ETH tokens=Off did not persist across the glitch (unrelated to feature).


---

# DefiLlama run

# Acceptance: DefiLlama provider (emulator-5554)

Folder: primary:Documents/CapitalTest-copy/CapitalTest-1790703501105/CapitalTest-1790703576987 (schema 3, bucket Reserve with Cash USD 900; Crypto was CoinGecko before).

| # | Result | Observation |
|---|--------|-------------|
| L1 | PASS | Crypto menu: DefiLlama, CoinGecko, CoinPaprika. Set DefiLlama. TRX was TronGrid -> PublicNode. ETH tokens=Blockscout, TON tokens=TON Center, TRX tokens=TronGrid. Attribution list has DefiLlama (plus Alchemy, TON Center, TonAPI, TronGrid, Blockscout, Ethplorer, CoinGecko, CoinPaprika, Frankfurter, ECB). Shots L1-*.png |
| L2 | PASS | Wallets did not exist; EthTokens/TonTokens/TrxTokens added to Reserve, all saved. |
| L3 | PASS | Refresh ~63 s (first poll at 10 s granularity). Notice: "Refreshed. Shared rates may revalue other buckets." No errors. L3-done.png |
| L4 | PASS | schema 3, settings.providers.Crypto=DefiLlama. All tokens checked after ONE refresh (100/100, 100/100, 77/77). 42 quotes (39 token ids, 3 native), all source DefiLlama. USDT 0xdac17f... known on ETH. See table. |
| L5 | PASS | No look-alike has a quote. Two ETH "USDT" look-alikes (0xd7cb56a2..., 0x28a016d9...) unquoted. (My script also flagged TRX USDT 41a614f8... but that is the canonical contract, quoted = correct.) TON USDT (symbol "USD₮", canonical 0:b113a994...) known, correct. |
| L6 | PASS | Known rows show symbol + $ value ("Token · qty SYM · addr"); unknown rows "Unknown token" + "Not counted · reported as "X" · addr · qty" (some "raw units N"); "Show N more unknown tokens" collapses extras. Bucket total UI $2,184,006,189.37 == snapshot expected 2,184,006,189.37 (Cash 900 + wallets). Wallet row shows native only (ETH $614.69, TON $2,376,949.18, TRX $4,852.51). Shots L6-*.png |
| L7 | PASS | Rates list shows ETH/TON/TRX only, no "ETH:0x…" ids; note "39 token prices by contract address are shown with their wallets." (equals 39 token quotes in snapshot). L7-rates.png |
| L8 | PASS | No FATAL. grep AndroidRuntime hits only are uiautomator dump processes ("START ... uiautomator.Launcher", "Shutting down VM"), not app crashes. |

## L4 per wallet
| Wallet | Native qty | tokens | checkedAt set | known | tokensError | expected USD |
|---|---|---|---|---|---|---|
| EthTokens (ETH) | 0.229425 (x $2679.25) | 100 | 100 | 15 | none | 2,181,561,444.54 |
| TonTokens (TON) | 1,592,540.632746 (x $1.49255) | 100 | 100 | 16 | none | 2,377,010.09 |
| TrxTokens (TRX) | 14,488.835459 (x $0.33491) | 77 | 77 | 8 | none | 66,834.74 |

Decimals set: ETH 100/100, TON 84/100, TRX 8/77 (=known ones). Symbol set: ETH 100, TON 99, TRX 8.

Known tokens (symbol, decimals, USD value):
- ETH: USDT 6 1,690,539,562.80 -> $1,690,086,881.04 (q 0.99973); XAUT 6 118,093.97 -> $491,473,927.92 (q 4161.72); SOLI $19.29; INUINU $0.79; SAITO $0.38; POL $0.23; MTV $0.10; ZIK $0.057; XEN $0.02; USDC 0.007171 $0.0072; TSUKA, HEX, XAU, TUSD ~0; LIME $0.009. Contracts in llama-snapshot.json.
- TON: MMM, GRM $0.46, BOLT $0.02, USD₮ 2.04501 $2.04, ATF, durev, TONNEL, ART, @BTC25 $36.36, MAJOR, DOGS $0.30, jUSDT $1.22, NOT $20.43, GEMSTON, KINGY $0.06, RAFF $0.007.
- TRX (decimals+symbol filled): WIN 1,163,090,535.92 $54,917.23; USDT 6,734.53 $6,732.73 (contract 41a614f8...); JST $296.91; WTRX $33.49; USDD (41e91a74...) 1 $1.00; USDD (4194f24e...) ~0; NFT $0.88; BTT ~0.

## Bugs / UX notes
1. TRX/TON tokens have symbols/decimals filled from chain now (unlike earlier TronGrid-only run), good. 69 TRX tokens and 84 TON tokens unknown (not listed by DefiLlama) -> Not counted, as expected.
2. TRX shows two "USDD" tokens both valued (old/new USDD contracts, 41e91a74... and 4194f24e...). Both are DefiLlama-listed; not a scam per canonical list, but same symbol twice.
3. TON: "@BTC25" token valued at $36.36 (200,010,000,000 units x $1.8e-10) counted in total: DefiLlama lists it; a low-liquidity/spam-like token contributes value. Minor.
4. Bucket detail: wallet row value is native only while bucket total includes token values, so displayed rows do not sum to the bucket total (also true in earlier run).
5. Snapshot (schema 3) fiat quotes absent; no issue.
6. Sluggish: none observed. Add-wallet UI worked; the top notice banner shifts layout (dismiss it before tapping).


---

# Exclusion, wallet summary, picker, unavailable values

# Acceptance: token exclusion, fetch, picker, unavailable (emulator-5554)

| # | Result | Observation | Shot |
|---|--------|-------------|------|
| X1 | PASS | EthTokens row $2,181,881,119.15 = ETH 616.92 + tokens (USDT 1,690,085,762.41; XAUT 491,794,718.93; SOLI 19.29; INUINU .79; SAITO .38; POL .23; MTV .10; ZIK .06; XEN .02; LIME .01; USDC .01; rest 0.00) = ...119.14 (cent rounding). Subtitle "Read-only · 0x5754284f34…", no qty. First entry native "ETH" "Native · ETH0.23"; known tokens by value desc; then "Unknown token" rows (5 shown + "Show 80 more unknown tokens"). TonTokens $2,394,334.06 = TON/GRAM 2,394,272.93 + 16 known (36.36, 20.64, 2.04, 1.22, .46, .30, .06, .02, .01, 0...) = ..334.04. TrxTokens $66,748.65 = TRX 4,853.73 + WIN 54,829.73 + USDT 6,732.72 + JST 297.09 + WTRX 33.49 + USDD 1.00 + NFT .87 (+2 ~0) = ..748.63. Cash: no native row. Sum 900 + 2,181,881,119.15 + 2,394,334.06 + 66,748.65 = 2,184,343,101.86 vs headline 2,184,343,101.85 (1 cent). | X1-eth.png, X1-header.png |
| X2 | PASS | Editor: Tokens section, hint text, "Fetch tokens" button, 100 rows = 100 switches (15 known by symbol + 85 "Unknown token"; known/unknown interleaved, NOT sorted like bucket list), all ON. Total stored 100 = cap. | X2-a.png, X2-b.png |
| X3 | PASS | Switched SOLI ($19.29, 3rd largest, contract 0x0eaddc…969c6e) OFF, Save. Before: wallet 2,181,881,119.15, headline 2,184,343,101.85. After: wallet 2,181,881,099.85 (-19.30), headline 2,184,343,082.56 (-19.29). SOLI no longer listed. Note "1 tokens excluded in the holding editor" shown at bottom of the EthTokens block (above Edit / move). "Saved on device" pop-up. | X3-off.png, X3-after.png, X3-note.png |
| X4 | PASS | Refresh done (<30 s). SOLI still absent, note still shown. Newest snapshot (…/CapitalTest-copy/CapitalTest-1790703501105/CapitalTest-1790703576987/capital-83397033…): excluded=[0x0eaddcbe240d7eeb1ba21f1eed48e58293969c6e], tokens=100 (SOLI still in tokens), no quote ETH:0x0eaddc… (quotes ETH:* went 15 -> 14). Headline changed by price refresh only. | X4-after.png |
| X5 | PASS | Editor: SOLI switch OFF, row shows "Unknown token … reported as “SOLI”" (title lost because its quote was dropped). Switched ON, Save, Refresh: SOLI listed again ($19.29), note gone, snapshot capital-47ccaae3…: excluded=[], quote ETH:0x0eaddc… present. | X5-off.png |
| X6 | PASS | Renamed TonTokens -> TonTokens2 without refresh: native TON/GRAM + 16 known + "Show 79 more unknown tokens" unchanged (same as before). Renamed back to TonTokens (editor showed TonTokens before Save). | X6-editor.png, X6-after.png |
| X7 | PASS | "0x123" + Fetch tokens: message "Enter a mainnet Ethereum address", no "Fetching tokens…" shown. Address 0x28C6…1d60: "Fetching tokens…" then list in about 9 s (incl. polling overhead): 100 rows / 100 switches (cap), all ON, no crash. Cancel -> "Discard edits?" dialog -> Discard; bucket still "4 holdings". | X7-invalid.png, X7-fetched.png |
| X8 | PASS | Alpha -> Connect bucket: selector shows "Reserve" (no id suffix); dropdown options: "Reserve" only (single bucket). Cancelled. Holding editor (Cash) Bucket selector: "Reserve", dropdown "Reserve". | X8-picker.png, X8-holding.png |
| X9 | PASS (see note) | After Save with ISK, Overview: "TOTAL VALUED SAVINGS · ISK  Unavailable · ISK", Allocated "Unavailable · ISK", Unallocated "Unavailable · ISK". Refresh: total became ISK263,343,501,421 (allocated ISK277,173). Currency set back to USD ($2,185,241,900.43). NOTE: no persistent incomplete-valuation notice found on Overview (full scroll; only "Unavailable" texts, a goals list still in $). If a notice was expected it may have been a transient pop-up not caught. | X9-isk.png, X9-after.png |
| X10 | PASS | logcat FATAL/Exception grep empty. | - |

## Numbers
X1: ETH row 2,181,881,119.15; TON 2,394,334.06; TRX 66,748.65; Cash 900.00; bucket 2,184,343,101.85.
X3: SOLI 19.29; wallet -19.30; bucket -19.29.

## Bugs / UX notes
1. Wallet "Edit / move"/"Delete" sit at the END of the holding block, after up to 100 token rows + "Show N more" (long scroll to reach); no buttons near the wallet header.
2. Native subtitle "Native · ETH0.23" / "Native · TON1,592,540.63": no space/separator between asset and quantity (dump shows concatenated text; check visually X1-eth.png).
3. "1 tokens excluded in the holding editor": grammar ("1 token"), also placed at bottom of the block far from header.
4. After a refresh, an excluded priced token appears in the editor as "Unknown token … reported as “SOLI”" (symbol title lost since quote dropped) — confusing.
5. Editor token order is stored order (unknown interleaved with known), unlike sorted bucket list.
6. Token list capped at 100 silently (no truncation hint) both in Fetch and stored; ETH wallet had 15 known + 85 unknown = exactly 100.
7. Settings editor title reads "Add settings".
8. Overview goals stay in $ while total is ISK/Unavailable (OK by design?); no incomplete-valuation notice seen (X9).
9. Pop-ups ("Saved on device", "Refreshing selected providers…") cover the lowest row region at y~2000-2080 but were dismissable by tap.
