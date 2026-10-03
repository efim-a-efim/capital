#!/usr/bin/env python3
"""Read-only smoke tests against public well-known addresses; no user data or API keys."""
import json
import urllib.request
import urllib.error
from pathlib import Path

checks = [
    ("Blockstream", "https://blockstream.info/api/address/1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", None),
    ("mempool.space", "https://mempool.space/api/address/1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", None),
    ("PublicNode ETH", "https://ethereum-rpc.publicnode.com", {"jsonrpc":"2.0","id":1,"method":"eth_getBalance","params":["0x0000000000000000000000000000000000000000","latest"]}),
    ("TON Center", "https://toncenter.com/api/v2/getAddressBalance?address=0:" + "0"*64, None),
    ("TonAPI", "https://tonapi.io/v2/accounts/0:" + "0"*64, None),
    ("PublicNode TRX", "https://tron.publicnode.com/walletsolidity/getaccount", {"address":"41"+"0"*40,"visible":False}),
    ("Frankfurter", "https://api.frankfurter.dev/v2/rates?base=USD&quotes=EUR,RSD", None),
    ("ECB", "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml", None),
    ("CoinPaprika TON", "https://api.coinpaprika.com/v1/tickers/toncoin-the-open-network", None),
    ("Blockscout tokens", "https://eth.blockscout.com/api/v2/addresses/0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045/token-balances", None),
    ("Ethplorer tokens", "https://api.ethplorer.io/getAddressInfo/0xdAC17F958D2ee523a2206206994597C13D831ec7?apiKey=freekey", None),
    ("TonAPI jettons", "https://tonapi.io/v2/accounts/EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N/jettons", None),
    ("TON Center jettons", "https://toncenter.com/api/v3/jetton/wallets?owner_address=EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N&exclude_zero_balance=true&limit=5", None),
    ("TronGrid TRC-20", "https://api.trongrid.io/v1/accounts/TLa2f6VPqDgRE67v1736s7bJ8Ray5wYjU7", None),
    ("DefiLlama", "https://coins.llama.fi/prices/current/coingecko:bitcoin,ethereum:0xdac17f958d2ee523a2206206994597c13d831ec7,tron:TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t,ton:EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs", None),
    ("CoinPaprika contract", "https://api.coinpaprika.com/v1/contracts/eth-ethereum/0xdac17f958d2ee523a2206206994597c13d831ec7", None),
    # Brokers need a user token; a bogus token proves the endpoint answers in the documented form.
    ("Interactive Brokers Flex", "https://ndcdyn.interactivebrokers.com/AccountManagement/FlexWebService/SendRequest?t=0&q=0&v=3", None),
    ("OANDA v20", "https://api-fxtrade.oanda.com/v3/accounts", None),
    ("Trading 212", "https://live.trading212.com/api/v0/equity/account/summary", None),
    ("SnapTrade", "https://api.snaptrade.com/api/v1/", None),
    ("Alpaca", "https://api.alpaca.markets/v2/account", None),
    ("Tradier", "https://api.tradier.com/v1/user/profile", None),
    ("tastytrade", "https://api.tastyworks.com/oauth/token", {"grant_type":"refresh_token","refresh_token":"x","client_secret":"y"}),
    ("Public.com", "https://api.public.com/userapiauthservice/personal/access-tokens", {"validityInMinutes":5,"secret":"xxxxxxxx"}),
    ("eToro", "https://public-api.etoro.com/api/v1/balances", None),
    ("Indexa Capital", "https://api.indexacapital.com/users/me", None),
    ("ALOR", "https://oauth.alor.ru/refresh", {"token":"x"}),
    ("Capital.com", "https://api-capital.backend-capital.com/api/v1/session", {"identifier":"a@b.c","password":"x","encryptedPassword":False}),
    ("Akahu", "https://api.akahu.io/v1/accounts", None),
]
# T-Invest (invest-public-api.tbank.ru) is not listed: it is served under the Russian Trusted Root CA, which only the app trusts for that host.
expected_errors={"OANDA v20": 401, "Trading 212": 401, "Alpaca": 401, "Tradier": 401, "tastytrade": 400, "Public.com": 401, "eToro": 401, "Indexa Capital": 401, "ALOR": 403, "Capital.com": 400, "Akahu": 401}
results=[]
for name,url,body in checks:
    req=urllib.request.Request(url,data=json.dumps(body).encode() if body else None,headers={"Content-Type":"application/json","User-Agent":"Java" if "interactivebrokers" in url else "Capital/0.1"})
    try:
        with urllib.request.urlopen(req,timeout=20) as response:
            text=response.read(8*1024*1024).decode()
            payload=text if name in ("ECB","Interactive Brokers Flex") else json.loads(text)
            results.append({"provider":name,"status":response.status,"sample":payload})
            print(name, response.status)
    except urllib.error.HTTPError as exc:
        if exc.code==expected_errors.get(name):
            results.append({"provider":name,"status":exc.code,"expected":True}); print(name,exc.code,"(expected without a token)")
        else:
            results.append({"provider":name,"error":str(exc)}); print(name,str(exc))
    except Exception as exc:
        results.append({"provider":name,"error":str(exc)})
        print(name,str(exc))
Path(".tools/provider-smoke.json").write_text(json.dumps(results,indent=2))
print("Alchemy, TronGrid, CoinGecko Demo and every broker: require user keys or tokens; not live-tested beyond reachability.")
