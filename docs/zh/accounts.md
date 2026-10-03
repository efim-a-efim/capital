---
layout: default
lang: zh
base: "/zh"
key: "accounts"
title: 券商和外汇账户
class: doc
---
# 券商和外汇账户

Capital 可以像读取加密货币钱包一样，读取证券或外汇账户的总价值。您把账户作为类型为 **券商账户** 的持仓添加到某个储蓄罐，每次刷新都会获取该账户以其基础货币计的净资产价值（net asset value）。应用只读取：它通过券商的报表接口、使用您自己创建的令牌访问，绝不会下单、修改或撤销订单，也绝不会转移资金。

Capital 只连接凭据长期有效的接口：您创建一次的令牌或密钥，在您撤销之前、在您选择的到期日之前，或至少数月内一直有效（T-Invest 令牌在三个月未使用后失效，ALOR 令牌在一年后失效）。无论应用设置为哪种语言，下列所有券商都可使用。目前支持：

| 券商 | 使用的接口 | 读取的内容 |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service（仅用于获取报表） | 上一个交易日的净资产价值，以账户基础货币计 |
| [OANDA](#oanda) | v20 REST API，fxTrade 真实账户 | 刷新时的净资产价值，以账户货币计 |
| [Trading 212](#trading-212) | Public API，Invest 和 Stocks ISA 账户 | 刷新时的账户总价值，以账户的主货币计 |
| [SnapTrade](#snaptrade) | SnapTrade Personal，覆盖众多券商的聚合服务 | 券商报告给 SnapTrade 的账户总价值，以账户货币计 |
| [Alpaca](#alpaca) | Trading API，真实账户 | 权益（现金加持仓），以美元计 |
| [Tradier](#tradier) | Brokerage API | 总权益，以美元计 |
| [tastytrade](#tastytrade) | Open API，使用个人 OAuth 授权 | 净清算价值，以美元计 |
| [Public.com](#public) | Individual API | 账户总价值，以美元计 |
| [eToro](#etoro) | Public API | 所选账户的余额（交易账户为现金加已投资持仓），以其货币计 |
| [Indexa Capital](#indexa-capital) | REST API，只读令牌 | 最近估值日的投资组合总额，以账户货币计 |
| [T-Invest](#t-invest) | T-Invest API（T-Bank） | 投资组合总价值，以卢布计 |
| [ALOR](#alor) | ALOR OpenAPI | 莫斯科交易所的投资组合估值，以卢布计 |
| [Capital.com](#capital-com) | Public API，真实账户 | 含未平仓盈亏的余额，以账户货币计 |
| [Akahu](#akahu) | Akahu 个人应用，新西兰的聚合服务 | 已连接账户（Sharesies、Hatch、Kernel、KiwiSaver 等）的余额，以其货币计 |

## 开始之前 {#before-you-start}

- **哪些数据会离开设备。** 每次刷新时，应用会通过 HTTPS 把您的访问令牌以及账户 ID 或查询 ID 发送给该券商。与任何请求一样，券商能看到您的 IP 地址。
- **凭据保存在哪里。** “券商”标签页 → “凭据”。它使用保存在 Android Keystore 中的密钥加密，绝不会写入您的数据文件夹，也不包含在导出文件和系统备份中。每家券商一套凭据即可覆盖您为该券商添加的所有账户。
- **您的文件夹中保存什么。** 账户 ID、最近读取的价值及读取时间。券商的其他任何数据都不会保存。
- **券商的界面可能会变化。** 下面的步骤与各券商网站截至 2026 年 10 月的界面一致。券商会不时重命名菜单、移动设置，所以您实际操作时某一步可能略有不同。每一节中链接的券商官方文档才是权威来源：如果这里的某一步与现状不符，请在券商页面上查找相同的术语。

## Interactive Brokers {#interactive-brokers}

Capital 使用 **Flex Web Service**，这是 Interactive Brokers 用于获取预先配置好的报表的接口。它所用的令牌只能生成和下载报表，不能登录、交易或提现。Capital 请求 Activity Flex Query 中的 **Net Asset Value (NAV) Summary in Base**，并取最新报告日期（report date）的 Total，因此得到的价值是上一个交易日的收盘值。

以下菜单和字段名称是您在 Interactive Brokers 英文界面中看到的名称，因此保留英文，首次出现时附上简短中文说明。

### 1. 创建 Flex Query

1. 登录 [Client Portal](https://www.interactivebrokers.com/portal)，打开 **Performance & Reports → Flex Queries**（业绩和报表；在某些账户中该菜单名为 *Reporting*）。
2. 在 **Activity Flex Query**（活动 Flex 查询）下点击 **+**（创建）。为查询起一个名称，例如 `Capital`。
3. 在 **Sections**（部分）列表中，恰好启用以下两个部分和字段（也可以选中某个部分的全部字段）：
   - **Account Information**：*Account ID*、*Currency*。
   - **Net Asset Value (NAV) Summary in Base**：*Report Date*、*Total*。
4. 在 **Delivery Configuration**（交付配置）中，将 **Format** 设为 `XML`，将 **Period** 设为 `Last Business Day`。其他选项保持默认即可。
5. 保存查询，然后点击它旁边的 **i**（信息）图标，记下 **Query ID**，这是一个数字。

一个查询必须只覆盖一个账户。如果您有关联账户或顾问架构，请为每个账户分别创建一个查询，并在创建时只选择该账户。

### 2. 启用 Flex Web Service 并创建令牌

1. 在同一个 **Flex Queries** 页面中打开 **Flex Web Service Configuration**。
2. 将 **Flex Web Service Status** 打开并保存。系统会创建一个令牌。
3. 如需选择令牌的有效期，请点击 **Generate New Token**：可选 6 小时到 1 年不等。手机的地址会变化，因此请将 **Valid for IP address** 留空。生成新令牌会使之前的令牌失效。
4. 复制令牌。

### 3. 在 Capital 中连接

1. **券商 → 凭据 → 访问令牌：Interactive Brokers**，粘贴令牌并保存。
2. **券商 → +**：输入名称，将 **券商** 设为 Interactive Brokers，输入 **Flex Query id** 并保存。
3. 打开储蓄罐，**添加持仓**，将 **跟踪方式** 设为 **券商账户**，选择该账户并保存。
4. 点击 **刷新**。首次运行最多需要半分钟，因为报表是按请求生成的。

令牌过期后，刷新会提示 *令牌已过期；请在 Client Portal 中重新生成*：请生成新令牌并粘贴到“凭据”下。Interactive Brokers 允许同一令牌每秒一次、每分钟十次报表请求，刷新绝不会超过这一限制。

Interactive Brokers 文档：[Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [启用并创建访问令牌](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [创建 Flex Query](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query 参考](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

Capital 调用 OANDA v20 REST API 的 **账户摘要（account summary）**，并保存账户的 NAV（余额加未实现盈亏），以账户货币计。仅支持 fxTrade 真实账户；模拟账户不属于储蓄。

**OANDA 个人访问令牌不是只读的。** 它拥有您登录名下所有子账户的完整 API 访问权限，包括交易。Capital 只会调用账户摘要，但任何获得该令牌的人都可以用它交易。请像对待密码一样对待它：只粘贴到 Capital 中，如果手机丢失，请在 OANDA 门户中撤销它。

### 1. 创建令牌

1. 登录您的 OANDA fxTrade 账户管理门户。
2. 打开 **My Services → Manage API Access**（我的服务 → 管理 API 访问；旧版门户中为 *My Account → My Services → Manage API Access*）。
3. 接受 API 许可协议并点击 **Generate**。复制令牌；OANDA 不会再次显示它。如果丢失，请在那里撤销并重新生成。

### 2. 查找账户 ID

v20 账户 ID 的形式为 `001-001-1234567-001`，带连字符。它在同一门户中列在每个子账户旁边，在 fxTrade 平台的账户详情中也能看到。

### 3. 在 Capital 中连接

1. **券商 → 凭据 → 访问令牌：OANDA**，粘贴令牌并保存。
2. **券商 → +**：输入名称，将 **券商** 设为 OANDA，输入 **OANDA 账户 ID** 并保存。
3. 打开储蓄罐，**添加持仓**，将 **跟踪方式** 设为 **券商账户**，选择该账户并保存。
4. 点击 **刷新**。

净资产价值为负的保证金账户会被报告为错误，而不会计入储蓄。

OANDA 文档：[v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [身份验证和个人访问令牌](https://developer.oanda.com/rest-live-v20/authentication/) · [账户端点](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital 调用 Trading 212 Public API 的 **账户摘要（account summary）**，并保存账户总价值，以账户的主货币计。该 API 覆盖 **Invest** 和 **Stocks ISA** 账户；一对密钥属于一个账户，而 Capital 只保存一对密钥，因此只能读取一个 Trading 212 账户。

### 1. 创建 API 密钥

1. 在 Trading 212 应用或网站中打开菜单（**☰**）→ **Settings** → **API (Beta)**，并接受风险提示。
2. 点击 **Generate API key**。为它起一个名称，只保留 **Account data** 权限（读取），并选择 *Unrestricted* IP 访问（手机的地址会变化）。
3. 提交。复制两个值：**API Key** 和 **API Secret Key**。密文只显示一次；如果丢失，请删除该密钥并重新生成一对。

### 2. 在 Capital 中连接

1. **券商 → 凭据 → API 密钥：Trading 212** 和 **API 密文：Trading 212**，分别粘贴各个值。
2. **券商 → +**：输入名称，将 **券商** 设为 Trading 212，输入 **Trading 212 账号**（应用中显示的账户 ID，仅数字）并保存。
3. 打开储蓄罐，**添加持仓**，将 **跟踪方式** 设为 **券商账户**，选择该账户并保存。
4. 点击 **刷新**。Trading 212 允许每 5 秒一次摘要请求。

Trading 212 文档：[Public API](https://docs.trading212.com/api) · [如何获取 API 密钥](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) 是一个 **聚合服务（aggregator）**：您只需把券商账户连接到 SnapTrade 一次，之后由 SnapTrade 替您读取。它覆盖许多自身没有公开 API 的券商。Capital 使用 **SnapTrade Personal**，即面向个人账户的免费方案，配合您自己的客户端 ID 和消费者密钥。该方案下的数据由 SnapTrade 大约每天刷新一次。

发送的内容：您的客户端 ID；消费者密钥本身不会被发送（请求只是用它签名）。与您的券商之间的连接由 SnapTrade 而非 Capital 持有，该连接适用 SnapTrade 的条款和隐私政策。

### 1. 创建 API 密钥

1. 在 [SnapTrade 控制台](https://dashboard.snaptrade.com/signup) 注册，并选择 **Personal** 方案。
2. 在控制台中创建 API 密钥。复制 **客户端 ID** 和 **消费者密钥**；消费者密钥只显示一次。

### 2. 在 Capital 中连接

1. **券商 → 凭据 → 客户端 ID：SnapTrade** 和 **消费者密钥：SnapTrade**，分别粘贴各个值。
2. **券商 → +**：输入名称，将 **券商** 设为 SnapTrade。
3. 点击 **通过 SnapTrade 连接券商**。SnapTrade Connection Portal 会在浏览器中打开；请在那里登录您的券商（链接有效期为 5 分钟）。然后返回 Capital。
4. 点击 **获取账户** 并选择账户；其 ID 会填入 **SnapTrade 账户 ID** 字段。保存。
5. 打开储蓄罐，**添加持仓**，将 **跟踪方式** 设为 **券商账户**，选择该账户并保存，然后点击 **刷新**。

SnapTrade 尚未完成同步的账户会提示 *SnapTrade 尚无此账户的总价值*；请稍后再刷新。

SnapTrade 文档：[Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [支持的券商](https://snaptrade.com/brokerage-integrations) · [定价](https://snaptrade.com/pricing)

## 在 Capital 中连接账户 {#connect}

下面各节说明如何在各券商处创建凭据。在 Capital 中，所有券商的步骤都相同：

1. **券商 → 凭据**：点击该券商的凭据按钮，并粘贴每个值。
2. **券商 → +**：输入名称，选择 **券商**，然后点击 **获取账户** 并选择账户（或输入其 ID），保存。
3. 打开储蓄罐，**添加持仓**，将 **跟踪方式** 设为 **券商账户**，选择该账户并保存。点击 **刷新**。

## Alpaca {#alpaca}

Alpaca 为每个账户签发一个密钥 ID 和一个密文；在您重新生成之前一直有效。只读取真实账户：模拟账户的密钥无法用于真实 API。

1. 登录 [Alpaca dashboard](https://app.alpaca.markets)，切换到您的真实账户，在主页的 **API Keys** 下点击 **Generate New Keys**。
2. 复制 **API Key ID** 和 **Secret Key**；密文只显示一次。
3. 在 Capital 中把它们粘贴为 **API 密钥：Alpaca** 和 **API 密文：Alpaca**，然后按照[在 Capital 中连接账户](#connect)操作。**获取账户** 会显示该密钥对应的账号。

Alpaca 文档：[Authentication](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

Tradier 设置中的 API 令牌永不过期。

1. 登录 Tradier，打开 [Settings → API Access](https://web.tradier.com/user/api)。复制您的券商账户（而非沙盒）的 **API Access Token**。
2. 在 Capital 中把它粘贴为 **访问令牌：Tradier**，然后按照[在 Capital 中连接账户](#connect)操作。

Tradier 文档：[Authentication](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

tastytrade 使用个人 OAuth 授权：您为自己创建一个应用，以及一个刷新令牌永不过期的授权。Capital 每次刷新时用它换取一个 15 分钟有效的访问令牌。

1. 在 [my.tastytrade.com](https://my.tastytrade.com) 打开 **Manage → My Profile → API → OAuth Applications**，点击 **+ New OAuth client**。填写名称、任意 HTTPS 重定向 URI（例如 `https://capital.fimych.dev` ），并且只选择 **read** 权限范围。保存并复制 **Client Secret**；它只显示一次。
2. 点击应用旁的 **Manage**，再点击 **Create Grant**，并复制 **refresh token**。
3. 在 Capital 中把它们粘贴为 **刷新令牌：tastytrade** 和 **客户端密文：tastytrade**，然后按照[在 Capital 中连接账户](#connect)操作。

tastytrade 文档：[OAuth2 and personal grants](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

Public 的 Individual API 供您本人的账户使用。秘密密钥长期有效且可撤销；Capital 每次刷新时用它换取一个五分钟有效的访问令牌。

1. 在 Public 的网页应用中打开设置里的 **API** 页面，并生成一个 **secret key**。
2. 在 Capital 中把它粘贴为 **秘密密钥：Public.com**，然后按照[在 Capital 中连接账户](#connect)操作。

Public 文档：[Quickstart](https://public.com/api/docs/quickstart) · [Access tokens](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [Portfolio](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

eToro 密钥长期有效；您可以为其设置到期日期和 IP 列表，也可以将其设为只读。您的 eToro 账户必须已通过验证。

1. 在 eToro 中打开 **Settings → Trading → API Key Management**，点击 **Create New Key**。选择 **Real** 环境、**Read** 权限、不设 IP 列表，并可选择设置到期日期。用短信验证码确认。
2. 复制 **Public API Key** 和 **User Key**；用户密钥只显示一次。
3. 在 Capital 中把它们粘贴为 **公开 API 密钥：eToro** 和 **用户密钥：eToro**，然后按照[在 Capital 中连接账户](#connect)操作。**获取账户** 会列出您的交易账户、现金账户及其他 eToro 账户。

eToro 文档：[Authentication](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [Getting started](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

Indexa 私人区域中的令牌是只读的。它与您的邮箱、密码和设备绑定：更改密码后，请重新生成。

1. 在 Indexa 的私人区域打开 **用户设置 → 应用**（User settings → Applications），并复制令牌。
2. 在 Capital 中把它粘贴为 **访问令牌：Indexa Capital**，然后按照[在 Capital 中连接账户](#connect)操作。养老金账户和投资账户都会列出。

Indexa 每个工作日对基金估值一次；观察日期即该估值日。

Indexa Capital 文档：[REST API](https://indexacapital.com/en/api-rest-v1) · [Connecting with the API](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

T-Bank 的 T-Invest API 接受您在投资设置中签发的令牌。令牌在最后一次使用三个月后失效，并且必须在签发后七天内使用；每周刷新一次可使其保持有效。请选择 **只读** 令牌。

1. 打开 [T-Invest 设置](https://www.tbank.ru/invest/settings/)，为交易所签发一个 **只读** 权限（所有账户或单个账户）的 **T-Invest API 令牌**。签发时必须关闭“通过验证码确认交易”。复制令牌；它只显示一次。
2. 在 Capital 中把它粘贴为 **访问令牌：T-Invest**，然后按照[在 Capital 中连接账户](#connect)操作。

T-Bank 使用俄罗斯 Trusted Root CA（Russian Trusted Root CA）为该 API 提供服务，而 Android 并不内置该证书。Capital 仅对 T-Invest API 地址（`invest-public-api.tbank.ru`）信任该证书，对其他任何连接都不信任。

T-Invest 文档：[Tokens](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

ALOR 签发有效期为一年的刷新令牌；Capital 每次刷新时用它换取一个 30 分钟有效的访问令牌。ALOR 不提供只读令牌：该令牌可以交易，而 Capital 只读取。

1. 登录 [ALOR 开发者门户](https://alor.dev)，绑定您的交易账户，打开 **API Access Tokens** 并点击 **Create Token**。复制刷新令牌。
2. 在 Capital 中把它粘贴为 **刷新令牌：ALOR**，然后按照[在 Capital 中连接账户](#connect)操作。**获取账户** 会列出该账户的投资组合（股票市场 D…、外汇市场 G…、衍生品 7500…）；每个投资组合添加一个账户。

ALOR 文档：[Refresh token](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [Access token](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

Capital.com 的密钥默认有效期为一年，或到您选择的日期为止。它们带有交易权限（Capital.com 没有只读密钥）；Capital 只读取。密钥有自己的密码，它不是您的账户密码。

1. 开启双重验证，然后打开 **Settings → API integrations** 并点击 **Generate API key**。为其设置标签和 **自定义密码**，保留或设置到期日，并用双重验证码确认。复制密钥；它只显示一次。
2. 在 Capital 中粘贴 **API 密钥：Capital.com**，把您的登录邮箱粘贴为 **登录邮箱：Capital.com**，把自定义密码粘贴为 **API 密钥密码：Capital.com**，然后按照[在 Capital 中连接账户](#connect)操作。只读取真实账户。

Capital.com 文档：[Public API](https://open-api.capital.com/)

## Akahu {#akahu}

[Akahu](https://www.akahu.nz) 连接新西兰的银行、投资平台和 KiwiSaver 计划；免费的个人应用可读取您自己的账户。Akahu 大约每天刷新一次数据。

1. 在 [my.akahu.nz](https://my.akahu.nz) 注册并连接您的服务商（例如 Sharesies、Hatch、Kernel、Simplicity、Milford 或您的 KiwiSaver 计划）。
2. 打开 **Developers** 页面，接受开发者条款，并复制 **App ID Token** 和 **User Access Token**。
3. 在 Capital 中把它们粘贴为 **应用 ID 令牌：Akahu** 和 **用户访问令牌：Akahu**，然后按照[在 Capital 中连接账户](#connect)操作。

Akahu 文档：[Personal apps](https://developers.akahu.nz/docs/personal-apps) · [Accounts](https://developers.akahu.nz/reference/get_accounts) · [Supported providers](https://developers.akahu.nz/docs/integrations)

## 热门券商（按市场） {#by-market}

Capital 所支持语言对应市场中最常用的券商可以如何连接，截至 2026 年 10 月。*直连* 表示上文有对应小节；*SnapTrade* 表示通过 [SnapTrade](#snaptrade)；其他情况则说明无法读取的原因，余额可作为 **手动** 持仓保存。

| 市场 | 券商 | 方式 |
|---|---|---|
| 美国 | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | 直连 |
| 美国 | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| 美国 | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | 无公开 API |
| 加拿大 | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| 加拿大 | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | 无公开 API |
| 英国和爱尔兰 | Trading 212, eToro, Interactive Brokers | 直连 |
| 英国和爱尔兰 | AJ Bell | SnapTrade |
| 英国和爱尔兰 | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | 无公开 API |
| 英国和爱尔兰 | IG | 无法支持：每次会话都需要账户密码 |
| 欧洲 | Indexa Capital (Spain), eToro, Trading 212, Interactive Brokers | 直连 |
| 欧洲 | DEGIRO, BUX | SnapTrade |
| 欧洲 | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | 投资业务无公开 API |
| 欧洲 | XTB | 无法支持：API 已于 2025 年 3 月关闭 |
| 欧洲 | Saxo, comdirect | 无法支持：仅有短期令牌或 TAN 会话 |
| 欧洲 | Bitpanda, Freedom24 | 无法支持：API 不返回账户总价值 |
| 俄罗斯和哈萨克斯坦 | T-Invest, ALOR | 直连 |
| 俄罗斯和哈萨克斯坦 | BCS | 无法支持：没有总价值，且其令牌 90 天后过期 |
| 俄罗斯和哈萨克斯坦 | Finam | 暂不支持：账户价值的货币未在文档中说明 |
| 俄罗斯和哈萨克斯坦 | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | 无公开 API，或其中没有总价值 |
| 印度 | Zerodha, Upstox | SnapTrade（SEBI 规定每天终止 API 会话，因此连接需要频繁续期） |
| 印度 | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | 无法支持：SEBI 规定每个 API 会话每天终止 |
| 巴基斯坦和孟加拉国 | 所有交易所券商 | 无公开 API |
| 中国大陆、香港和台湾 | moomoo | SnapTrade |
| 中国大陆、香港和台湾 | Futu, Tiger Brokers, Longbridge | 暂不支持：密钥有效期或响应格式未完整说明，或密钥无法限定为只读 |
| 中国大陆、香港和台湾 | East Money, Huatai, CITIC, Yuanta, Fubon | 无公开网页 API（仅有桌面终端或证书 SDK） |
| 日本 | OANDA Japan（符合 API 访问条件的账户） | 直连，同 OANDA |
| 日本 | SBI Securities, Rakuten Securities, Monex, Matsui | 无公开 API |
| 澳大利亚和新西兰 | CommSec, Stake | SnapTrade |
| 澳大利亚和新西兰 | Sharesies, Hatch, Kernel, Simplicity, KiwiSaver schemes | Akahu（新西兰账户） |
| 中东和非洲 | eToro | 直连 |
| 中东和非洲 | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | 个人无公开 API |
| 东南亚 | Stockbit, Ajaib, Bibit, IPOT, VPS | 无公开 API |
| 东南亚 | SSI, TCBS, DNSE | 无法支持：8 小时令牌且需一次性验证码，或仅有现金余额 |
| 拉丁美洲 | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | 个人无公开 API，或仅支持密码登录 |
| 外汇和差价合约 | OANDA, Capital.com | 直连 |
| 外汇和差价合约 | MetaTrader brokers (XM, Exness, Pepperstone, IC Markets, Admirals) | 无法支持：没有 HTTPS 读取接口 |
| 外汇和差价合约 | cTrader brokers, FXCM, Forex.com | 无法支持：需要注册应用、API 已弃用或仅支持密码登录 |

## 其他券商 {#other-brokers}

Capital 只连接满足以下条件的接口：可在手机上通过 HTTPS 使用，令牌可由您自己创建，且只能读取、无法交易。因此目前不支持：

- **MetaTrader 4 和 5** 账户。投资者密码提供只读访问，但仅限于 MetaTrader 终端内部；券商没有为它提供 HTTPS 接口。
- API 需要在电脑上运行程序的券商（例如 Interactive Brokers Client Portal Web API 网关；Capital 改用 Flex Web Service），或需要注册 OAuth 应用的券商。
- 其 API 仅通过 OAuth 签发短期令牌的券商，例如 Saxo Bank（访问令牌的有效期为 20 分钟；开发者门户的 24 小时令牌仅适用于模拟环境）。
- 没有公开 API 的银行和券商。

其中许多券商可通过 [SnapTrade](#snaptrade) 接入。否则，请把余额作为 **手动** 持仓输入，并在核对对账单时更新数字。如果您的券商提供简单的、基于令牌的 HTTPS 接口，可读取账户价值，请[提交 issue]({{ site.repo }}/issues)，并附上其文档链接。Capital 中的每个券商都是一个小型插件；开发者可参考[插件指南]({{ site.repo }}/blob/main/BROKER-PLUGINS.md)自行添加。

## 提示信息及处理方法 {#messages}

| 提示信息 | 处理方法 |
|---|---|
| *Interactive Brokers 需要在“券商”页面填写凭据* / *OANDA 需要在“券商”页面填写凭据* | 在“券商”标签页的“凭据”下粘贴令牌、密钥或客户端 ID。 |
| *令牌已过期；请在 Client Portal 中重新生成* | 生成新的 Flex Web Service 令牌并粘贴。 |
| *令牌无效* | 重新复制令牌；新令牌会替换旧令牌。 |
| *令牌仅限其他 IP 地址使用* | 生成不限制 IP 的令牌。 |
| *Flex Query id 无效* | 检查数字；该查询必须是此登录名下的 Activity Flex Query。 |
| *请在 Flex Query 中添加 Net Asset Value (NAV) Summary in Base 部分，并包含 Report Date 和 Total* | 编辑查询，添加该部分和字段。 |
| *请在 Flex Query 中添加 Account Information 的 Currency 字段* | 编辑查询，添加该字段。 |
| *该查询返回了 N 个账户；请为每个账户单独创建一个 Flex Query* | 创建只覆盖一个账户的查询。 |
| *报表尚未生成；请一分钟后再刷新* | Interactive Brokers 仍在生成报表；请稍后再刷新。 |
| *访问被拒绝；请检查数据源密钥或配额* | OANDA 令牌有误或已被撤销，或者 Trading 212 密钥对有误或缺少 Account data 权限。 |
| *SnapTrade 尚无此账户的总价值；请同步连接后重试* | SnapTrade 尚未同步该券商；请稍后再刷新。 |
| *尚未连接任何账户。请先通过 SnapTrade 连接券商。* | 在编辑器中打开 Connection Portal 并连接券商。 |
| *不支持负的账户价值 …* | 账户处于借方；它不会为您的储蓄增加任何内容。 |
| *tastytrade 拒绝了刷新令牌或客户端密文；请创建新的授权* | 为该应用创建新的授权并粘贴其刷新令牌；检查客户端密文。 |
| *Capital.com 未能建立会话；请检查 API 密钥、登录名和密钥密码* | 密钥、邮箱或密钥的自定义密码有误，或密钥已过期。 |
| *未找到账户；请重新选择* | 券商不再列出此账户；请编辑它，并从 **获取账户** 中选择。 |

以上任何一种情况下，之前的数值仍会显示，并标记为已过期。
