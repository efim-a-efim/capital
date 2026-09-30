---
layout: default
lang: ja
base: "/ja"
key: "financial-features"
title: 金融機能の申告
class: doc
---
# 金融機能の申告

<p class="meta">Google Play Console のフォーム（Policy and programs（ポリシーとプログラム） → App content（アプリのコンテンツ） → Financial features（金融機能））への回答とその根拠です。2026年9月30日にバージョン2.2.1を対象に確認しました。</p>

## フォームへの回答

**Select all of the financial features the app provides（アプリが提供する金融機能をすべて選択）：** **The app does not provide any financial features（アプリは金融機能を提供していない）.**

## 理由

Capitalは個人向けの貯蓄管理アプリです。ユーザーがすでに保有しているものを記録し、その貯蓄がユーザー自身の目標にどう割り当てられるかを表示します。フォームの各機能との対応は以下のとおりです：

| フォームの機能 | Capital |
|---|---|
| Personal loan direct lender, loan facilitator, payday loans, line of credit, earned wage advances, microfinance, buy now pay later（個人ローンの直接貸付、ローンの仲介、ペイデイローン、与信枠、給与前払い、マイクロファイナンス、後払い決済） | いかなる貸付も行わない |
| Banking（銀行業務） | 口座、預金、口座へのアクセスはない。銀行残高はユーザーが手入力する |
| Mobile payments and digital wallets, money transfer and wire services（モバイル決済とデジタルウォレット、送金・電信送金サービス） | お金の送金、受け取り、保管はできない。目標への配分は画面に表示される計算であり、何も移動しない |
| Cryptocurrency wallet（暗号資産ウォレット） | ユーザーが貼り付けた公開アドレスの残高を読み取る。秘密鍵やシードフレーズを保持することはなく、トランザクションに署名したりブロードキャストしたりできないため、ウォレットではない |
| Cryptocurrency exchange（暗号資産取引所） | 取引、注文の取り次ぎ、法定通貨からの入金手段はない |
| Rewards and incentives, crowdfunding and chit funds, prediction markets（リワードとインセンティブ、クラウドファンディングと頼母子講、予測市場） | なし |
| Credit monitoring and reporting（信用情報の監視と報告） | なし |
| Financial advice（金融アドバイス） | なし。予測はユーザー自身の数値に対する計算結果を示すだけで（「貯蓄予定により … に目標達成の見込み」）、商品、資産、行動を推奨しない。リバランス計算機は、ユーザー自身が設定した割合に合わせるために必要な購入を一覧にするだけ |
| Insurance（保険） | なし |

また、アプリ内課金や有料機能もありません。

## 審査担当者の判断が異なる場合

Play の審査でアプリが金融機能を提供していると分類された場合、最も近い選択肢は **Other（その他）** で、説明は次のとおりです：

> Read-only personal savings tracker. Users type in their balances or paste public blockchain addresses; the app fetches balances and market prices from third-party data sources and shows how the savings cover the user's own goals. No custody, no keys, no transactions, no lending, no trading, no advice.
>
> （読み取り専用の個人向け貯蓄管理アプリ。ユーザーは残高を入力するか、公開ブロックチェーンアドレスを貼り付けます。アプリは第三者のデータソースから残高と市場価格を取得し、貯蓄がユーザー自身の目標をどれだけ賄えるかを表示します。資産の預かり、鍵、トランザクション、貸付、取引、アドバイスはいずれもありません。）

個人ローンアプリに関する国別の要件や、米国の暗号資産に関する質問は、これらの機能をいずれも選択していないため適用されません。

## 審査担当者から質問される可能性のある関連事項

- 市場データは、ユーザーが選択した第三者の事業者から取得します（[プライバシーポリシー]({{ page.base }}/privacy) を参照）。アプリは「設定」に事業者の名前とサイトを表示します。
- ウォレットの照会には、公開の読み取り専用ブロックチェーンAPIを使用します。
- アプリは完全に端末上で動作し、開発者が運営するサーバーはありません。
