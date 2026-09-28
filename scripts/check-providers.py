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
]
results=[]
for name,url,body in checks:
    req=urllib.request.Request(url,data=json.dumps(body).encode() if body else None,headers={"Content-Type":"application/json","User-Agent":"Capital/0.1"})
    try:
        with urllib.request.urlopen(req,timeout=20) as response:
            text=response.read(8*1024*1024).decode()
            payload=text if name=="ECB" else json.loads(text)
            results.append({"provider":name,"status":response.status,"sample":payload})
            print(name, response.status)
    except Exception as exc:
        results.append({"provider":name,"error":str(exc)})
        print(name,str(exc))
Path(".tools/provider-smoke.json").write_text(json.dumps(results,indent=2))
print("Alchemy, TronGrid, CoinGecko Demo: require user keys; not live-tested.")
