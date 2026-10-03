---
layout: default
lang: ja
base: "/ja"
key: "accounts"
title: ブローカー・FX口座
class: doc
---
# ブローカー・FX口座

Capitalは、暗号資産ウォレットと同じように、証券口座やFX口座の評価額の合計を読み取れます。口座を **ブローカー口座** タイプの保有資産としてポケットに追加すると、更新のたびに、その口座の基準通貨での純資産価値（NAV）を取得します。アプリは読み取りしか行いません。ご自身で作成したトークンを使ってブローカーのレポート用インターフェースにアクセスするだけで、注文の発注・変更・取消は一切行わず、お金を動かすこともありません。

Capitalが接続するのは、認証情報の有効期間が長いインターフェースのみです。つまり、一度作成すれば無効化するまで有効なトークンまたはキーです（Interactive Brokersの場合は、選んだ有効期限まで。最長1年）。現在対応しているブローカー：

| ブローカー | 使用するインターフェース | 読み取る内容 |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service（レポートの取得のみ） | 直近の営業日の純資産価値。口座の基準通貨で取得 |
| [OANDA](#oanda) | v20 REST API、fxTradeのライブ口座 | 更新時点の純資産価値。口座の通貨で取得 |
| [Trading 212](#trading-212) | Trading 212 Public API | 口座の合計評価額。口座の主要通貨で取得 |
| [SnapTrade](#snaptrade) | SnapTrade Personal（アグリゲーター） | 接続したブローカー口座の合計評価額 |

## 始める前に {#before-you-start}

- **端末の外に送信されるもの。** 更新のたびに、アプリはアクセストークンと口座ID（またはクエリID）をHTTPSでそのブローカーに送信します。他のあらゆるリクエストと同様に、ブローカーにはあなたのIPアドレスが伝わります。
- **認証情報の保管場所。** 設定 → ブローカー口座。Android Keystoreで保持する鍵で暗号化され、データフォルダには書き込まれず、エクスポートやシステムのバックアップにも含まれません。ブローカーごとに1組の認証情報で、そのブローカーに追加するすべての口座を扱えます。
- **フォルダに保存されるもの。** 口座ID、最後に読み取った評価額、その読み取り日時。ブローカーのそれ以外のデータは保存されません。
- **ブローカーの画面は変更されることがあります。** 以下の手順は、2026年10月時点のブローカーのウェブサイトに基づいています。ブローカーはメニュー名を変更したり設定を移動したりすることがあるため、実際の画面とは少し異なる場合があります。各セクションにリンクしたブローカー自身のドキュメントが正式な情報源です。ここに書かれた手順が合わなくなっていたら、ブローカーのページで同じ用語を探してください。

## Interactive Brokers {#interactive-brokers}

Capitalは、Interactive Brokersが事前設定したレポートを取得するために提供している **Flex Web Service** を使います。このトークンでできるのはレポートの生成とダウンロードだけで、ログイン、取引、出金はできません。Capitalは、Activity Flex Queryの **Net Asset Value (NAV) Summary in Base** を要求し、最新のレポート日の合計を取得します。そのため、評価額は直近の営業日の終値になります。

以下のメニューや項目名は、ブローカーの英語の画面に表示されるものです。そのまま英語で記載しています。

### 1. Flex Queryを作成する

1. [Client Portal](https://www.interactivebrokers.com/portal) にログインし、**Performance & Reports（パフォーマンスとレポート） → Flex Queries** を開きます（口座によってはメニュー名が *Reporting* になっています）。
2. **Activity Flex Query** の下で **+**（作成）を押します。クエリに名前を付けます（例：`Capital`）。
3. **Sections（セクション）** の一覧で、次の2つのセクションとフィールドだけを有効にします（セクションのすべてのフィールドを選んでも構いません）：
   - **Account Information**：*Account ID*、*Currency*。
   - **Net Asset Value (NAV) Summary in Base**：*Report Date*、*Total*。
4. **Delivery Configuration（配信設定）** で、**Format** を `XML`、**Period** を `Last Business Day` に設定します。その他の項目はデフォルトのままで構いません。
5. クエリを保存し、その横の **i**（情報）アイコンを押して、数字の **Query ID** を控えます。

クエリは1つの口座を対象にする必要があります。リンクされた口座やアドバイザー構成をお持ちの場合は、口座ごとにクエリを1つ作成し、作成時にその口座だけを選択してください。

### 2. Flex Web Serviceを有効にしてトークンを作成する

1. 同じ **Flex Queries** ページで **Flex Web Service Configuration** を開きます。
2. **Flex Web Service Status** をオンにして保存します。トークンが作成されます。
3. トークンの有効期間を選ぶには **Generate New Token** を押します。6時間から1年の範囲で選べます。携帯電話はアドレスが変わるため、**Valid for IP address** は空のままにしてください。新しいトークンを生成すると、以前のトークンは無効になります。
4. トークンをコピーします。

### 3. Capitalで接続する

1. **設定 → ブローカー口座 → アクセストークン：Interactive Brokers** で、トークンを貼り付けて保存します。
2. ポケットを開き、**保有資産を追加** で **追跡方法** を **ブローカー口座**、**ブローカー** をInteractive Brokersに設定し、**Flex Query id** を入力して保存します。
3. **更新** を押します。レポートは要求に応じて生成されるため、初回は最大30秒ほどかかります。

トークンの有効期限が切れると、更新時に *トークンの有効期限が切れました。Client Portalで新しいトークンを生成してください* と表示されます。新しいトークンを生成して、設定に貼り付けてください。Interactive Brokersでは、1つのトークンにつきレポートの要求は1秒に1回、1分に10回までですが、1回の更新でこの上限を超えることはありません。

Interactive Brokersのドキュメント：[Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [Enable and create the access token](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [Create a Flex Query](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query reference](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

Capitalは、OANDA v20 REST APIの **アカウントサマリー** を呼び出し、口座のNAV（残高に未実現損益を加えたもの）を口座の通貨で保存します。対応しているのはfxTradeのライブ口座のみです。デモ口座は貯蓄ではありません。

**OANDAのパーソナルアクセストークンは読み取り専用ではありません。** ログインに紐付くすべてのサブ口座に対して、取引を含むAPIへのフルアクセスを与えます。Capitalが呼び出すのはアカウントサマリーだけですが、トークンを入手した人は誰でもそれで取引できてしまいます。パスワードと同じように扱ってください。トークンはCapitalにのみ貼り付け、携帯電話を紛失した場合はOANDAのポータルで無効化してください。

### 1. トークンを作成する

1. OANDAのfxTradeのアカウント管理ポータルにログインします。
2. **My Services → Manage API Access** を開きます（旧ポータルでは *My Account → My Services → Manage API Access*）。
3. APIライセンスに同意して **Generate** を押します。トークンをコピーしてください。OANDAは再表示しません。紛失した場合は、そこで無効化して新しいトークンを生成します。

### 2. 口座IDを確認する

v20の口座IDは、ハイフン付きの `001-001-1234567-001` という形式です。同じポータルの各サブ口座の横と、fxTradeプラットフォームの口座詳細に表示されています。

### 3. Capitalで接続する

1. **設定 → ブローカー口座 → アクセストークン：OANDA** で、トークンを貼り付けて保存します。
2. ポケットを開き、**保有資産を追加** で **追跡方法** を **ブローカー口座**、**ブローカー** をOANDAに設定し、**OANDA口座ID** を入力して保存します。
3. **更新** を押します。

NAVがマイナスになっている証拠金口座は、貯蓄として集計されず、エラーとして報告されます。

OANDAのドキュメント：[v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capitalは、Trading 212 Public APIの **アカウントサマリー** を呼び出し、口座の合計評価額を口座の主要通貨で保存します。このAPIが対象とするのは **Invest** と **Stocks ISA** の口座です。キーのペアは1つの口座に属し、Capitalが保持するキーのペアは1組なので、読み取れるTrading 212の口座は1つです。

### 1. APIキーを作成する

1. Trading 212のアプリまたはウェブサイトで、メニュー（**☰**）→ **Settings** → **API (Beta)** を開き、リスクに関する警告に同意します。
2. **Generate API key** を押します。名前を付け、**Account data** の権限（読み取り）だけを残し、IPアクセスは *Unrestricted* を選びます（携帯電話のアドレスは変わるため）。
3. 送信します。**API Key** と **API Secret Key** の両方をコピーしてください。シークレットは一度しか表示されません。紛失した場合は、キーを削除して新しいペアを生成します。

### 2. Capitalで接続する

1. **設定 → ブローカー口座 → APIキー：Trading 212** と **APIシークレット：Trading 212** に、それぞれの値を貼り付けます。
2. ポケットを開き、**保有資産を追加** で **追跡方法** を **ブローカー口座**、**ブローカー** をTrading 212に設定し、**Trading 212口座番号**（アプリに表示される口座ID。数字のみ）を入力して保存します。
3. **更新** を押します。Trading 212では、サマリーのリクエストは5秒に1回までです。

Trading 212のドキュメント：[Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) はアグリゲーター（利用者に代わって多くのブローカーに接続するサービス）です。ブローカー口座をSnapTradeに一度接続すれば、あとはSnapTradeが読み取ってくれます。独自の公開APIを持たない多くのブローカーに対応しています。Capitalが使うのは、ご自身の口座向けの無料プラン **SnapTrade Personal** で、ご自身のクライアントIDとコンシューマーキーを使います。このプランのデータは、SnapTradeによって1日に1回ほど更新されます。

送信されるもの：クライアントID、そして署名として、コンシューマーキーそのものは送信されません（リクエストはそのキーで署名されます）。ブローカーとの接続を保持するのはCapitalではなくSnapTradeであり、その接続にはSnapTradeの利用規約とプライバシーポリシーが適用されます。

### 1. APIキーを作成する

1. [SnapTradeダッシュボード](https://dashboard.snaptrade.com/signup)でサインアップし、**Personal** プランを選びます。
2. ダッシュボードでAPIキーを作成します。**クライアントID** と **コンシューマーキー** をコピーしてください。コンシューマーキーは一度しか表示されません。

### 2. Capitalで接続する

1. **設定 → ブローカー口座 → クライアントID：SnapTrade** と **コンシューマーキー：SnapTrade** に、それぞれの値を貼り付けます。
2. ポケットを開き、**保有資産を追加** で **追跡方法** を **ブローカー口座**、**ブローカー** をSnapTradeに設定します。
3. **SnapTradeでブローカーを接続** を押します。SnapTradeのConnection Portalがブラウザで開くので、そこでブローカーにログインします（リンクの有効期間は5分です）。その後Capitalに戻ります。
4. **口座を取得** を押して口座を選びます。その口座のIDが **SnapTrade口座ID** 欄に入力されます。保存してから **更新** を押します。

SnapTradeがまだ同期を終えていない口座では *SnapTradeにはこの口座の合計評価額がまだありません* と表示されます。しばらくしてからもう一度更新してください。

SnapTradeのドキュメント：[Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## その他のブローカー {#other-brokers}

Capitalが接続するのは、携帯電話からHTTPSで動作し、ご自身で作成できるトークンを使い、取引はできず読み取りだけができるインターフェースに限られます。そのため、現時点では次のものは対象外です：

- **MetaTrader 4と5** の口座。投資家パスワードで読み取り専用のアクセスはできますが、それはMetaTraderターミナルの中だけです。ブローカーはこれ用のHTTPSインターフェースを公開していません。
- APIの利用にパソコン上で動くプログラム（たとえばInteractive BrokersのClient Portal Web APIゲートウェイ。Capitalは代わりにFlex Web Serviceを使います）やOAuthアプリケーションの登録が必要なブローカー。
- OAuth経由で有効期間の短いトークンしか発行しないブローカー。たとえばSaxo Bank（アクセストークンの有効期間は20分で、開発者ポータルの24時間トークンが使えるのはシミュレーション環境のみ）。
- 公開APIのない銀行やブローカー。

これらの多くのブローカーは[SnapTrade](#snaptrade)で対応できます。それ以外の場合は、残高を **手動**  の保有資産として入力し、明細を確認したときに数値を更新してください。口座の評価額を読み取れる、トークン方式のシンプルなHTTPSエンドポイントをお使いのブローカーが提供している場合は、そのドキュメントへのリンクを添えて[issueを作成]({{ site.repo }}/issues)してください。

## メッセージと対処方法 {#messages}

| メッセージ | 対処方法 |
|---|---|
| *Interactive Brokers には、設定 → ブローカー口座でアクセストークンの入力が必要です* / *OANDA には、設定 → ブローカー口座でアクセストークンの入力が必要です* | 設定 → ブローカー口座で、トークン、キー、またはクライアントIDを貼り付けます。 |
| *トークンの有効期限が切れました。Client Portalで新しいトークンを生成してください* | Flex Web Serviceの新しいトークンを生成して貼り付けます。 |
| *トークンが無効です* | トークンをもう一度コピーしてください。新しいトークンを生成すると古いトークンは置き換えられます。 |
| *このトークンは別のIPアドレスに制限されています* | IPアドレスの制限なしでトークンを生成します。 |
| *Flex Query idが無効です* | 番号を確認してください。クエリは、このログインのActivity Flex Queryである必要があります。 |
| *Flex Queryに、Report DateとTotalを含むNet Asset Value (NAV) Summary in Baseセクションを追加してください* | クエリを編集して、セクションとフィールドを追加します。 |
| *Flex Queryに、Account InformationのCurrencyフィールドを追加してください* | クエリを編集して、フィールドを追加します。 |
| *クエリが N 件の口座を返しました。口座ごとにFlex Queryを1つ作成してください* | 1つの口座を対象にしたクエリを作成します。 |
| *レポートはまだ準備中です。1分ほど待ってからもう一度更新してください* | Interactive Brokersがまだレポートを生成中です。もう一度更新してください。 |
| *アクセスが拒否されました。プロバイダのキーまたは利用上限を確認してください* | OANDAのトークンが間違っているか、無効化されています。あるいは、Trading 212のキーのペアが間違っているか、Account dataの権限がありません。 |
| *SnapTradeにはこの口座の合計評価額がまだありません。接続を同期してから再試行してください* | SnapTradeがまだブローカーを同期していません。しばらくしてからもう一度更新してください。 |
| *接続された口座はまだありません。先にSnapTradeでブローカーを接続してください。* | エディタからConnection Portalを開き、ブローカーを接続します。 |
| *マイナスの口座評価額 … には対応していません* | 口座が借越し状態です。貯蓄には加算されません。 |

これらのいずれの場合も、前回の値は古い値として表示されたまま残ります。
