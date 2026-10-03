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

Capital se conecta solo a interfaces cuyas credenciales son de larga duración: un token o una clave que usted crea una vez y que sigue siendo válida hasta que la revoque (o, en Interactive Brokers, hasta la caducidad que haya elegido, de hasta un año). Compatibles actualmente:

| Bróker | Interfaz utilizada | Qué se lee |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (solo descarga de informes) | Valor liquidativo neto del último día hábil, en la moneda base de la cuenta |
| [OANDA](#oanda) | API REST v20, cuentas reales de fxTrade | Valor liquidativo neto en el momento de la actualización, en la moneda de la cuenta |
| [Trading 212](#trading-212) | API pública, cuentas Invest y Stocks ISA | Valor total de la cuenta en el momento de la actualización, en la moneda principal de la cuenta |
| [SnapTrade](#snaptrade) | SnapTrade Personal, un agregador que cubre muchos brókeres | Valor total de la cuenta tal como el bróker lo comunica a SnapTrade, en la moneda de la cuenta |

## Antes de empezar {#before-you-start}

- **Qué sale del dispositivo.** En cada actualización la aplicación envía su token de acceso y el ID de la cuenta o de la consulta a ese bróker, mediante HTTPS. El bróker ve su dirección IP, como en cualquier solicitud.
- **Dónde se guardan las credenciales.** Ajustes → Cuentas de bróker. Se cifran con una clave guardada en Android Keystore, nunca se escriben en su carpeta de datos y quedan fuera de las exportaciones y de las copias de seguridad del sistema. Un conjunto de credenciales por bróker sirve para todas las cuentas que añada de ese bróker.
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

1. **Ajustes → Cuentas de bróker → Token de acceso: Interactive Brokers**, pegue el token y guarde.
2. Abra el bolsillo, **Añadir posición**, ponga **Seguimiento** en **Cuenta de bróker**, **Bróker** en Interactive Brokers, introduzca el **Flex Query id** y guarde.
3. Pulse **Actualizar**. La primera vez tarda hasta medio minuto porque el informe se genera bajo demanda.

Cuando el token caduca, la actualización informa *El token ha caducado; genere uno nuevo en Client Portal*: genere un token nuevo y péguelo en Ajustes. Interactive Brokers permite una solicitud de informe por segundo y diez por minuto desde un mismo token, límite que una actualización nunca supera.

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

1. **Ajustes → Cuentas de bróker → Token de acceso: OANDA**, pegue el token y guarde.
2. Abra el bolsillo, **Añadir posición**, ponga **Seguimiento** en **Cuenta de bróker**, **Bróker** en OANDA, introduzca el **ID de cuenta de OANDA** y guarde.
3. Pulse **Actualizar**.

Una cuenta de margen cuyo NAV es negativo se notifica como un error en lugar de contarse como ahorros.

Documentación de OANDA: [API REST v20](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital llama al **resumen de la cuenta** (account summary) de la API pública de Trading 212 y guarda el valor total de la cuenta en su moneda principal. La API cubre las cuentas **Invest** y **Stocks ISA**; un par de claves pertenece a una sola cuenta y Capital guarda un único par de claves, así que lee una cuenta de Trading 212.

### 1. Cree la clave de API

1. En la aplicación o el sitio web de Trading 212 abra el menú (**☰**) → **Settings** → **API (Beta)** y acepte la advertencia de riesgo.
2. Pulse **Generate API key**. Dele un nombre, deje solo el permiso **Account data** (lectura) y elija acceso por IP *Unrestricted* (sin restricciones; la dirección de un teléfono cambia).
3. Envíe el formulario. Copie ambos valores: la **API Key** y la **API Secret Key**. El secreto se muestra una sola vez; si lo pierde, elimine la clave y genere un par nuevo.

### 2. Conéctelo en Capital

1. **Ajustes → Cuentas de bróker → Clave de API: Trading 212** y **Secreto de API: Trading 212**, pegue cada valor.
2. Abra el bolsillo, **Añadir posición**, ponga **Seguimiento** en **Cuenta de bróker**, **Bróker** en Trading 212, introduzca el **Número de cuenta de Trading 212** (el ID de cuenta que muestra la aplicación, solo dígitos) y guarde.
3. Pulse **Actualizar**. Trading 212 permite una solicitud de resumen cada 5 segundos.

Documentación de Trading 212: [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) es un agregador: usted conecta una cuenta de bróker a SnapTrade una sola vez y SnapTrade la lee por usted. Cubre muchos brókeres que no tienen una API pública propia. Capital usa **SnapTrade Personal**, el plan gratuito para sus propias cuentas, con su propio ID de cliente y su propia clave de consumidor. En este plan SnapTrade actualiza los datos aproximadamente una vez al día.

Qué se envía: su ID de cliente y, como firma, nada de la clave de consumidor en sí (las solicitudes se firman con ella). SnapTrade, y no Capital, mantiene la conexión con su bróker; a esa conexión se aplican sus condiciones y su política de privacidad.

### 1. Cree la clave de API

1. Regístrese en el [panel de SnapTrade](https://dashboard.snaptrade.com/signup) y elija el plan **Personal**.
2. En el panel cree una clave de API. Copie el **ID de cliente** (client id) y la **clave de consumidor** (consumer key); la clave de consumidor se muestra una sola vez.

### 2. Conéctelo en Capital

1. **Ajustes → Cuentas de bróker → ID de cliente: SnapTrade** y **Clave de consumidor: SnapTrade**, pegue cada valor.
2. Abra el bolsillo, **Añadir posición**, ponga **Seguimiento** en **Cuenta de bróker** y **Bróker** en SnapTrade.
3. Pulse **Conectar un bróker a través de SnapTrade**. El SnapTrade Connection Portal se abre en el navegador; inicie sesión allí en su bróker (el enlace es válido durante 5 minutos). Vuelva a Capital.
4. Pulse **Obtener cuentas** y elija la cuenta; su ID rellena el campo **ID de cuenta de SnapTrade**. Guarde y pulse **Actualizar**.

Una cuenta que SnapTrade aún no ha terminado de sincronizar informa *SnapTrade aún no tiene un valor total para esta cuenta*; actualice de nuevo más tarde.

Documentación de SnapTrade: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Otros brókeres {#other-brokers}

Capital se conecta solo a interfaces que funcionan desde un teléfono mediante HTTPS con un token que usted mismo puede crear y que permiten leer sin poder operar. Eso excluye, por ahora:

- Las cuentas de **MetaTrader 4 y 5**. La contraseña de inversor da acceso de solo lectura, pero únicamente dentro del terminal MetaTrader; los brókeres no publican ninguna interfaz HTTPS para ello.
- Los brókeres cuya API necesita un programa ejecutándose en un ordenador (por ejemplo, la pasarela Client Portal Web API de Interactive Brokers; Capital usa el Flex Web Service en su lugar) o el registro de una aplicación OAuth.
- Los brókeres cuya API emite solo tokens de corta duración mediante OAuth, por ejemplo Saxo Bank (los tokens de acceso duran 20 minutos; el 24-hour token del portal de desarrolladores sirve solo para el entorno de simulación).
- Los bancos y brókeres sin API pública.

Muchos de estos brókeres están cubiertos por [SnapTrade](#snaptrade). En otro caso, introduzca el saldo como una posición **Manual** y actualice la cifra cuando consulte su extracto. Si su bróker ofrece un punto de acceso HTTPS sencillo basado en token que lea el valor de la cuenta, [abra una incidencia]({{ site.repo }}/issues) con un enlace a su documentación.

## Mensajes y qué hacer {#messages}

| Mensaje | Qué hacer |
|---|---|
| *Interactive Brokers necesita su token de acceso en Ajustes → Cuentas de bróker* / *OANDA necesita su token de acceso …* | Pegue el token, la clave o el ID de cliente en Ajustes → Cuentas de bróker. |
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

El valor anterior sigue visible tras cualquiera de estos mensajes, marcado como desactualizado.
