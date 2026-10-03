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

Capitalが接続するのは、認証情報の有効期間が長いインターフェースのみです。つまり、一度作成すれば、無効化するまで、選んだ有効期限まで、または少なくとも数か月間有効なトークンまたはキーです（T-Investのトークンは3か月間使わないと失効し、ALORのトークンは1年で失効します）。以下のブローカーはすべて、アプリの言語設定にかかわらず利用できます。現在対応しているブローカー：

| ブローカー | 使用するインターフェース | 読み取る内容 |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service（レポートの取得のみ） | 直近の営業日の純資産価値。口座の基準通貨で取得 |
| [OANDA](#oanda) | v20 REST API、fxTradeのライブ口座 | 更新時点の純資産価値。口座の通貨で取得 |
| [Trading 212](#trading-212) | Trading 212 Public API | 口座の合計評価額。口座の主要通貨で取得 |
| [SnapTrade](#snaptrade) | SnapTrade Personal（アグリゲーター） | 接続したブローカー口座の合計評価額 |
| [Alpaca](#alpaca) | Trading API、ライブ口座 | 純資産（現金とポジションの合計）。米ドルで取得 |
| [Tradier](#tradier) | Brokerage API | 総資産。米ドルで取得 |
| [tastytrade](#tastytrade) | 個人用OAuthグラントを使うOpen API | 純清算価値。米ドルで取得 |
| [Public.com](#public) | Individual API | 口座の合計評価額。米ドルで取得 |
| [eToro](#etoro) | Public API | 選んだ口座の残高（取引口座の場合は現金と投資中のポジションの合計）。その口座の通貨で取得 |
| [Indexa Capital](#indexa-capital) | REST API、読み取り専用トークン | 直近の評価日時点のポートフォリオ合計。口座の通貨で取得 |
| [T-Invest](#t-invest) | T-Invest API（T-Bank） | ポートフォリオの合計評価額。ルーブルで取得 |
| [ALOR](#alor) | ALOR OpenAPI | モスクワ取引所でのポートフォリオ評価額。ルーブルで取得 |
| [Capital.com](#capital-com) | Public API、ライブ口座 | 建玉の損益を含む残高。口座の通貨で取得 |
| [Akahu](#akahu) | Akahuの個人用アプリ（ニュージーランドのアグリゲーター） | 接続した口座（Sharesies、Hatch、Kernel、KiwiSaverなど）の残高。その通貨で取得 |

## 始める前に {#before-you-start}

- **端末の外に送信されるもの。** 更新のたびに、アプリはアクセストークンと口座ID（またはクエリID）をHTTPSでそのブローカーに送信します。他のあらゆるリクエストと同様に、ブローカーにはあなたのIPアドレスが伝わります。
- **認証情報の保管場所。** ブローカータブ → 認証情報。Android Keystoreで保持する鍵で暗号化され、データフォルダには書き込まれず、エクスポートやシステムのバックアップにも含まれません。ブローカーごとに1組の認証情報で、そのブローカーに追加するすべての口座を扱えます。
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

1. **ブローカー → 認証情報 → アクセストークン：Interactive Brokers** で、トークンを貼り付けて保存します。
2. **ブローカー → +**：名前を入力し、**ブローカー** をInteractive Brokersに設定して、**Flex Query id** を入力し、保存します。
3. ポケットを開き、**保有資産を追加** で **追跡方法** を **ブローカー口座** に設定し、口座を選んで保存します。
4. **更新** を押します。レポートは要求に応じて生成されるため、初回は最大30秒ほどかかります。

トークンの有効期限が切れると、更新時に *トークンの有効期限が切れました。Client Portalで新しいトークンを生成してください* と表示されます。新しいトークンを生成して、認証情報に貼り付けてください。Interactive Brokersでは、1つのトークンにつきレポートの要求は1秒に1回、1分に10回までですが、1回の更新でこの上限を超えることはありません。

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

1. **ブローカー → 認証情報 → アクセストークン：OANDA** で、トークンを貼り付けて保存します。
2. **ブローカー → +**：名前を入力し、**ブローカー** をOANDAに設定して、**OANDA口座ID** を入力し、保存します。
3. ポケットを開き、**保有資産を追加** で **追跡方法** を **ブローカー口座** に設定し、口座を選んで保存します。
4. **更新** を押します。

NAVがマイナスになっている証拠金口座は、貯蓄として集計されず、エラーとして報告されます。

OANDAのドキュメント：[v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capitalは、Trading 212 Public APIの **アカウントサマリー** を呼び出し、口座の合計評価額を口座の主要通貨で保存します。このAPIが対象とするのは **Invest** と **Stocks ISA** の口座です。キーのペアは1つの口座に属し、Capitalが保持するキーのペアは1組なので、読み取れるTrading 212の口座は1つです。

### 1. APIキーを作成する

1. Trading 212のアプリまたはウェブサイトで、メニュー（**☰**）→ **Settings** → **API (Beta)** を開き、リスクに関する警告に同意します。
2. **Generate API key** を押します。名前を付け、**Account data** の権限（読み取り）だけを残し、IPアクセスは *Unrestricted* を選びます（携帯電話のアドレスは変わるため）。
3. 送信します。**API Key** と **API Secret Key** の両方をコピーしてください。シークレットは一度しか表示されません。紛失した場合は、キーを削除して新しいペアを生成します。

### 2. Capitalで接続する

1. **ブローカー → 認証情報 → APIキー：Trading 212** と **APIシークレット：Trading 212** に、それぞれの値を貼り付けます。
2. **ブローカー → +**：名前を入力し、**ブローカー** をTrading 212に設定して、**Trading 212口座番号**（アプリに表示される口座ID。数字のみ）を入力し、保存します。
3. ポケットを開き、**保有資産を追加** で **追跡方法** を **ブローカー口座** に設定し、口座を選んで保存します。
4. **更新** を押します。Trading 212では、サマリーのリクエストは5秒に1回までです。

Trading 212のドキュメント：[Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) はアグリゲーター（利用者に代わって多くのブローカーに接続するサービス）です。ブローカー口座をSnapTradeに一度接続すれば、あとはSnapTradeが読み取ってくれます。独自の公開APIを持たない多くのブローカーに対応しています。Capitalが使うのは、ご自身の口座向けの無料プラン **SnapTrade Personal** で、ご自身のクライアントIDとコンシューマーキーを使います。このプランのデータは、SnapTradeによって1日に1回ほど更新されます。

送信されるもの：クライアントID、そして署名として、コンシューマーキーそのものは送信されません（リクエストはそのキーで署名されます）。ブローカーとの接続を保持するのはCapitalではなくSnapTradeであり、その接続にはSnapTradeの利用規約とプライバシーポリシーが適用されます。

### 1. APIキーを作成する

1. [SnapTradeダッシュボード](https://dashboard.snaptrade.com/signup)でサインアップし、**Personal** プランを選びます。
2. ダッシュボードでAPIキーを作成します。**クライアントID** と **コンシューマーキー** をコピーしてください。コンシューマーキーは一度しか表示されません。

### 2. Capitalで接続する

1. **ブローカー → 認証情報 → クライアントID：SnapTrade** と **コンシューマーキー：SnapTrade** に、それぞれの値を貼り付けます。
2. **ブローカー → +**：名前を入力し、**ブローカー** をSnapTradeに設定します。
3. **SnapTradeでブローカーを接続** を押します。SnapTradeのConnection Portalがブラウザで開くので、そこでブローカーにログインします（リンクの有効期間は5分です）。その後Capitalに戻ります。
4. **口座を取得** を押して口座を選びます。その口座のIDが **SnapTrade口座ID** 欄に入力されます。保存します。
5. ポケットを開き、**保有資産を追加** で **追跡方法** を **ブローカー口座** に設定し、口座を選んで保存してから、**更新** を押します。

SnapTradeがまだ同期を終えていない口座では *SnapTradeにはこの口座の合計評価額がまだありません* と表示されます。しばらくしてからもう一度更新してください。

SnapTradeのドキュメント：[Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Capitalで口座を接続する {#connect}

以下の各セクションでは、各ブローカーで認証情報を作成する方法を説明します。Capitalでの手順は、どのブローカーでも同じです。

1. **ブローカー → 認証情報**：そのブローカーの認証情報のボタンを押し、それぞれの値を貼り付けます。
2. **ブローカー → +**：名前を入力し、**ブローカー** を選んで、**口座を取得** を押し、口座を選びます（IDを直接入力しても構いません）。保存します。
3. ポケットを開き、**保有資産を追加** で **追跡方法** を **ブローカー口座** に設定し、口座を選んで保存します。**更新** を押します。

## Alpaca {#alpaca}

Alpacaは口座ごとにキーIDとシークレットを発行します。再生成するまで有効です。読み取れるのはライブ口座のみで、ペーパー口座のキーはライブAPIでは使えません。

1. [Alpacaのダッシュボード](https://app.alpaca.markets)にログインしてライブ口座に切り替え、ホームページの **API Keys** の下で **Generate New Keys** を押します。
2. **API Key ID** と **Secret Key** をコピーします。シークレットは一度しか表示されません。
3. Capitalで **APIキー：Alpaca** と **APIシークレット：Alpaca** に貼り付け、[Capitalで口座を接続する](#connect)の手順に進みます。**口座を取得** には、そのキーの口座番号が表示されます。

Alpacaのドキュメント：[Authentication](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

Tradierの設定から取得したAPIトークンに有効期限はありません。

1. Tradierにログインし、[Settings → API Access](https://web.tradier.com/user/api) を開きます。証券口座の **API Access Token**（サンドボックスのトークンではありません）をコピーします。
2. Capitalで **アクセストークン：Tradier** に貼り付け、[Capitalで口座を接続する](#connect)の手順に進みます。

Tradierのドキュメント：[Authentication](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

tastytradeでは個人用のOAuthグラントを使います。ご自身用のアプリケーションと、有効期限のないリフレッシュトークンを持つグラントを作成します。Capitalは更新のたびに、それを15分間有効なアクセストークンに交換します。

1. [my.tastytrade.com](https://my.tastytrade.com) で **Manage → My Profile → API → OAuth Applications** を開き、**+ New OAuth client** を押します。名前、任意のHTTPSリダイレクトURI (例：`https://capital.fimych.dev`) と **read** スコープのみを指定します。保存して **Client Secret** をコピーします。シークレットは一度しか表示されません。
2. アプリケーションの横の **Manage** を押し、続けて **Create Grant** を押して、**リフレッシュトークン** をコピーします。
3. Capitalで **リフレッシュトークン：tastytrade** と **クライアントシークレット：tastytrade** に貼り付け、[Capitalで口座を接続する](#connect)の手順に進みます。

tastytradeのドキュメント：[OAuth2 and personal grants](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

PublicのIndividual APIは、ご自身の口座向けです。シークレットキーは有効期間が長く、無効化もできます。Capitalは更新のたびに、それを5分間有効なアクセストークンに交換します。

1. Publicのウェブアプリで設定の **API** ページを開き、**シークレットキー** を生成します。
2. Capitalで **シークレットキー：Public.com** に貼り付け、[Capitalで口座を接続する](#connect)の手順に進みます。

Publicのドキュメント：[Quickstart](https://public.com/api/docs/quickstart) · [Access tokens](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [Portfolio](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

eToroのキーは有効期間が長く、有効期限とIPアドレスのリストを設定でき、読み取り専用にもできます。eToroの口座は本人確認済みである必要があります。

1. eToroで **Settings → Trading → API Key Management** を開き、**Create New Key** を押します。環境は **Real**、権限は **Read**、IPリストなし、有効期限は任意で指定します。SMSコードで確定します。
2. **Public API Key** と **User Key** をコピーします。ユーザーキーは一度しか表示されません。
3. Capitalで **公開APIキー：eToro** と **ユーザーキー：eToro** に貼り付け、[Capitalで口座を接続する](#connect)の手順に進みます。**口座を取得** には、eToroの取引口座、現金口座などが一覧表示されます。

eToroのドキュメント：[Authentication](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [Getting started](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

Indexaのプライベートエリアで取得するトークンは読み取り専用です。メールアドレス、パスワード、端末に紐づいており、パスワードを変更したら再度生成してください。

1. Indexaのプライベートエリアで **ユーザー設定 → アプリケーション** を開き、トークンをコピーします。
2. Capitalで **アクセストークン：Indexa Capital** に貼り付け、[Capitalで口座を接続する](#connect)の手順に進みます。年金口座と投資口座の両方が一覧表示されます。

Indexaはファンドを営業日ごとに1回評価します。観測日はその評価日です。

Indexa Capitalのドキュメント：[REST API](https://indexacapital.com/en/api-rest-v1) · [Connecting with the API](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

T-BankのT-Invest APIでは、投資設定で発行したトークンを使います。トークンは最後に使ってから3か月で失効し、発行から7日以内に使う必要があります。週に1回更新すれば有効なまま保てます。**読み取り専用** のトークンを選んでください。

1. [T-Investの設定](https://www.tbank.ru/invest/settings/)を開き、取引所向けの **T-Invest APIトークン** を **読み取り専用** のアクセス（全口座または1口座）で発行します。発行するには、コードによる取引の確認をオフにしておく必要があります。トークンをコピーします。一度しか表示されません。
2. Capitalで **アクセストークン：T-Invest** に貼り付け、[Capitalで口座を接続する](#connect)の手順に進みます。

T-BankはこのAPIを、Androidに含まれていないロシアのTrusted Root CAのもとで提供しています。Capitalがこの証明書を信頼するのはT-Invest APIのアドレス（`invest-public-api.tbank.ru`）のみで、他の接続には適用しません。

T-Investのドキュメント：[Tokens](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

ALORは有効期間1年のリフレッシュトークンを発行します。Capitalは更新のたびに、それを30分間有効なアクセストークンに交換します。ALORには読み取り専用のトークンがありません。このトークンは取引もできますが、Capitalは読み取りしか行いません。

1. [ALORの開発者ポータル](https://alor.dev)にサインインし、取引口座を紐づけて **API Access Tokens** を開き、**Create Token** を押します。リフレッシュトークンをコピーします。
2. Capitalで **リフレッシュトークン：ALOR** に貼り付け、[Capitalで口座を接続する](#connect)の手順に進みます。**口座を取得** には、その口座のポートフォリオが一覧表示されます（株式市場はD…、通貨市場はG…、デリバティブは7500…）。ポートフォリオごとに1件ずつ追加してください。

ALORのドキュメント：[Refresh token](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [Access token](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

Capital.comのキーは、デフォルトで1年間、または指定した日付まで有効です。キーには取引の権限が付いており（Capital.comに読み取り専用のキーはありません）、Capitalは読み取りしか行いません。キーには専用のパスワードがあり、口座のパスワードとは別のものです。

1. 二要素認証をオンにしてから **Settings → API integrations** を開き、**Generate API key** を押します。ラベルと **カスタムパスワード** を入力し、有効期限はそのままにするか設定して、2FAコードで確定します。キーをコピーします。一度しか表示されません。
2. Capitalで **APIキー：Capital.com** に貼り付け、ログイン用のメールアドレスを **ログイン用メールアドレス：Capital.com** に、カスタムパスワードを **APIキーのパスワード：Capital.com** に貼り付けて、[Capitalで口座を接続する](#connect)の手順に進みます。読み取れるのはライブ口座のみです。

Capital.comのドキュメント：[Public API](https://open-api.capital.com/)

## Akahu {#akahu}

[Akahu](https://www.akahu.nz) は、ニュージーランドの銀行、投資プラットフォーム、KiwiSaverのスキームを接続します。無料の個人用アプリで、ご自身の口座を読み取れます。Akahuはデータを1日に1回ほど更新します。

1. [my.akahu.nz](https://my.akahu.nz) でサインアップし、ご利用のプロバイダ（例：Sharesies、Hatch、Kernel、Simplicity、Milford、またはご自身のKiwiSaverスキーム）を接続します。
2. **Developers** ページを開き、開発者向け利用規約に同意して、**App ID Token** と **User Access Token** をコピーします。
3. Capitalで **アプリIDトークン：Akahu** と **ユーザーアクセストークン：Akahu** に貼り付け、[Capitalで口座を接続する](#connect)の手順に進みます。

Akahuのドキュメント：[Personal apps](https://developers.akahu.nz/docs/personal-apps) · [Accounts](https://developers.akahu.nz/reference/get_accounts) · [Supported providers](https://developers.akahu.nz/docs/integrations)

## 市場別の主なブローカー {#by-market}

Capitalが対応する言語の市場で最もよく使われているブローカーを、どのように接続できるかを一覧にしました（2026年10月時点）。*直接* は上のセクションがあるもの、*SnapTrade* は[SnapTrade](#snaptrade)経由で接続できるものです。それ以外は読み取れない理由を記載しています。その場合、残高は **手動** の保有資産として管理できます。

| 市場 | ブローカー | 接続方法 |
|---|---|---|
| 米国 | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | 直接 |
| 米国 | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| 米国 | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | 公開APIなし |
| カナダ | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| カナダ | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | 公開APIなし |
| 英国・アイルランド | Trading 212, eToro, Interactive Brokers | 直接 |
| 英国・アイルランド | AJ Bell | SnapTrade |
| 英国・アイルランド | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | 公開APIなし |
| 英国・アイルランド | IG | 不可：セッションごとに口座のパスワードが必要 |
| 欧州 | Indexa Capital（スペイン）, eToro, Trading 212, Interactive Brokers | 直接 |
| 欧州 | DEGIRO, BUX | SnapTrade |
| 欧州 | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | 投資向けの公開APIなし |
| 欧州 | XTB | 不可：APIは2025年3月に終了 |
| 欧州 | Saxo, comdirect | 不可：有効期間の短いトークンまたはTANセッションのみ |
| 欧州 | Bitpanda, Freedom24 | 不可：APIが口座の評価額の合計を返さない |
| ロシア・カザフスタン | T-Invest, ALOR | 直接 |
| ロシア・カザフスタン | BCS | 不可：評価額の合計がなく、トークンは90日で失効 |
| ロシア・カザフスタン | Finam | 未対応：口座評価額の通貨が文書化されていない |
| ロシア・カザフスタン | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | 公開APIなし、またはAPIに評価額の合計がない |
| インド | Zerodha, Upstox | SnapTrade（SEBIの規則によりAPIセッションが毎日終了するため、頻繁な再接続が必要） |
| インド | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | 不可：SEBIの規則によりAPIセッションが毎日終了する |
| パキスタン・バングラデシュ | すべての取引所のブローカー | 公開APIなし |
| 中国・香港・台湾 | moomoo | SnapTrade |
| 中国・香港・台湾 | Futu, Tiger Brokers, Longbridge | 未対応：キーの有効期間やレスポンス形式が十分に文書化されていない、またはキーを読み取り専用に制限できない |
| 中国・香港・台湾 | East Money, Huatai, CITIC, Yuanta, Fubon | 公開ウェブAPIなし（デスクトップ端末または証明書方式のSDKのみ） |
| 日本 | OANDA Japan（API利用の条件を満たす口座） | 直接（OANDAとして） |
| 日本 | SBI証券, 楽天証券, マネックス証券, 松井証券 | 公開APIなし |
| オーストラリア・ニュージーランド | CommSec, Stake | SnapTrade |
| オーストラリア・ニュージーランド | Sharesies, Hatch, Kernel, Simplicity, KiwiSaver schemes | Akahu（ニュージーランドの口座） |
| 中東・アフリカ | eToro | 直接 |
| 中東・アフリカ | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | 個人向けの公開APIなし |
| 東南アジア | Stockbit, Ajaib, Bibit, IPOT, VPS | 公開APIなし |
| 東南アジア | SSI, TCBS, DNSE | 不可：ワンタイムコード付きの8時間トークン、または現金残高のみ |
| 中南米 | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | 個人向けの公開APIなし、またはパスワードのみのログイン |
| FX・CFD | OANDA, Capital.com | 直接 |
| FX・CFD | MetaTraderのブローカー（XM, Exness, Pepperstone, IC Markets, Admirals） | 不可：HTTPSでの読み取りアクセスなし |
| FX・CFD | cTraderのブローカー, FXCM, Forex.com | 不可：アプリの登録、廃止予定のAPI、またはパスワードログインが必要 |

## その他のブローカー {#other-brokers}

Capitalが接続するのは、携帯電話からHTTPSで動作し、ご自身で作成できるトークンを使い、取引はできず読み取りだけができるインターフェースに限られます。そのため、現時点では次のものは対象外です：

- **MetaTrader 4と5** の口座。投資家パスワードで読み取り専用のアクセスはできますが、それはMetaTraderターミナルの中だけです。ブローカーはこれ用のHTTPSインターフェースを公開していません。
- APIの利用にパソコン上で動くプログラム（たとえばInteractive BrokersのClient Portal Web APIゲートウェイ。Capitalは代わりにFlex Web Serviceを使います）やOAuthアプリケーションの登録が必要なブローカー。
- OAuth経由で有効期間の短いトークンしか発行しないブローカー。たとえばSaxo Bank（アクセストークンの有効期間は20分で、開発者ポータルの24時間トークンが使えるのはシミュレーション環境のみ）。
- 公開APIのない銀行やブローカー。

これらの多くのブローカーは[SnapTrade](#snaptrade)で対応できます。それ以外の場合は、残高を **手動**  の保有資産として入力し、明細を確認したときに数値を更新してください。口座の評価額を読み取れる、トークン方式のシンプルなHTTPSエンドポイントをお使いのブローカーが提供している場合は、そのドキュメントへのリンクを添えて[issueを作成]({{ site.repo }}/issues)してください。Capitalのブローカーはすべて小さなプラグインです。開発者は[プラグインガイド]({{ site.repo }}/blob/main/BROKER-PLUGINS.md)に従って追加できます。

## メッセージと対処方法 {#messages}

| メッセージ | 対処方法 |
|---|---|
| *Interactive Brokers の認証情報をブローカー画面で入力してください* / *OANDA の認証情報をブローカー画面で入力してください* | ブローカータブの認証情報で、トークン、キー、またはクライアントIDを貼り付けます。 |
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
| *tastytrade がリフレッシュトークンまたはクライアントシークレットを受け付けませんでした。新しいグラントを作成してください* | アプリケーションの新しいグラントを作成してそのリフレッシュトークンを貼り付け、クライアントシークレットも確認します。 |
| *Capital.com でセッションを開始できませんでした。APIキー、ログイン、キーのパスワードを確認してください* | キー、メールアドレス、またはキー専用のカスタムパスワードが間違っているか、キーの有効期限が切れています。 |
| *口座が見つかりません。もう一度選択してください* | ブローカーの一覧にこの口座がなくなりました。編集して、**口座を取得** から選び直してください。 |

これらのいずれの場合も、前回の値は古い値として表示されたまま残ります。
