---
layout: default
lang: ru
base: "/ru"
key: "accounts"
title: Брокерские и форекс-счета
class: doc
---
# Брокерские и форекс-счета

Capital умеет читать общую стоимость брокерского или форекс-счёта так же, как баланс криптокошелька. Вы добавляете счёт в копилку как актив типа **Брокерский счёт**, и при каждом обновлении приложение загружает чистую стоимость активов (net asset value) счёта в его базовой валюте. Приложение только читает: оно использует интерфейс отчётности брокера с токеном, который вы создаёте сами, никогда не отправляет, не меняет и не отменяет ордера и никогда не перемещает деньги.

Capital подключается только к интерфейсам с долгоживущими учётными данными: токеном или ключом, который вы создаёте один раз и который действует, пока вы его не отзовёте, до выбранной вами даты истечения или не менее нескольких месяцев (токены T-Invest перестают действовать через три месяца без использования, токены ALOR — через год). Каждый из перечисленных ниже брокеров доступен при любом языке приложения. Сейчас поддерживаются:

| Брокер | Используемый интерфейс | Что читается |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (только получение отчётов) | Чистая стоимость активов на последний рабочий день в базовой валюте счёта |
| [OANDA](#oanda) | v20 REST API, реальные счета fxTrade | Чистая стоимость активов на момент обновления в валюте счёта |
| [Trading 212](#trading-212) | Public API, счета Invest и Stocks ISA | Общая стоимость счёта на момент обновления в основной валюте счёта |
| [SnapTrade](#snaptrade) | SnapTrade Personal, агрегатор, охватывающий многих брокеров | Общая стоимость счёта в том виде, как брокер передаёт её в SnapTrade, в валюте счёта |
| [Alpaca](#alpaca) | Trading API, реальные счета | Капитал счёта (деньги плюс позиции) в долларах США |
| [Tradier](#tradier) | Brokerage API | Общий капитал в долларах США |
| [tastytrade](#tastytrade) | Open API с личным OAuth grant | Чистая ликвидационная стоимость в долларах США |
| [Public.com](#public) | Individual API | Общая стоимость счёта в долларах США |
| [eToro](#etoro) | Public API | Баланс выбранного счёта (для торгового счёта: деньги плюс вложенные позиции) в его валюте |
| [Indexa Capital](#indexa-capital) | REST API, токен только для чтения | Итог портфеля на дату последней оценки в валюте счёта |
| [T-Invest](#t-invest) | T-Invest API (T-Bank) | Общая стоимость портфеля в рублях |
| [ALOR](#alor) | ALOR OpenAPI | Оценка портфеля на Московской бирже в рублях |
| [Capital.com](#capital-com) | Public API, реальные счета | Баланс с учётом открытой прибыли и убытка в валюте счёта |
| [Akahu](#akahu) | Личное приложение Akahu, агрегатор Новой Зеландии | Баланс подключённого счёта (Sharesies, Hatch, Kernel, KiwiSaver и другие) в его валюте |

## Прежде чем начать {#before-you-start}

- **Что покидает устройство.** При каждом обновлении приложение отправляет этому брокеру ваш токен доступа и ID счёта или запроса по HTTPS. Брокер видит ваш IP-адрес, как и при любом запросе.
- **Где хранятся учётные данные.** Вкладка «Брокеры» → Учётные данные. Они зашифрованы ключом из Android Keystore, никогда не записываются в папку с данными и не попадают в экспорт и системные резервные копии. Один набор учётных данных на брокера покрывает все счета этого брокера, которые вы добавите.
- **Что хранится в вашей папке.** ID счёта, последнее прочитанное значение и время его чтения. Больше ничего от брокера.
- **Экраны брокера могут измениться.** Описанные ниже шаги соответствуют сайтам брокеров на октябрь 2026 года. Брокеры время от времени переименовывают меню и переносят настройки, поэтому при выполнении шаг может выглядеть немного иначе. Главный источник — собственная документация брокера, ссылки на неё приведены в каждом разделе: если шаг здесь больше не совпадает, найдите тот же термин на странице брокера.

## Interactive Brokers {#interactive-brokers}

Capital использует **Flex Web Service** — интерфейс Interactive Brokers для получения заранее настроенных отчётов. Токен, который для этого нужен, может только создавать и скачивать отчёты; с ним нельзя войти в аккаунт, торговать или выводить деньги. Capital запрашивает раздел **Net Asset Value (NAV) Summary in Base** запроса Activity Flex Query и берёт итог на последнюю дату отчёта, то есть значение на закрытие последнего рабочего дня.

### 1. Создайте Flex Query

1. Войдите в [Client Portal](https://www.interactivebrokers.com/portal) и откройте **Performance & Reports → Flex Queries** (отчёты и показатели; в некоторых аккаунтах меню называется *Reporting*).
2. В разделе **Activity Flex Query** нажмите **+** (создать). Задайте запросу имя, например `Capital`.
3. В списке **Sections** (разделы) включите ровно эти два раздела и поля (можно выбрать все поля раздела):
   - **Account Information**: *Account ID*, *Currency*.
   - **Net Asset Value (NAV) Summary in Base**: *Report Date*, *Total*.
4. В **Delivery Configuration** (параметры выдачи) установите **Format** (формат) `XML` и **Period** (период) `Last Business Day`. Остальные параметры можно оставить по умолчанию.
5. Сохраните запрос, затем нажмите значок **i** (информация) рядом с ним и запишите **Query ID** — число.

Запрос должен охватывать один счёт. Если у вас связанные счета или структура консультанта, создайте по одному запросу на счёт и при создании выбирайте только этот счёт.

### 2. Включите Flex Web Service и создайте токен

1. На той же странице **Flex Queries** откройте **Flex Web Service Configuration**.
2. Включите **Flex Web Service Status** и сохраните. Токен будет создан.
3. Чтобы выбрать срок действия токена, нажмите **Generate New Token**: от 6 часов до 1 года. Поле **Valid for IP address** (действителен для IP-адреса) для телефона, адрес которого меняется, оставьте пустым. Новый токен делает предыдущий недействительным.
4. Скопируйте токен.

### 3. Подключите его в Capital

1. **Брокеры → Учётные данные → Токен доступа: Interactive Brokers**, вставьте токен и сохраните.
2. **Брокеры → +**: введите название, в поле **Брокер** выберите Interactive Brokers, введите **Flex Query id** и сохраните.
3. Откройте копилку, **Добавить актив**, в поле **Способ учёта** выберите **Брокерский счёт**, выберите счёт и сохраните.
4. Нажмите **Обновить**. Первый запуск занимает до 30 секунд, потому что отчёт формируется по запросу.

Когда срок действия токена истекает, при обновлении появляется сообщение *Срок действия токена истёк; создайте новый в Client Portal*: создайте новый токен и вставьте его в разделе «Учётные данные». Interactive Brokers допускает один запрос отчёта в секунду и десять в минуту с одного токена; при обновлении этот предел не превышается.

Документация Interactive Brokers: [Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [Enable and create the access token](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [Create a Flex Query](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query reference](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

Capital вызывает **сводку по счёту** (account summary) в OANDA v20 REST API и сохраняет NAV счёта (баланс плюс нереализованная прибыль или убыток) в валюте счёта. Поддерживаются только реальные счета **fxTrade**; демо-счета не являются сбережениями.

**Персональный токен доступа OANDA не ограничен чтением.** Он даёт полный доступ к API для всех субсчетов вашего логина, включая торговлю. Capital обращается только к сводке по счёту, но тот, кто получит токен, сможет торговать с его помощью. Относитесь к нему как к паролю: вставляйте его только в Capital и отзовите его на портале OANDA, если потеряете телефон.

### 1. Создайте токен

1. Войдите на портал управления счётом OANDA fxTrade.
2. Откройте **My Services → Manage API Access** (на старом портале: *My Account → My Services → Manage API Access*).
3. Примите лицензию на API и нажмите **Generate**. Скопируйте токен; повторно OANDA его не показывает. Если вы его потеряли, отзовите его там и создайте новый.

### 2. Найдите ID счёта

ID счёта v20 имеет вид `001-001-1234567-001`, с дефисами. Он указан на том же портале рядом с каждым субсчётом, а также на платформе fxTrade в сведениях о счёте.

### 3. Подключите его в Capital

1. **Брокеры → Учётные данные → Токен доступа: OANDA**, вставьте токен и сохраните.
2. **Брокеры → +**: введите название, в поле **Брокер** выберите OANDA, введите **ID счёта OANDA** и сохраните.
3. Откройте копилку, **Добавить актив**, в поле **Способ учёта** выберите **Брокерский счёт**, выберите счёт и сохраните.
4. Нажмите **Обновить**.

Маржинальный счёт с отрицательным NAV считается ошибкой и не учитывается как сбережения.

Документация OANDA: [v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital вызывает **сводку по счёту** (account summary) в Trading 212 Public API и сохраняет общую стоимость счёта в основной валюте счёта. API охватывает счета **Invest** и **Stocks ISA**; пара ключей принадлежит одному счёту, а Capital хранит одну пару ключей, поэтому читает один счёт Trading 212.

### 1. Создайте API key

1. В приложении или на сайте Trading 212 откройте меню (**☰**) → **Settings** → **API (Beta)** и примите предупреждение о рисках.
2. Нажмите **Generate API key**. Задайте ему имя, оставьте только разрешение **Account data** (чтение) и выберите доступ с любого IP-адреса, *Unrestricted* (адрес телефона меняется).
3. Подтвердите. Скопируйте оба значения: **API Key** и **API Secret Key**. Секрет показывается один раз; если вы его потеряли, удалите ключ и создайте новую пару.

### 2. Подключите его в Capital

1. **Брокеры → Учётные данные → API key: Trading 212** и **API secret: Trading 212**, вставьте каждое значение.
2. **Брокеры → +**: введите название, в поле **Брокер** выберите Trading 212, введите **Номер счёта Trading 212** (ID счёта, указанный в приложении, только цифры) и сохраните.
3. Откройте копилку, **Добавить актив**, в поле **Способ учёта** выберите **Брокерский счёт**, выберите счёт и сохраните.
4. Нажмите **Обновить**. Trading 212 допускает один запрос сводки раз в 5 секунд.

Документация Trading 212: [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) — агрегатор: вы один раз подключаете брокерский счёт к SnapTrade, и SnapTrade читает его за вас. Он охватывает многих брокеров, у которых нет собственного публичного API. Capital использует **SnapTrade Personal**, бесплатный тариф для ваших собственных счетов, с вашими собственными client id и consumer key. SnapTrade обновляет данные на этом тарифе примерно раз в сутки.

Что отправляется: ваш client id и подпись; сам consumer key не передаётся (им подписываются запросы). Подключение к вашему брокеру хранит SnapTrade, а не Capital; к этому подключению применяются условия и политика конфиденциальности SnapTrade.

### 1. Создайте API key

1. Зарегистрируйтесь на [панели SnapTrade](https://dashboard.snaptrade.com/signup) и выберите тариф **Personal**.
2. На панели создайте API key. Скопируйте **client id** и **consumer key**; consumer key показывается один раз.

### 2. Подключите его в Capital

1. **Брокеры → Учётные данные → Client id: SnapTrade** и **Consumer key: SnapTrade**, вставьте каждое значение.
2. **Брокеры → +**: введите название и выберите в поле **Брокер** SnapTrade.
3. Нажмите **Подключить брокера через SnapTrade**. В браузере откроется Connection Portal SnapTrade; войдите там в аккаунт брокера (ссылка действует 5 минут). Вернитесь в Capital.
4. Нажмите **Загрузить счета** и выберите счёт; его ID заполнит поле **ID счёта SnapTrade**. Сохраните.
5. Откройте копилку, **Добавить актив**, в поле **Способ учёта** выберите **Брокерский счёт**, выберите счёт и сохраните, затем нажмите **Обновить**.

Если SnapTrade ещё не закончил синхронизацию счёта, появится сообщение *У SnapTrade пока нет общей стоимости этого счёта*; обновите позже.

Документация SnapTrade: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Подключение счёта в Capital {#connect}

Ниже описано, как создать учётные данные у каждого брокера. В Capital шаги одинаковы для всех:

1. **Брокеры → Учётные данные**: нажмите кнопки учётных данных брокера и вставьте каждое значение.
2. **Брокеры → +**: введите название, выберите **Брокер**, затем нажмите **Загрузить счета** и выберите счёт (или введите его ID) и сохраните.
3. Откройте копилку, **Добавить актив**, в поле **Способ учёта** выберите **Брокерский счёт**, выберите счёт и сохраните. Нажмите **Обновить**.

## Alpaca {#alpaca}

Alpaca выдаёт идентификатор ключа и секрет для каждого счёта; они действуют, пока вы не создадите их заново. Читаются только реальные счета: ключи бумажного (paper) счёта с реальным API не работают.

1. Войдите на [панель Alpaca](https://app.alpaca.markets), переключитесь на реальный счёт и на главной странице в разделе **API Keys** нажмите **Generate New Keys**.
2. Скопируйте **API Key ID** и **Secret Key**; секрет показывается один раз.
3. В Capital вставьте их как **API key: Alpaca** и **API secret: Alpaca**, затем выполните шаги из раздела [Подключение счёта](#connect). **Загрузить счета** показывает номер счёта этого ключа.

Документация Alpaca: [Authentication](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

API-токен из настроек Tradier не истекает.

1. Войдите в Tradier и откройте [Settings → API Access](https://web.tradier.com/user/api). Скопируйте **API Access Token** вашего брокерского счёта (не токен песочницы).
2. В Capital вставьте его как **Токен доступа: Tradier**, затем выполните шаги из раздела [Подключение счёта](#connect).

Документация Tradier: [Authentication](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

tastytrade использует личный OAuth grant: вы создаёте приложение для себя и grant, refresh-токен которого не истекает. При каждом обновлении Capital обменивает его на access-токен со сроком 15 минут.

1. На [my.tastytrade.com](https://my.tastytrade.com) откройте **Manage → My Profile → API → OAuth Applications** и нажмите **+ New OAuth client**. Задайте имя, любой HTTPS redirect URI (например, `https://capital.fimych.dev`) и только область **read**. Сохраните и скопируйте **Client Secret**; он показывается один раз.
2. Нажмите **Manage** рядом с приложением, затем **Create Grant** и скопируйте **refresh token**.
3. В Capital вставьте их как **Токен обновления: tastytrade** и **Client secret: tastytrade**, затем выполните шаги из раздела [Подключение счёта](#connect).

Документация tastytrade: [OAuth2 and personal grants](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

Individual API от Public предназначен для ваших собственных счетов. Секретный ключ долгоживущий и отзываемый; при каждом обновлении Capital обменивает его на access-токен со сроком пять минут.

1. В веб-приложении Public откройте страницу **API** в настройках и создайте **секретный ключ**.
2. В Capital вставьте его как **Секретный ключ: Public.com**, затем выполните шаги из раздела [Подключение счёта](#connect).

Документация Public: [Quickstart](https://public.com/api/docs/quickstart) · [Access tokens](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [Portfolio](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

Ключи eToro долгоживущие; для них можно задать дату истечения и список IP-адресов, а также сделать их только для чтения. Ваш счёт eToro должен быть верифицирован.

1. В eToro откройте **Settings → Trading → API Key Management** и нажмите **Create New Key**. Выберите среду **Real**, разрешение **Read**, без списка IP-адресов и при желании дату истечения. Подтвердите кодом из SMS.
2. Скопируйте **Public API Key** и **User Key**; пользовательский ключ показывается один раз.
3. В Capital вставьте их как **Public API key: eToro** и **User key: eToro**, затем выполните шаги из раздела [Подключение счёта](#connect). **Загрузить счета** показывает ваши торговые, денежные и другие счета eToro.

Документация eToro: [Authentication](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [Getting started](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

Токен из личного кабинета Indexa доступен только для чтения. Он привязан к вашему e-mail, паролю и устройству: после смены пароля создайте его заново.

1. В личном кабинете Indexa откройте **Настройки пользователя → Приложения** (User settings → Applications) и скопируйте токен.
2. В Capital вставьте его как **Токен доступа: Indexa Capital**, затем выполните шаги из раздела [Подключение счёта](#connect). Пенсионные и инвестиционные счета показываются вместе.

Indexa оценивает фонды раз в рабочий день; дата наблюдения — это дата такой оценки.

Документация Indexa Capital: [REST API](https://indexacapital.com/en/api-rest-v1) · [Connecting with the API](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

T-Invest API банка T-Bank принимает токен, который вы выпускаете в настройках инвестиций. Токен перестаёт действовать через три месяца после последнего использования и должен быть использован в течение семи дней после выпуска; еженедельное обновление поддерживает его в рабочем состоянии. Выбирайте токен **только для чтения**.

1. Откройте [настройки T-Invest](https://www.tbank.ru/invest/settings/) и выпустите **токен T-Invest API** для биржи с доступом **только для чтения** (все счета или один). Для выпуска подтверждение сделок кодом должно быть отключено. Скопируйте токен; он показывается один раз.
2. В Capital вставьте его как **Токен доступа: T-Invest**, затем выполните шаги из раздела [Подключение счёта](#connect).

T-Bank обслуживает этот API под российским Trusted Root CA, которого нет в Android. Capital доверяет этому сертификату только для адреса T-Invest API (`invest-public-api.tbank.ru`) и ни для каких других подключений.

Документация T-Invest: [Tokens](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

ALOR выдаёт refresh-токен со сроком действия один год; при каждом обновлении Capital обменивает его на access-токен со сроком 30 минут. У ALOR нет токена только для чтения: токен мог бы торговать, а Capital только читает.

1. Войдите на [портал разработчиков ALOR](https://alor.dev), привяжите торговый счёт, откройте **API Access Tokens** и нажмите **Create Token**. Скопируйте refresh-токен.
2. В Capital вставьте его как **Токен обновления: ALOR**, затем выполните шаги из раздела [Подключение счёта](#connect). **Загрузить счета** показывает портфели счёта (фондовый рынок D…, валютный рынок G…, срочный рынок 7500…); добавьте по одному на каждый портфель.

Документация ALOR: [Refresh token](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [Access token](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

Ключи Capital.com действуют один год по умолчанию или до выбранной вами даты. Они дают торговые права (read-only-ключей у Capital.com нет); Capital только читает. У ключа есть собственный пароль, который не совпадает с паролем от вашего аккаунта.

1. Включите двухфакторную аутентификацию, затем откройте **Settings → API integrations** и нажмите **Generate API key**. Задайте метку и **собственный пароль**, оставьте или измените срок действия и подтвердите кодом 2FA. Скопируйте ключ; он показывается один раз.
2. В Capital вставьте **API key: Capital.com**, ваш логин (e-mail) как **Логин (e-mail): Capital.com** и собственный пароль как **Пароль API key: Capital.com**, затем выполните шаги из раздела [Подключение счёта](#connect). Читаются только реальные счета.

Документация Capital.com: [Public API](https://open-api.capital.com/)

## Akahu {#akahu}

[Akahu](https://www.akahu.nz) объединяет банки, инвестиционные платформы и схемы KiwiSaver Новой Зеландии; бесплатное личное приложение читает ваши собственные счета. Akahu обновляет данные примерно раз в сутки.

1. Зарегистрируйтесь на [my.akahu.nz](https://my.akahu.nz) и подключите своих провайдеров (например, Sharesies, Hatch, Kernel, Simplicity, Milford или вашу схему KiwiSaver).
2. Откройте страницу **Developers**, примите условия для разработчиков и скопируйте **App ID Token** и **User Access Token**.
3. В Capital вставьте их как **App ID token: Akahu** и **Токен доступа пользователя: Akahu**, затем выполните шаги из раздела [Подключение счёта](#connect).

Документация Akahu: [Personal apps](https://developers.akahu.nz/docs/personal-apps) · [Accounts](https://developers.akahu.nz/reference/get_accounts) · [Supported providers](https://developers.akahu.nz/docs/integrations)

## Популярные брокеры по рынкам {#by-market}

Как можно подключить самых популярных брокеров на рынках, для которых есть языки Capital, по состоянию на октябрь 2026 года. *Напрямую* означает раздел выше; *SnapTrade* — через [SnapTrade](#snaptrade); в остальных случаях указана причина, по которой счёт нельзя прочитать, а остаток можно вести как актив **Вручную**.

| Рынок | Брокер | Как |
|---|---|---|
| США | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | Напрямую |
| США | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| США | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | Нет публичного API |
| Канада | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| Канада | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | Нет публичного API |
| Великобритания и Ирландия | Trading 212, eToro, Interactive Brokers | Напрямую |
| Великобритания и Ирландия | AJ Bell | SnapTrade |
| Великобритания и Ирландия | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | Нет публичного API |
| Великобритания и Ирландия | IG | Невозможно: для каждой сессии нужен пароль от аккаунта |
| Европа | Indexa Capital (Испания), eToro, Trading 212, Interactive Brokers | Напрямую |
| Европа | DEGIRO, BUX | SnapTrade |
| Европа | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | Нет публичного API для инвестиций |
| Европа | XTB | Невозможно: API закрыт в марте 2025 года |
| Европа | Saxo, comdirect | Невозможно: только короткоживущие токены или сессии с TAN |
| Европа | Bitpanda, Freedom24 | Невозможно: API не возвращает общую стоимость счёта |
| Россия и Казахстан | T-Invest, ALOR | Напрямую |
| Россия и Казахстан | BCS | Невозможно: нет общей стоимости, а токен истекает через 90 дней |
| Россия и Казахстан | Finam | Пока нет: валюта стоимости счёта не документирована |
| Россия и Казахстан | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | Нет публичного API или общей стоимости в нём |
| Индия | Zerodha, Upstox | SnapTrade (правила SEBI завершают API-сессии ежедневно, поэтому подключение нужно часто обновлять) |
| Индия | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | Невозможно: правила SEBI завершают каждую API-сессию ежедневно |
| Пакистан и Бангладеш | Все биржевые брокеры | Нет публичного API |
| Китай, Гонконг и Тайвань | moomoo | SnapTrade |
| Китай, Гонконг и Тайвань | Futu, Tiger Brokers, Longbridge | Пока нет: срок действия ключа или формат ответа не полностью документированы, либо ключи нельзя ограничить чтением |
| Китай, Гонконг и Тайвань | East Money, Huatai, CITIC, Yuanta, Fubon | Нет публичного веб-API (только настольные терминалы или SDK с сертификатами) |
| Япония | OANDA Japan (счета, подходящие для доступа к API) | Напрямую, как OANDA |
| Япония | SBI Securities, Rakuten Securities, Monex, Matsui | Нет публичного API |
| Австралия и Новая Зеландия | CommSec, Stake | SnapTrade |
| Австралия и Новая Зеландия | Sharesies, Hatch, Kernel, Simplicity, схемы KiwiSaver | Akahu (счета в Новой Зеландии) |
| Ближний Восток и Африка | eToro | Напрямую |
| Ближний Восток и Африка | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | Нет публичного API для частных лиц |
| Юго-Восточная Азия | Stockbit, Ajaib, Bibit, IPOT, VPS | Нет публичного API |
| Юго-Восточная Азия | SSI, TCBS, DNSE | Невозможно: 8-часовые токены с одноразовым кодом или только денежный остаток |
| Латинская Америка | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | Нет публичного API для частных лиц или вход только по паролю |
| Форекс и CFD | OANDA, Capital.com | Напрямую |
| Форекс и CFD | Брокеры MetaTrader (XM, Exness, Pepperstone, IC Markets, Admirals) | Невозможно: нет доступа на чтение по HTTPS |
| Форекс и CFD | Брокеры cTrader, FXCM, Forex.com | Невозможно: регистрация приложения, устаревший API или вход по паролю |

## Другие брокеры {#other-brokers}

Capital подключается только к интерфейсам, которые работают с телефона по HTTPS, с токеном, который вы можете создать сами, и которые позволяют читать данные без возможности торговать. Пока это исключает:

- Счета **MetaTrader 4 и 5**. Пароль инвестора даёт доступ только для чтения, но лишь внутри терминала MetaTrader; HTTPS-интерфейса для него брокеры не публикуют.
- Брокеров, чей API требует программу, запущенную на компьютере (например, шлюз Client Portal Web API у Interactive Brokers; Capital использует вместо него Flex Web Service), или регистрации OAuth-приложения.
- Брокеров, чей API выдаёт только короткоживущие токены через OAuth, например Saxo Bank (токены доступа действуют 20 минут; 24-hour token с портала разработчика работает только в среде симуляции).
- Банки и брокеры без публичного API.

Многих из этих брокеров охватывает [SnapTrade](#snaptrade). В остальных случаях вводите остаток как актив **Вручную** и обновляйте число, когда сверяетесь с выпиской. Если ваш брокер предлагает простую HTTPS-конечную точку с токеном, которая читает стоимость счёта, [создайте обращение]({{ site.repo }}/issues) со ссылкой на её документацию. Каждый брокер в Capital — это небольшой плагин; разработчики могут добавить свой, следуя [руководству по плагинам]({{ site.repo }}/blob/main/BROKER-PLUGINS.md).

## Сообщения и что делать {#messages}

| Сообщение | Что делать |
|---|---|
| *Для Interactive Brokers нужны учётные данные на экране «Брокеры»* / *Для OANDA нужны учётные данные …* | Вставьте токен, ключ или client id в разделе «Учётные данные» на вкладке «Брокеры». |
| *Срок действия токена истёк; создайте новый в Client Portal* | Создайте новый токен Flex Web Service и вставьте его. |
| *Токен недействителен* | Скопируйте токен заново; новый токен заменяет старый. |
| *Токен привязан к другому IP-адресу* | Создайте токен без ограничения по IP-адресу. |
| *Некорректный Flex Query id* | Проверьте число; запрос должен быть Activity Flex Query этого логина. |
| *Добавьте в Flex Query раздел Net Asset Value (NAV) Summary in Base с полями Report Date и Total* | Измените запрос и добавьте раздел и поля. |
| *Добавьте в Flex Query поле Currency из раздела Account Information* | Измените запрос и добавьте поле. |
| *Запрос вернул счетов: N; создайте отдельный Flex Query для каждого счёта* | Создайте запрос, который охватывает один счёт. |
| *Отчёт ещё не готов; обновите снова через минуту* | Interactive Brokers ещё формирует отчёт; обновите снова. |
| *Доступ запрещён; проверьте ключ провайдера или квоту* | Токен OANDA неверен или отозван, либо пара ключей Trading 212 неверна или не имеет разрешения Account data. |
| *У SnapTrade пока нет общей стоимости этого счёта; синхронизируйте подключение и повторите* | SnapTrade ещё не синхронизировал брокера; обновите позже. |
| *Счета пока не подключены. Сначала подключите брокера через SnapTrade.* | Откройте Connection Portal из редактора и подключите брокера. |
| *Отрицательная стоимость счёта (…) не поддерживается* | Счёт в минусе; он ничего не добавляет к вашим сбережениям. |
| *tastytrade отклонил токен обновления или client secret; создайте новый grant* | Создайте новый grant для приложения и вставьте его refresh-токен; проверьте client secret. |
| *Capital.com не открыл сессию; проверьте API key, логин и пароль ключа* | Ключ, e-mail или собственный пароль ключа неверны, либо срок действия ключа истёк. |
| *Счёт не найден; выберите его заново* | Брокер больше не показывает этот счёт; откройте его для редактирования и выберите из **Загрузить счета**. |

После любого из этих сообщений прежнее значение остаётся на экране с пометкой об устаревании.
