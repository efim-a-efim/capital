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

Capital 只连接凭据长期有效的接口：您创建一次、在您撤销之前一直有效的令牌或密钥（对于 Interactive Brokers，则是在您选择的到期日之前有效，最长一年）。目前支持：

| 券商 | 使用的接口 | 读取的内容 |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service（仅用于获取报表） | 上一个交易日的净资产价值，以账户基础货币计 |
| [OANDA](#oanda) | v20 REST API，fxTrade 真实账户 | 刷新时的净资产价值，以账户货币计 |
| [Trading 212](#trading-212) | Public API，Invest 和 Stocks ISA 账户 | 刷新时的账户总价值，以账户的主货币计 |
| [SnapTrade](#snaptrade) | SnapTrade Personal，覆盖众多券商的聚合服务 | 券商报告给 SnapTrade 的账户总价值，以账户货币计 |

## 开始之前 {#before-you-start}

- **哪些数据会离开设备。** 每次刷新时，应用会通过 HTTPS 把您的访问令牌以及账户 ID 或查询 ID 发送给该券商。与任何请求一样，券商能看到您的 IP 地址。
- **凭据保存在哪里。** “设置”→“券商账户”。它使用保存在 Android Keystore 中的密钥加密，绝不会写入您的数据文件夹，也不包含在导出文件和系统备份中。每家券商一套凭据即可覆盖您为该券商添加的所有账户。
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

1. **设置 → 券商账户 → 访问令牌：Interactive Brokers**，粘贴令牌并保存。
2. 打开储蓄罐，**添加持仓**，将 **跟踪方式** 设为 **券商账户**，将 **券商** 设为 Interactive Brokers，输入 **Flex Query id** 并保存。
3. 点击 **刷新**。首次运行最多需要半分钟，因为报表是按请求生成的。

令牌过期后，刷新会提示 *令牌已过期；请在 Client Portal 中重新生成*：请生成新令牌并粘贴到“设置”中。Interactive Brokers 允许同一令牌每秒一次、每分钟十次报表请求，刷新绝不会超过这一限制。

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

1. **设置 → 券商账户 → 访问令牌：OANDA**，粘贴令牌并保存。
2. 打开储蓄罐，**添加持仓**，将 **跟踪方式** 设为 **券商账户**，将 **券商** 设为 OANDA，输入 **OANDA 账户 ID** 并保存。
3. 点击 **刷新**。

净资产价值为负的保证金账户会被报告为错误，而不会计入储蓄。

OANDA 文档：[v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [身份验证和个人访问令牌](https://developer.oanda.com/rest-live-v20/authentication/) · [账户端点](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital 调用 Trading 212 Public API 的 **账户摘要（account summary）**，并保存账户总价值，以账户的主货币计。该 API 覆盖 **Invest** 和 **Stocks ISA** 账户；一对密钥属于一个账户，而 Capital 只保存一对密钥，因此只能读取一个 Trading 212 账户。

### 1. 创建 API 密钥

1. 在 Trading 212 应用或网站中打开菜单（**☰**）→ **Settings** → **API (Beta)**，并接受风险提示。
2. 点击 **Generate API key**。为它起一个名称，只保留 **Account data** 权限（读取），并选择 *Unrestricted* IP 访问（手机的地址会变化）。
3. 提交。复制两个值：**API Key** 和 **API Secret Key**。密文只显示一次；如果丢失，请删除该密钥并重新生成一对。

### 2. 在 Capital 中连接

1. **设置 → 券商账户 → API 密钥：Trading 212** 和 **API 密文：Trading 212**，分别粘贴各个值。
2. 打开储蓄罐，**添加持仓**，将 **跟踪方式** 设为 **券商账户**，将 **券商** 设为 Trading 212，输入 **Trading 212 账号**（应用中显示的账户 ID，仅数字）并保存。
3. 点击 **刷新**。Trading 212 允许每 5 秒一次摘要请求。

Trading 212 文档：[Public API](https://docs.trading212.com/api) · [如何获取 API 密钥](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) 是一个 **聚合服务（aggregator）**：您只需把券商账户连接到 SnapTrade 一次，之后由 SnapTrade 替您读取。它覆盖许多自身没有公开 API 的券商。Capital 使用 **SnapTrade Personal**，即面向个人账户的免费方案，配合您自己的客户端 ID 和消费者密钥。该方案下的数据由 SnapTrade 大约每天刷新一次。

发送的内容：您的客户端 ID；消费者密钥本身不会被发送（请求只是用它签名）。与您的券商之间的连接由 SnapTrade 而非 Capital 持有，该连接适用 SnapTrade 的条款和隐私政策。

### 1. 创建 API 密钥

1. 在 [SnapTrade 控制台](https://dashboard.snaptrade.com/signup) 注册，并选择 **Personal** 方案。
2. 在控制台中创建 API 密钥。复制 **客户端 ID** 和 **消费者密钥**；消费者密钥只显示一次。

### 2. 在 Capital 中连接

1. **设置 → 券商账户 → 客户端 ID：SnapTrade** 和 **消费者密钥：SnapTrade**，分别粘贴各个值。
2. 打开储蓄罐，**添加持仓**，将 **跟踪方式** 设为 **券商账户**，将 **券商** 设为 SnapTrade。
3. 点击 **通过 SnapTrade 连接券商**。SnapTrade Connection Portal 会在浏览器中打开；请在那里登录您的券商（链接有效期为 5 分钟）。然后返回 Capital。
4. 点击 **获取账户** 并选择账户；其 ID 会填入 **SnapTrade 账户 ID** 字段。保存，然后点击 **刷新**。

SnapTrade 尚未完成同步的账户会提示 *SnapTrade 尚无此账户的总价值*；请稍后再刷新。

SnapTrade 文档：[Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [支持的券商](https://snaptrade.com/brokerage-integrations) · [定价](https://snaptrade.com/pricing)

## 其他券商 {#other-brokers}

Capital 只连接满足以下条件的接口：可在手机上通过 HTTPS 使用，令牌可由您自己创建，且只能读取、无法交易。因此目前不支持：

- **MetaTrader 4 和 5** 账户。投资者密码提供只读访问，但仅限于 MetaTrader 终端内部；券商没有为它提供 HTTPS 接口。
- API 需要在电脑上运行程序的券商（例如 Interactive Brokers Client Portal Web API 网关；Capital 改用 Flex Web Service），或需要注册 OAuth 应用的券商。
- 其 API 仅通过 OAuth 签发短期令牌的券商，例如 Saxo Bank（访问令牌的有效期为 20 分钟；开发者门户的 24 小时令牌仅适用于模拟环境）。
- 没有公开 API 的银行和券商。

其中许多券商可通过 [SnapTrade](#snaptrade) 接入。否则，请把余额作为 **手动** 持仓输入，并在核对对账单时更新数字。如果您的券商提供简单的、基于令牌的 HTTPS 接口，可读取账户价值，请[提交 issue]({{ site.repo }}/issues)，并附上其文档链接。

## 提示信息及处理方法 {#messages}

| 提示信息 | 处理方法 |
|---|---|
| *Interactive Brokers 需要您在“设置”→“券商账户”中填写访问令牌* / *OANDA 需要您在“设置”→“券商账户”中填写访问令牌* | 在“设置”→“券商账户”中粘贴令牌、密钥或客户端 ID。 |
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

以上任何一种情况下，之前的数值仍会显示，并标记为已过期。
