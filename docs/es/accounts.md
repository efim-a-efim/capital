---
layout: default
lang: es
base: "/es"
key: "accounts"
title: Cuentas de bróker y de divisas
class: doc
---
# Cuentas de bróker y de divisas

Capital puede leer el valor total de una cuenta de valores o de divisas del mismo modo que lee una billetera de criptomonedas. Usted añade la cuenta a un bolsillo como una posición de tipo **Cuenta de bróker**, y cada actualización obtiene el valor liquidativo neto de la cuenta (net asset value) en su moneda base. La aplicación solo lee: usa la interfaz de informes del bróker con un token que usted mismo crea, nunca da, modifica ni cancela una orden y nunca mueve dinero.

Capital se conecta solo a interfaces cuyas credenciales son de larga duración: un token o una clave que usted crea una vez y que sigue siendo válida hasta que la revoque, hasta una caducidad que haya elegido, o durante al menos meses (los tokens de T-Invest caducan tras tres meses sin uso; los de ALOR, tras un año). Todos los brókeres de abajo están disponibles sea cual sea el idioma de la aplicación. Compatibles actualmente:

| Bróker | Interfaz utilizada | Qué se lee |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (solo descarga de informes) | Valor liquidativo neto del último día hábil, en la moneda base de la cuenta |
| [OANDA](#oanda) | API REST v20, cuentas reales de fxTrade | Valor liquidativo neto en el momento de la actualización, en la moneda de la cuenta |
| [Trading 212](#trading-212) | API pública, cuentas Invest y Stocks ISA | Valor total de la cuenta en el momento de la actualización, en la moneda principal de la cuenta |
| [SnapTrade](#snaptrade) | SnapTrade Personal, un agregador que cubre muchos brókeres | Valor total de la cuenta tal como el bróker lo comunica a SnapTrade, en la moneda de la cuenta |
| [Alpaca](#alpaca) | Trading API, cuentas reales | Equity (efectivo más posiciones), en dólares estadounidenses |
| [Tradier](#tradier) | Brokerage API | Equity total, en dólares estadounidenses |
| [tastytrade](#tastytrade) | Open API con una autorización OAuth personal | Valor liquidativo neto (net liquidating value), en dólares estadounidenses |
| [Public.com](#public) | API Individual | Valor total de la cuenta, en dólares estadounidenses |
| [eToro](#etoro) | API pública | Saldo de la cuenta elegida (en una cuenta de trading: efectivo más posiciones invertidas), en su moneda |
| [Indexa Capital](#indexa-capital) | API REST, token de solo lectura | Total de la cartera en la última fecha de valoración, en la moneda de la cuenta |
| [T-Invest](#t-invest) | API de T-Invest (T-Bank) | Valor total de la cartera, en rublos |
| [ALOR](#alor) | ALOR OpenAPI | Valoración de la cartera en la Bolsa de Moscú, en rublos |
| [Capital.com](#capital-com) | API pública, cuentas reales | Saldo incluidas las ganancias y pérdidas abiertas, en la moneda de la cuenta |
| [Akahu](#akahu) | Aplicación personal de Akahu, un agregador de Nueva Zelanda | Saldo de una cuenta conectada (Sharesies, Hatch, Kernel, KiwiSaver y otras), en su moneda |

## Antes de empezar {#before-you-start}

- **Qué sale del dispositivo.** En cada actualización la aplicación envía su token de acceso y el ID de la cuenta o de la consulta a ese bróker, mediante HTTPS. El bróker ve su dirección IP, como en cualquier solicitud.
- **Dónde se guardan las credenciales.** Pestaña Brókeres → Credenciales. Se cifran con una clave guardada en Android Keystore, nunca se escriben en su carpeta de datos y quedan fuera de las exportaciones y de las copias de seguridad del sistema. Un conjunto de credenciales por bróker sirve para todas las cuentas que añada de ese bróker.
- **Qué se guarda en su carpeta.** El ID de la cuenta, el último valor leído y cuándo se leyó. Nada más del bróker.
- **Las pantallas del bróker pueden cambiar.** Los pasos siguientes corresponden a los sitios web de los brókeres en octubre de 2026. Los brókeres renombran menús y cambian las opciones de sitio de vez en cuando, así que un paso puede verse algo distinto cuando lo siga. La documentación del propio bróker, enlazada en cada sección, es la fuente de referencia: si un paso de aquí ya no coincide, busque el mismo término en la página del bróker.

## Interactive Brokers {#interactive-brokers}

Capital usa el **Flex Web Service**, la interfaz de Interactive Brokers para descargar informes preconfigurados. El token que usa solo puede generar y descargar informes; no puede iniciar sesión, operar ni retirar fondos. Capital pide el **Net Asset Value (NAV) Summary in Base** (resumen del valor liquidativo neto en la moneda base) de una Activity Flex Query y toma el total de la última fecha del informe, de modo que el valor es el cierre del último día hábil.

### 1. Cree la Flex Query

1. Inicie sesión en [Client Portal](https://www.interactivebrokers.com/portal) y abra **Performance & Reports → Flex Queries** (Rendimiento e informes; en algunas cuentas el menú se llama *Reporting*).
2. En **Activity Flex Query** pulse **+** (Crear). Dé un nombre a la consulta, por ejemplo `Capital`.
3. En la lista **Sections** (Secciones) active exactamente estas dos secciones y campos (seleccionar todos los campos de una sección también funciona):
   - **Account Information**: *Account ID*, *Currency*.
   - **Net Asset Value (NAV) Summary in Base**: *Report Date*, *Total*.
4. En **Delivery Configuration** (Configuración de entrega) ponga **Format** (Formato) en `XML` y **Period** (Periodo) en `Last Business Day`. Las demás opciones pueden quedar con sus valores predeterminados.
5. Guarde la consulta, pulse el icono **i** (información) junto a ella y anote el **Query ID**, un número.

La consulta debe abarcar una sola cuenta. Si tiene cuentas vinculadas o una estructura de asesor, cree una consulta por cuenta y seleccione solo esa cuenta al crearla.

### 2. Active el Flex Web Service y cree el token

1. En la misma página **Flex Queries** abra **Flex Web Service Configuration**.
2. Active **Flex Web Service Status** y guarde. Se crea un token.
3. Para elegir cuánto tiempo sigue siendo válido el token, pulse **Generate New Token**: de 6 horas a 1 año. Deje **Valid for IP address** vacío en un teléfono, cuya dirección cambia. Generar un token nuevo invalida el anterior.
4. Copie el token.

### 3. Conéctelo en Capital

1. **Brókeres → Credenciales → Token de acceso: Interactive Brokers**, pegue el token y guarde.
2. **Brókeres → +**: introduzca un nombre, ponga **Bróker** en Interactive Brokers, introduzca el **Flex Query id** y guarde.
3. Abra el bolsillo, **Añadir posición**, ponga **Seguimiento** en **Cuenta de bróker**, elija la cuenta y guarde.
4. Pulse **Actualizar**. La primera vez tarda hasta medio minuto porque el informe se genera bajo demanda.

Cuando el token caduca, la actualización informa *El token ha caducado; genere uno nuevo en Client Portal*: genere un token nuevo y péguelo en Credenciales. Interactive Brokers permite una solicitud de informe por segundo y diez por minuto desde un mismo token, límite que una actualización nunca supera.

Documentación de Interactive Brokers: [Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [Enable and create the access token](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [Create a Flex Query](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query reference](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

Capital llama al **resumen de la cuenta** (account summary) de la API REST v20 de OANDA y guarda el NAV de la cuenta (saldo más ganancias o pérdidas no realizadas) en la moneda de la cuenta. Solo se admiten cuentas reales de **fxTrade**; las cuentas de práctica no son ahorros.

**Un token de acceso personal de OANDA no es de solo lectura.** Da acceso completo a la API de todas las subcuentas de su inicio de sesión, incluida la operativa. Capital solo llama al resumen de la cuenta, pero quien obtenga el token podría operar con él. Trátelo como una contraseña: péguelo solo en Capital y revóquelo en el portal de OANDA si pierde el teléfono.

### 1. Cree el token

1. Inicie sesión en el portal de gestión de cuentas de OANDA fxTrade.
2. Abra **My Services → Manage API Access** (Mis servicios → Gestionar acceso a la API; en el portal antiguo: *My Account → My Services → Manage API Access*).
3. Acepte la licencia de la API y pulse **Generate**. Copie el token; OANDA no lo vuelve a mostrar. Si lo pierde, revóquelo allí y genere uno nuevo.

### 2. Encuentre el ID de la cuenta

El ID de cuenta v20 tiene la forma `001-001-1234567-001`, con guiones. Aparece en el mismo portal junto a cada subcuenta, y en la plataforma fxTrade en los detalles de la cuenta.

### 3. Conéctelo en Capital

1. **Brókeres → Credenciales → Token de acceso: OANDA**, pegue el token y guarde.
2. **Brókeres → +**: introduzca un nombre, ponga **Bróker** en OANDA, introduzca el **ID de cuenta de OANDA** y guarde.
3. Abra el bolsillo, **Añadir posición**, ponga **Seguimiento** en **Cuenta de bróker**, elija la cuenta y guarde.
4. Pulse **Actualizar**.

Una cuenta de margen cuyo NAV es negativo se notifica como un error en lugar de contarse como ahorros.

Documentación de OANDA: [API REST v20](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital llama al **resumen de la cuenta** (account summary) de la API pública de Trading 212 y guarda el valor total de la cuenta en su moneda principal. La API cubre las cuentas **Invest** y **Stocks ISA**; un par de claves pertenece a una sola cuenta y Capital guarda un único par de claves, así que lee una cuenta de Trading 212.

### 1. Cree la clave de API

1. En la aplicación o el sitio web de Trading 212 abra el menú (**☰**) → **Settings** → **API (Beta)** y acepte la advertencia de riesgo.
2. Pulse **Generate API key**. Dele un nombre, deje solo el permiso **Account data** (lectura) y elija acceso por IP *Unrestricted* (sin restricciones; la dirección de un teléfono cambia).
3. Envíe el formulario. Copie ambos valores: la **API Key** y la **API Secret Key**. El secreto se muestra una sola vez; si lo pierde, elimine la clave y genere un par nuevo.

### 2. Conéctelo en Capital

1. **Brókeres → Credenciales → Clave de API: Trading 212** y **Secreto de API: Trading 212**, pegue cada valor.
2. **Brókeres → +**: introduzca un nombre, ponga **Bróker** en Trading 212, introduzca el **Número de cuenta de Trading 212** (el ID de cuenta que muestra la aplicación, solo dígitos) y guarde.
3. Abra el bolsillo, **Añadir posición**, ponga **Seguimiento** en **Cuenta de bróker**, elija la cuenta y guarde.
4. Pulse **Actualizar**. Trading 212 permite una solicitud de resumen cada 5 segundos.

Documentación de Trading 212: [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) es un agregador: usted conecta una cuenta de bróker a SnapTrade una sola vez y SnapTrade la lee por usted. Cubre muchos brókeres que no tienen una API pública propia. Capital usa **SnapTrade Personal**, el plan gratuito para sus propias cuentas, con su propio ID de cliente y su propia clave de consumidor. En este plan SnapTrade actualiza los datos aproximadamente una vez al día.

Qué se envía: su ID de cliente y, como firma, nada de la clave de consumidor en sí (las solicitudes se firman con ella). SnapTrade, y no Capital, mantiene la conexión con su bróker; a esa conexión se aplican sus condiciones y su política de privacidad.

### 1. Cree la clave de API

1. Regístrese en el [panel de SnapTrade](https://dashboard.snaptrade.com/signup) y elija el plan **Personal**.
2. En el panel cree una clave de API. Copie el **ID de cliente** (client id) y la **clave de consumidor** (consumer key); la clave de consumidor se muestra una sola vez.

### 2. Conéctelo en Capital

1. **Brókeres → Credenciales → ID de cliente: SnapTrade** y **Clave de consumidor: SnapTrade**, pegue cada valor.
2. **Brókeres → +**: introduzca un nombre y ponga **Bróker** en SnapTrade.
3. Pulse **Conectar un bróker a través de SnapTrade**. El SnapTrade Connection Portal se abre en el navegador; inicie sesión allí en su bróker (el enlace es válido durante 5 minutos). Vuelva a Capital.
4. Pulse **Obtener cuentas** y elija la cuenta; su ID rellena el campo **ID de cuenta de SnapTrade**. Guarde.
5. Abra el bolsillo, **Añadir posición**, ponga **Seguimiento** en **Cuenta de bróker**, elija la cuenta y guarde, y después pulse **Actualizar**.

Una cuenta que SnapTrade aún no ha terminado de sincronizar informa *SnapTrade aún no tiene un valor total para esta cuenta*; actualice de nuevo más tarde.

Documentación de SnapTrade: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Conectar una cuenta en Capital {#connect}

Las secciones siguientes explican cómo crear la credencial en cada bróker. En Capital los pasos son los mismos para todos:

1. **Brókeres → Credenciales**: pulse los botones de credenciales del bróker y pegue cada valor.
2. **Brókeres → +**: introduzca un nombre, elija el **Bróker**, pulse **Obtener cuentas**, elija la cuenta (o escriba su ID) y guarde.
3. Abra un bolsillo, **Añadir posición**, ponga **Seguimiento** en **Cuenta de bróker**, elija la cuenta y guarde. Pulse **Actualizar**.

## Alpaca {#alpaca}

Alpaca emite un ID de clave y un secreto por cuenta; siguen siendo válidos hasta que usted los regenere. Solo se leen las cuentas reales: las claves de una cuenta de práctica (paper) no funcionan con la API real.

1. Inicie sesión en el [panel de Alpaca](https://app.alpaca.markets), cambie a su cuenta real y, en la página de inicio, en **API Keys**, pulse **Generate New Keys**.
2. Copie el **API Key ID** y la **Secret Key**; el secreto se muestra una sola vez.
3. En Capital péguelos como **Clave de API: Alpaca** y **Secreto de API: Alpaca**, y siga [Conectar una cuenta](#connect). **Obtener cuentas** muestra el número de cuenta de la clave.

Documentación de Alpaca: [Authentication](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

El token de API de los ajustes de Tradier nunca caduca.

1. Inicie sesión en Tradier y abra [Settings → API Access](https://web.tradier.com/user/api). Copie el **API Access Token** de su cuenta de corretaje (no el token del sandbox).
2. En Capital péguelo como **Token de acceso: Tradier** y siga [Conectar una cuenta](#connect).

Documentación de Tradier: [Authentication](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

tastytrade usa una autorización OAuth personal (personal grant): usted crea una aplicación para sí mismo y una autorización cuyo token de actualización nunca caduca. En cada actualización Capital lo canjea por un token de acceso de 15 minutos.

1. En [my.tastytrade.com](https://my.tastytrade.com) abra **Manage → My Profile → API → OAuth Applications** y pulse **+ New OAuth client**. Dele un nombre, cualquier URI de redirección HTTPS (por ejemplo `https://capital.fimych.dev`) y solo el ámbito **read**. Guarde y copie el **Client Secret**; se muestra una sola vez.
2. Pulse **Manage** junto a la aplicación, después **Create Grant**, y copie el **refresh token**.
3. En Capital péguelos como **Token de actualización: tastytrade** y **Secreto de cliente: tastytrade**, y siga [Conectar una cuenta](#connect).

Documentación de tastytrade: [OAuth2 and personal grants](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

La API Individual de Public está pensada para sus propias cuentas. La clave secreta es de larga duración y revocable; en cada actualización Capital la canjea por un token de acceso de cinco minutos.

1. En la aplicación web de Public abra la página **API** de sus ajustes y genere una **clave secreta** (secret key).
2. En Capital péguela como **Clave secreta: Public.com** y siga [Conectar una cuenta](#connect).

Documentación de Public: [Quickstart](https://public.com/api/docs/quickstart) · [Access tokens](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [Portfolio](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

Las claves de eToro son de larga duración; puede darles una fecha de caducidad y una lista de IP, y puede hacerlas de solo lectura. Su cuenta de eToro debe estar verificada.

1. En eToro abra **Settings → Trading → API Key Management** y pulse **Create New Key**. Elija el entorno **Real**, el permiso **Read**, ninguna lista de IP y, si lo desea, una fecha de caducidad. Confirme con el código SMS.
2. Copie la **Public API Key** y la **User Key**; la clave de usuario se muestra una sola vez.
3. En Capital péguelas como **Clave de API pública: eToro** y **Clave de usuario: eToro**, y siga [Conectar una cuenta](#connect). **Obtener cuentas** muestra sus cuentas de trading, de efectivo y otras cuentas de eToro.

Documentación de eToro: [Authentication](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [Getting started](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

El token del área privada de Indexa es de solo lectura. Está vinculado a su e-mail, su contraseña y su dispositivo: tras cambiar la contraseña, genérelo de nuevo.

1. En el área privada de Indexa abra **Configuración de usuario → Aplicaciones** y copie el token.
2. En Capital péguelo como **Token de acceso: Indexa Capital** y siga [Conectar una cuenta](#connect). Se muestran tanto las cuentas de pensiones como las de inversión.

Indexa valora los fondos una vez por día hábil; la fecha de observación es esa fecha de valoración.

Documentación de Indexa Capital: [REST API](https://indexacapital.com/en/api-rest-v1) · [Conectar con la API](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

La API T-Invest de T-Bank acepta un token que usted emite en los ajustes de inversión. Un token caduca tres meses después de su último uso y debe usarse en los siete días siguientes a su emisión; una actualización semanal lo mantiene vivo. Elija un token de **solo lectura**.

1. Abra los [ajustes de T-Invest](https://www.tbank.ru/invest/settings/) y emita un **token de la API de T-Invest** para la bolsa con acceso de **solo lectura** (todas las cuentas o una). Para emitirlo debe estar desactivada la confirmación de operaciones mediante código. Copie el token; se muestra una sola vez.
2. En Capital péguelo como **Token de acceso: T-Invest** y siga [Conectar una cuenta](#connect).

T-Bank ofrece esta API con el certificado raíz Russian Trusted Root CA, que Android no incluye. Capital confía en ese certificado solo para la dirección de la API de T-Invest (`invest-public-api.tbank.ru`) y para ninguna otra conexión.

Documentación de T-Invest: [Tokens](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

ALOR emite un token de actualización válido durante un año; en cada actualización Capital lo canjea por un token de acceso de 30 minutos. ALOR no ofrece un token de solo lectura: el token podría operar, Capital solo lee.

1. Inicie sesión en el [portal para desarrolladores de ALOR](https://alor.dev), vincule su cuenta de trading, abra **API Access Tokens** y pulse **Create Token**. Copie el token de actualización.
2. En Capital péguelo como **Token de actualización: ALOR** y siga [Conectar una cuenta](#connect). **Obtener cuentas** muestra las carteras de la cuenta (mercado de valores D…, mercado de divisas G…, derivados 7500…); añada una por cartera.

Documentación de ALOR: [Refresh token](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [Access token](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

Las claves de Capital.com son válidas durante un año por defecto, o hasta la fecha que elija. Tienen permisos de operar (Capital.com no ofrece claves de solo lectura); Capital solo lee. Una clave tiene su propia contraseña, que no es la contraseña de su cuenta.

1. Active la autenticación de dos factores, abra **Settings → API integrations** y pulse **Generate API key**. Póngale una etiqueta y una **contraseña personalizada**, mantenga o fije la caducidad y confirme con el código 2FA. Copie la clave; se muestra una sola vez.
2. En Capital pegue **Clave de API: Capital.com**, su e-mail de acceso como **E-mail de acceso: Capital.com** y la contraseña personalizada como **Contraseña de la clave de API: Capital.com**, y siga [Conectar una cuenta](#connect). Solo se leen las cuentas reales.

Documentación de Capital.com: [Public API](https://open-api.capital.com/)

## Akahu {#akahu}

[Akahu](https://www.akahu.nz) conecta bancos, plataformas de inversión y planes KiwiSaver de Nueva Zelanda; una aplicación personal gratuita lee sus propias cuentas. Akahu actualiza los datos aproximadamente una vez al día.

1. Regístrese en [my.akahu.nz](https://my.akahu.nz) y conecte sus proveedores (por ejemplo Sharesies, Hatch, Kernel, Simplicity, Milford o su plan KiwiSaver).
2. Abra la página **Developers**, acepte las condiciones para desarrolladores y copie el **App ID Token** y el **User Access Token**.
3. En Capital péguelos como **Token de ID de app: Akahu** y **Token de acceso de usuario: Akahu**, y siga [Conectar una cuenta](#connect).

Documentación de Akahu: [Personal apps](https://developers.akahu.nz/docs/personal-apps) · [Accounts](https://developers.akahu.nz/reference/get_accounts) · [Supported providers](https://developers.akahu.nz/docs/integrations)

## Brókeres populares por mercado {#by-market}

Cómo se pueden conectar los brókeres más usados en los mercados de los idiomas de Capital, a octubre de 2026. *Directo* significa una sección de arriba; *SnapTrade* significa a través de [SnapTrade](#snaptrade); en otro caso, el motivo por el que no se puede leer, y el saldo puede guardarse como una posición **Manual**.

| Mercado | Bróker | Cómo |
|---|---|---|
| Estados Unidos | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | Directo |
| Estados Unidos | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| Estados Unidos | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | Sin API pública |
| Canadá | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| Canadá | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | Sin API pública |
| Reino Unido e Irlanda | Trading 212, eToro, Interactive Brokers | Directo |
| Reino Unido e Irlanda | AJ Bell | SnapTrade |
| Reino Unido e Irlanda | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | Sin API pública |
| Reino Unido e Irlanda | IG | No es posible: cada sesión exige la contraseña de la cuenta |
| Europa | Indexa Capital (España), eToro, Trading 212, Interactive Brokers | Directo |
| Europa | DEGIRO, BUX | SnapTrade |
| Europa | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | Sin API pública para inversiones |
| Europa | XTB | No es posible: la API se cerró en marzo de 2025 |
| Europa | Saxo, comdirect | No es posible: solo tokens de corta duración o sesiones con TAN |
| Europa | Bitpanda, Freedom24 | No es posible: la API no devuelve el valor total de la cuenta |
| Rusia y Kazajistán | T-Invest, ALOR | Directo |
| Rusia y Kazajistán | BCS | No es posible: no hay valor total y su token caduca a los 90 días |
| Rusia y Kazajistán | Finam | Aún no: la moneda del valor de la cuenta no está documentada |
| Rusia y Kazajistán | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | Sin API pública, o sin valor total en ella |
| India | Zerodha, Upstox | SnapTrade (las normas de la SEBI cierran las sesiones de API cada día, así que la conexión exige renovarse con frecuencia) |
| India | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | No es posible: las normas de la SEBI cierran cada sesión de API a diario |
| Pakistán y Bangladés | Todos los brókeres de bolsa | Sin API pública |
| China, Hong Kong y Taiwán | moomoo | SnapTrade |
| China, Hong Kong y Taiwán | Futu, Tiger Brokers, Longbridge | Aún no: la duración de la clave o el formato de respuesta no están del todo documentados, o las claves no se pueden limitar a lectura |
| China, Hong Kong y Taiwán | East Money, Huatai, CITIC, Yuanta, Fubon | Sin API web pública (solo terminales de escritorio o SDK con certificado) |
| Japón | OANDA Japan (cuentas que cumplen los requisitos de acceso a la API) | Directo, como OANDA |
| Japón | SBI Securities, Rakuten Securities, Monex, Matsui | Sin API pública |
| Australia y Nueva Zelanda | CommSec, Stake | SnapTrade |
| Australia y Nueva Zelanda | Sharesies, Hatch, Kernel, Simplicity, planes KiwiSaver | Akahu (cuentas de Nueva Zelanda) |
| Oriente Medio y África | eToro | Directo |
| Oriente Medio y África | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | Sin API pública para particulares |
| Sudeste Asiático | Stockbit, Ajaib, Bibit, IPOT, VPS | Sin API pública |
| Sudeste Asiático | SSI, TCBS, DNSE | No es posible: tokens de 8 horas con código de un solo uso, o solo saldo en efectivo |
| Latinoamérica | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | Sin API pública para particulares, o inicios de sesión solo con contraseña |
| Forex y CFD | OANDA, Capital.com | Directo |
| Forex y CFD | Brókeres de MetaTrader (XM, Exness, Pepperstone, IC Markets, Admirals) | No es posible: sin acceso de lectura por HTTPS |
| Forex y CFD | Brókeres de cTrader, FXCM, Forex.com | No es posible: registro de aplicación, API obsoleta o inicios de sesión con contraseña |

## Otros brókeres {#other-brokers}

Capital se conecta solo a interfaces que funcionan desde un teléfono mediante HTTPS con un token que usted mismo puede crear y que permiten leer sin poder operar. Eso excluye, por ahora:

- Las cuentas de **MetaTrader 4 y 5**. La contraseña de inversor da acceso de solo lectura, pero únicamente dentro del terminal MetaTrader; los brókeres no publican ninguna interfaz HTTPS para ello.
- Los brókeres cuya API necesita un programa ejecutándose en un ordenador (por ejemplo, la pasarela Client Portal Web API de Interactive Brokers; Capital usa el Flex Web Service en su lugar) o el registro de una aplicación OAuth.
- Los brókeres cuya API emite solo tokens de corta duración mediante OAuth, por ejemplo Saxo Bank (los tokens de acceso duran 20 minutos; el 24-hour token del portal de desarrolladores sirve solo para el entorno de simulación).
- Los bancos y brókeres sin API pública.

Muchos de estos brókeres están cubiertos por [SnapTrade](#snaptrade). En otro caso, introduzca el saldo como una posición **Manual** y actualice la cifra cuando consulte su extracto. Si su bróker ofrece un punto de acceso HTTPS sencillo basado en token que lea el valor de la cuenta, [abra una incidencia]({{ site.repo }}/issues) con un enlace a su documentación. Cada bróker de Capital es un pequeño complemento; los desarrolladores pueden añadir uno siguiendo [la guía de complementos]({{ site.repo }}/blob/main/BROKER-PLUGINS.md).

## Mensajes y qué hacer {#messages}

| Mensaje | Qué hacer |
|---|---|
| *Interactive Brokers necesita sus credenciales en la pantalla Brókeres* / *OANDA necesita sus credenciales …* | Pegue el token, la clave o el ID de cliente en Credenciales, en la pestaña Brókeres. |
| *El token ha caducado; genere uno nuevo en Client Portal* | Genere un token nuevo del Flex Web Service y péguelo. |
| *El token no es válido* | Copie el token de nuevo; un token nuevo sustituye al anterior. |
| *El token está restringido a otra dirección IP* | Genere el token sin restricción de IP. |
| *El Flex Query id no es válido* | Compruebe el número; la consulta debe ser una Activity Flex Query de este inicio de sesión. |
| *Añada a la Flex Query la sección Net Asset Value (NAV) Summary in Base con Report Date y Total* | Edite la consulta y añada la sección y los campos. |
| *Añada a la Flex Query el campo Currency de Account Information* | Edite la consulta y añada el campo. |
| *La consulta devolvió N cuentas; cree una Flex Query por cuenta* | Cree una consulta que abarque una sola cuenta. |
| *El informe aún no está listo; actualice de nuevo en un minuto* | Interactive Brokers sigue generando el informe; actualice de nuevo. |
| *Acceso denegado; compruebe la clave del proveedor o la cuota* | El token de OANDA es incorrecto o fue revocado, o el par de claves de Trading 212 es incorrecto o carece del permiso Account data. |
| *SnapTrade aún no tiene un valor total para esta cuenta; sincronice la conexión y reintente* | SnapTrade aún no ha sincronizado el bróker; actualice de nuevo más tarde. |
| *Aún no hay cuentas conectadas. Conecte primero un bróker a través de SnapTrade.* | Abra el Connection Portal desde el editor y conecte un bróker. |
| *El valor de cuenta negativo … no es compatible* | La cuenta está en descubierto; no suma nada a sus ahorros. |
| *tastytrade rechazó el token de actualización o el secreto de cliente; cree una nueva autorización* | Cree una nueva autorización para la aplicación y pegue su token de actualización; compruebe el secreto de cliente. |
| *Capital.com no abrió una sesión; compruebe la clave de API, el inicio de sesión y la contraseña de la clave* | La clave, el e-mail o la contraseña personalizada de la clave son incorrectos, o la clave ha caducado. |
| *Cuenta no encontrada; elíjala de nuevo* | El bróker ya no muestra esta cuenta; edítela y elíjala en **Obtener cuentas**. |

El valor anterior sigue visible tras cualquiera de estos mensajes, marcado como desactualizado.
