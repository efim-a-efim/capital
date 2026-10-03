---
layout: default
lang: de
base: "/de"
key: "accounts"
title: Broker- und Forex-Konten
class: doc
---
# Broker- und Forex-Konten

Capital kann den Gesamtwert eines Depot- oder Forex-Kontos genauso lesen wie eine Krypto-Wallet. Sie fügen das Konto einem Topf als Position vom Typ **Brokerkonto** hinzu, und jede Aktualisierung ruft den Nettovermögenswert (Net Asset Value, NAV) des Kontos in dessen Basiswährung ab. Die App liest nur: Sie nutzt die Reporting-Schnittstelle des Brokers mit einem Token, das Sie selbst erstellen, sie erteilt, ändert oder storniert niemals eine Order und bewegt niemals Geld.

Capital verbindet sich nur mit Schnittstellen, deren Zugangsdaten langlebig sind: ein Token oder Schlüssel, den Sie einmal erstellen und der gültig bleibt, bis Sie ihn widerrufen, bis zu einem von Ihnen gewählten Ablauf oder mindestens monatelang (T-Invest-Token verfallen nach drei Monaten ohne Nutzung, ALOR-Token nach einem Jahr). Jeder Broker unten ist verfügbar, unabhängig davon, auf welche Sprache die App eingestellt ist. Derzeit unterstützt:

| Broker | Verwendete Schnittstelle | Was gelesen wird |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (nur Berichtsabruf) | Nettovermögenswert am letzten Geschäftstag, in der Basiswährung des Kontos |
| [OANDA](#oanda) | v20 REST API, fxTrade-Live-Konten | Nettovermögenswert zum Zeitpunkt der Aktualisierung, in der Währung des Kontos |
| [Trading 212](#trading-212) | Public API, Invest- und Stocks-ISA-Konten | Gesamtwert des Kontos zum Zeitpunkt der Aktualisierung, in der Hauptwährung des Kontos |
| [SnapTrade](#snaptrade) | SnapTrade Personal, ein Aggregator für viele Broker | Gesamtwert des Kontos, wie der Broker ihn an SnapTrade meldet, in der Währung des Kontos |
| [Alpaca](#alpaca) | Trading API, Live-Konten | Equity (Barmittel plus Positionen), in US-Dollar |
| [Tradier](#tradier) | Brokerage API | Gesamtes Eigenkapital, in US-Dollar |
| [tastytrade](#tastytrade) | Open API mit persönlichem OAuth-Grant | Net Liquidating Value, in US-Dollar |
| [Public.com](#public) | Individual API | Gesamtwert des Kontos, in US-Dollar |
| [eToro](#etoro) | Public API | Saldo des gewählten Kontos (bei einem Handelskonto: Barmittel plus investierte Positionen), in dessen Währung |
| [Indexa Capital](#indexa-capital) | REST API, schreibgeschütztes Token | Portfoliosumme zum letzten Bewertungsstichtag, in der Währung des Kontos |
| [T-Invest](#t-invest) | T-Invest API (T-Bank) | Gesamtwert des Portfolios, in Rubel |
| [ALOR](#alor) | ALOR OpenAPI | Portfoliobewertung an der Moskauer Börse, in Rubel |
| [Capital.com](#capital-com) | Public API, Live-Konten | Saldo einschließlich offenem Gewinn und Verlust, in der Währung des Kontos |
| [Akahu](#akahu) | Akahu Personal App, ein Aggregator in Neuseeland | Saldo eines verbundenen Kontos (Sharesies, Hatch, Kernel, KiwiSaver und andere), in dessen Währung |

## Bevor Sie beginnen {#before-you-start}

- **Was das Gerät verlässt.** Bei jeder Aktualisierung sendet die App Ihr Zugriffstoken und die Konto- oder Query-ID per HTTPS an diesen Broker. Der Broker sieht Ihre IP-Adresse, wie bei jeder Anfrage.
- **Wo die Zugangsdaten liegen.** Tab „Broker“ → Zugangsdaten. Sie werden mit einem im Android Keystore gehaltenen Schlüssel verschlüsselt, nie in Ihren Datenordner geschrieben und von Exporten und Systembackups ausgeschlossen. Ein Satz Zugangsdaten pro Broker gilt für alle Konten, die Sie für diesen Broker hinzufügen.
- **Was in Ihrem Ordner gespeichert wird.** Die Konto-ID, der zuletzt gelesene Wert und der Zeitpunkt des Lesens. Sonst nichts vom Broker.
- **Die Oberflächen der Broker können sich ändern.** Die folgenden Schritte entsprechen den Websites der Broker mit Stand Oktober 2026. Broker benennen Menüs um und verschieben Einstellungen von Zeit zu Zeit, daher kann ein Schritt beim Nachvollziehen etwas anders aussehen. Maßgeblich ist die eigene Dokumentation des Brokers, die in jedem Abschnitt verlinkt ist: Stimmt ein Schritt hier nicht mehr, suchen Sie auf der Seite des Brokers nach demselben Begriff.

## Interactive Brokers {#interactive-brokers}

Capital nutzt den **Flex Web Service**, die Schnittstelle von Interactive Brokers zum Abruf vorkonfigurierter Berichte. Das dafür verwendete Token kann nur Berichte erzeugen und herunterladen; es kann sich nicht anmelden, handeln oder abheben. Capital fordert die **Net Asset Value (NAV) Summary in Base** einer Activity Flex Query an und nimmt die Summe des neuesten Berichtsdatums, der Wert ist also der Schlusswert des letzten Geschäftstags.

### 1. Flex Query erstellen

1. Melden Sie sich im [Client Portal](https://www.interactivebrokers.com/portal) an und öffnen Sie **Performance & Reports → Flex Queries** (Leistung und Berichte; bei manchen Konten heißt das Menü *Reporting*).
2. Klicken Sie unter **Activity Flex Query** auf **+** (Erstellen). Geben Sie der Query einen Namen, zum Beispiel `Capital`.
3. Aktivieren Sie in der Liste **Sections** (Abschnitte) genau diese zwei Abschnitte und Felder (alle Felder eines Abschnitts auszuwählen funktioniert ebenfalls):
   - **Account Information**: *Account ID*, *Currency*.
   - **Net Asset Value (NAV) Summary in Base**: *Report Date*, *Total*.
4. Stellen Sie in **Delivery Configuration** (Lieferkonfiguration) **Format** auf `XML` und **Period** (Zeitraum) auf `Last Business Day`. Die übrigen Optionen können die Standardwerte behalten.
5. Speichern Sie die Query, klicken Sie dann auf das **i**-Symbol (Information) daneben und notieren Sie die **Query ID**, eine Zahl.

Die Query muss ein Konto abdecken. Wenn Sie verknüpfte Konten oder eine Berater-Struktur haben, erstellen Sie pro Konto eine Query und wählen Sie beim Erstellen nur dieses Konto aus.

### 2. Flex Web Service aktivieren und Token erstellen

1. Öffnen Sie auf derselben Seite **Flex Queries** die **Flex Web Service Configuration**.
2. Schalten Sie den **Flex Web Service Status** ein und speichern Sie. Ein Token wird erstellt.
3. Um die Gültigkeitsdauer des Tokens zu wählen, klicken Sie auf **Generate New Token** (Neues Token erzeugen): zwischen 6 Stunden und 1 Jahr. Lassen Sie **Valid for IP address** (Gültig für IP-Adresse) für ein Smartphone leer, dessen Adresse wechselt. Ein neu erzeugtes Token macht das vorherige ungültig.
4. Kopieren Sie das Token.

### 3. In Capital verbinden

1. **Broker → Zugangsdaten → Zugriffstoken: Interactive Brokers**, fügen Sie das Token ein und speichern Sie.
2. **Broker → +**: Geben Sie einen Namen ein, stellen Sie **Broker** auf Interactive Brokers, geben Sie die **Flex-Query-ID** ein und speichern Sie.
3. Öffnen Sie den Topf, **Position hinzufügen**, stellen Sie **Erfassung** auf **Brokerkonto**, wählen Sie das Konto und speichern Sie.
4. Tippen Sie auf **Aktualisieren**. Der erste Durchlauf dauert bis zu einer halben Minute, weil der Bericht auf Anforderung erzeugt wird.

Läuft das Token ab, meldet die Aktualisierung *Token ist abgelaufen; erzeugen Sie im Client Portal einen neuen*: Erzeugen Sie ein neues Token und fügen Sie es unter Zugangsdaten ein. Interactive Brokers erlaubt pro Token eine Berichtsanfrage pro Sekunde und zehn pro Minute; eine Aktualisierung überschreitet das nie.

Dokumentation von Interactive Brokers: [Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [Enable and create the access token](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [Create a Flex Query](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query reference](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

Capital ruft die **Kontozusammenfassung** (account summary) der OANDA v20 REST API ab und speichert den NAV des Kontos (Saldo plus nicht realisierter Gewinn oder Verlust) in der Währung des Kontos. Unterstützt werden nur fxTrade-Live-Konten; Übungskonten sind keine Ersparnisse.

**Ein persönliches OANDA-Zugriffstoken ist nicht schreibgeschützt.** Es gewährt vollen API-Zugriff auf jedes Unterkonto Ihres Logins, einschließlich Handel. Capital ruft ausschließlich die Kontozusammenfassung ab, aber jeder, der das Token erlangt, könnte damit handeln. Behandeln Sie es wie ein Passwort: Fügen Sie es nur in Capital ein und widerrufen Sie es im OANDA-Portal, wenn Sie das Smartphone verlieren.

### 1. Token erstellen

1. Melden Sie sich im Kontoverwaltungsportal Ihres OANDA-fxTrade-Kontos an.
2. Öffnen Sie **My Services → Manage API Access** (Meine Dienste → API-Zugang verwalten; im älteren Portal: *My Account → My Services → Manage API Access*).
3. Akzeptieren Sie die API-Lizenz und klicken Sie auf **Generate** (Erzeugen). Kopieren Sie das Token; OANDA zeigt es nicht erneut an. Haben Sie es verloren, widerrufen Sie es dort und erzeugen Sie ein neues.

### 2. Konto-ID finden

Die v20-Konto-ID hat die Form `001-001-1234567-001`, mit Bindestrichen. Sie steht im selben Portal neben jedem Unterkonto und auf der fxTrade-Plattform in den Kontodetails.

### 3. In Capital verbinden

1. **Broker → Zugangsdaten → Zugriffstoken: OANDA**, fügen Sie das Token ein und speichern Sie.
2. **Broker → +**: Geben Sie einen Namen ein, stellen Sie **Broker** auf OANDA, geben Sie die **OANDA-Konto-ID** ein und speichern Sie.
3. Öffnen Sie den Topf, **Position hinzufügen**, stellen Sie **Erfassung** auf **Brokerkonto**, wählen Sie das Konto und speichern Sie.
4. Tippen Sie auf **Aktualisieren**.

Ein Margin-Konto mit negativem NAV wird als Fehler gemeldet und nicht als Ersparnis gezählt.

OANDA-Dokumentation: [v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital ruft die **Kontozusammenfassung** (account summary) der Trading 212 Public API ab und speichert den Gesamtwert des Kontos in der Hauptwährung des Kontos. Die API deckt **Invest**- und **Stocks ISA**-Konten ab; ein Schlüsselpaar gehört zu einem Konto, und Capital hält ein Schlüsselpaar, liest also ein Trading-212-Konto.

### 1. API-Schlüssel erstellen

1. Öffnen Sie in der Trading-212-App oder auf der Website das Menü (**☰**) → **Settings** (Einstellungen) → **API (Beta)** und akzeptieren Sie den Risikohinweis.
2. Klicken Sie auf **Generate API key** (API-Schlüssel erzeugen). Geben Sie ihm einen Namen, behalten Sie nur die Berechtigung **Account data** (Lesen) und wählen Sie *Unrestricted* (unbeschränkten) IP-Zugriff (die Adresse eines Smartphones wechselt).
3. Senden Sie das Formular ab. Kopieren Sie beide Werte: den **API Key** und den **API Secret Key**. Das Secret wird nur einmal angezeigt; haben Sie es verloren, löschen Sie den Schlüssel und erzeugen Sie ein neues Paar.

### 2. In Capital verbinden

1. **Broker → Zugangsdaten → API-Schlüssel: Trading 212** und **API-Secret: Trading 212**, fügen Sie jeden Wert ein.
2. **Broker → +**: Geben Sie einen Namen ein, stellen Sie **Broker** auf Trading 212, geben Sie die **Trading-212-Kontonummer** ein (die in der App angezeigte Konto-ID, nur Ziffern) und speichern Sie.
3. Öffnen Sie den Topf, **Position hinzufügen**, stellen Sie **Erfassung** auf **Brokerkonto**, wählen Sie das Konto und speichern Sie.
4. Tippen Sie auf **Aktualisieren**. Trading 212 erlaubt eine Zusammenfassungsanfrage alle 5 Sekunden.

Dokumentation von Trading 212: [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) ist ein Aggregator: Sie verbinden ein Depotkonto einmal mit SnapTrade, und SnapTrade liest es für Sie. Es deckt viele Broker ab, die keine eigene öffentliche API haben. Capital nutzt **SnapTrade Personal**, den kostenlosen Tarif für eigene Konten, mit Ihrer eigenen Client-ID und Ihrem eigenen Consumer Key. Die Daten dieses Tarifs aktualisiert SnapTrade etwa einmal täglich.

Was gesendet wird: Ihre Client-ID und als Signatur, aber nicht der Consumer Key selbst (Anfragen werden damit signiert). SnapTrade, nicht Capital, hält die Verbindung zu Ihrem Broker; für diese Verbindung gelten dessen Bedingungen und Datenschutzerklärung.

### 1. API-Schlüssel erstellen

1. Registrieren Sie sich im [SnapTrade-Dashboard](https://dashboard.snaptrade.com/signup) und wählen Sie den Tarif **Personal**.
2. Erstellen Sie im Dashboard einen API-Schlüssel. Kopieren Sie die **Client-ID** und den **Consumer Key**; der Consumer Key wird nur einmal angezeigt.

### 2. In Capital verbinden

1. **Broker → Zugangsdaten → Client-ID: SnapTrade** und **Consumer Key: SnapTrade**, fügen Sie jeden Wert ein.
2. **Broker → +**: Geben Sie einen Namen ein und stellen Sie **Broker** auf SnapTrade.
3. Tippen Sie auf **Broker über SnapTrade verbinden**. Das SnapTrade Connection Portal öffnet sich im Browser; melden Sie sich dort bei Ihrem Broker an (der Link ist 5 Minuten gültig). Kehren Sie zu Capital zurück.
4. Tippen Sie auf **Konten abrufen** und wählen Sie das Konto; seine ID füllt das Feld **SnapTrade-Konto-ID**. Speichern Sie.
5. Öffnen Sie den Topf, **Position hinzufügen**, stellen Sie **Erfassung** auf **Brokerkonto**, wählen Sie das Konto und speichern Sie, dann **Aktualisieren**.

Ein Konto, das SnapTrade noch nicht fertig synchronisiert hat, meldet *SnapTrade hat für dieses Konto noch keinen Gesamtwert*; aktualisieren Sie später erneut.

SnapTrade-Dokumentation: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Ein Konto in Capital verbinden {#connect}

Die folgenden Abschnitte beschreiben, wie Sie die Zugangsdaten beim jeweiligen Broker erstellen. In Capital sind die Schritte für alle gleich:

1. **Broker → Zugangsdaten**: Tippen Sie auf die Schaltflächen des Brokers und fügen Sie jeden Wert ein.
2. **Broker → +**: Geben Sie einen Namen ein, wählen Sie den **Broker**, tippen Sie dann auf **Konten abrufen**, wählen Sie das Konto (oder geben Sie seine ID ein) und speichern Sie.
3. Öffnen Sie einen Topf, **Position hinzufügen**, stellen Sie **Erfassung** auf **Brokerkonto**, wählen Sie das Konto und speichern Sie. Tippen Sie auf **Aktualisieren**.

## Alpaca {#alpaca}

Alpaca vergibt pro Konto eine Schlüssel-ID und ein Secret; sie bleiben gültig, bis Sie sie neu erzeugen. Gelesen werden nur Live-Konten: Schlüssel eines Paper-Kontos funktionieren nicht mit der Live-API.

1. Melden Sie sich im [Alpaca-Dashboard](https://app.alpaca.markets) an, wechseln Sie zu Ihrem Live-Konto und tippen Sie auf der Startseite unter **API Keys** auf **Generate New Keys**.
2. Kopieren Sie die **API Key ID** und den **Secret Key**; das Secret wird nur einmal angezeigt.
3. Fügen Sie sie in Capital als **API-Schlüssel: Alpaca** und **API-Secret: Alpaca** ein und folgen Sie dann [Ein Konto verbinden](#connect). **Konten abrufen** zeigt die Kontonummer des Schlüssels.

Alpaca-Dokumentation: [Authentication](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

Das API-Token aus Ihren Tradier-Einstellungen läuft nie ab.

1. Melden Sie sich bei Tradier an und öffnen Sie [Settings → API Access](https://web.tradier.com/user/api). Kopieren Sie das **API Access Token** Ihres Brokerage-Kontos (nicht das Sandbox-Token).
2. Fügen Sie es in Capital als **Zugriffstoken: Tradier** ein und folgen Sie dann [Ein Konto verbinden](#connect).

Tradier-Dokumentation: [Authentication](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

tastytrade verwendet einen persönlichen OAuth-Grant: Sie erstellen eine Anwendung für sich selbst und einen Grant, dessen Refresh-Token nie abläuft. Capital tauscht es bei jeder Aktualisierung gegen ein 15-Minuten-Zugriffstoken.

1. Öffnen Sie auf [my.tastytrade.com](https://my.tastytrade.com) **Manage → My Profile → API → OAuth Applications** und tippen Sie auf **+ New OAuth client**. Geben Sie einen Namen, eine beliebige HTTPS-Redirect-URI (zum Beispiel `https://capital.fimych.dev`) und nur den Scope **read** an. Speichern Sie und kopieren Sie das **Client Secret**; es wird nur einmal angezeigt.
2. Tippen Sie neben der Anwendung auf **Manage**, dann auf **Create Grant**, und kopieren Sie das **Refresh-Token**.
3. Fügen Sie sie in Capital als **Refresh-Token: tastytrade** und **Client-Secret: tastytrade** ein und folgen Sie dann [Ein Konto verbinden](#connect).

tastytrade-Dokumentation: [OAuth2 and personal grants](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

Die Individual API von Public ist für Ihre eigenen Konten gedacht. Der Secret Key ist langlebig und widerrufbar; Capital tauscht ihn bei jeder Aktualisierung gegen ein Fünf-Minuten-Zugriffstoken.

1. Öffnen Sie in der Web-App von Public in Ihren Einstellungen die Seite **API** und erzeugen Sie einen **Secret Key**.
2. Fügen Sie ihn in Capital als **Secret Key: Public.com** ein und folgen Sie dann [Ein Konto verbinden](#connect).

Public-Dokumentation: [Quickstart](https://public.com/api/docs/quickstart) · [Access tokens](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [Portfolio](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

eToro-Schlüssel sind langlebig; Sie können ihnen ein Ablaufdatum und eine IP-Liste geben und sie schreibgeschützt machen. Ihr eToro-Konto muss verifiziert sein.

1. Öffnen Sie in eToro **Settings → Trading → API Key Management** und tippen Sie auf **Create New Key**. Wählen Sie die Umgebung **Real**, die Berechtigung **Read**, keine IP-Liste und optional ein Ablaufdatum. Bestätigen Sie mit dem SMS-Code.
2. Kopieren Sie den **Public API Key** und den **User Key**; der User Key wird nur einmal angezeigt.
3. Fügen Sie sie in Capital als **Öffentlicher API-Schlüssel: eToro** und **Nutzerschlüssel: eToro** ein und folgen Sie dann [Ein Konto verbinden](#connect). **Konten abrufen** listet Ihre Handels-, Cash- und sonstigen eToro-Konten auf.

eToro-Dokumentation: [Authentication](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [Getting started](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

Das Token aus dem privaten Bereich von Indexa ist schreibgeschützt. Es ist an Ihre E-Mail-Adresse, Ihr Passwort und Ihr Gerät gebunden: Erzeugen Sie es nach einer Passwortänderung neu.

1. Öffnen Sie im privaten Bereich von Indexa **Benutzereinstellungen → Anwendungen** und kopieren Sie das Token.
2. Fügen Sie es in Capital als **Zugriffstoken: Indexa Capital** ein und folgen Sie dann [Ein Konto verbinden](#connect). Renten- und Anlagekonten werden beide aufgelistet.

Indexa bewertet Fonds einmal pro Geschäftstag; das Beobachtungsdatum ist dieser Bewertungsstichtag.

Indexa-Capital-Dokumentation: [REST API](https://indexacapital.com/en/api-rest-v1) · [Connecting with the API](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

Die T-Invest API von T-Bank akzeptiert ein Token, das Sie in den Anlageeinstellungen ausstellen. Ein Token verfällt drei Monate nach der letzten Nutzung und muss innerhalb von sieben Tagen nach der Ausstellung verwendet werden; eine wöchentliche Aktualisierung hält es am Leben. Wählen Sie ein **schreibgeschütztes** Token.

1. Öffnen Sie die [T-Invest-Einstellungen](https://www.tbank.ru/invest/settings/) und stellen Sie ein **T-Invest-API-Token** für die Börse mit **schreibgeschütztem** Zugriff aus (alle Konten oder eines). Die Bestätigung von Geschäften per Code muss dafür ausgeschaltet sein. Kopieren Sie das Token; es wird nur einmal angezeigt.
2. Fügen Sie es in Capital als **Zugriffstoken: T-Invest** ein und folgen Sie dann [Ein Konto verbinden](#connect).

T-Bank stellt diese API unter der Russian Trusted Root CA bereit, die Android nicht enthält. Capital vertraut diesem Zertifikat nur für die Adresse der T-Invest API (`invest-public-api.tbank.ru`) und für keine andere Verbindung.

T-Invest-Dokumentation: [Tokens](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

ALOR stellt ein Refresh-Token aus, das ein Jahr gültig ist; Capital tauscht es bei jeder Aktualisierung gegen ein 30-Minuten-Zugriffstoken. ALOR bietet kein schreibgeschütztes Token: Das Token könnte handeln, Capital liest nur.

1. Melden Sie sich im [ALOR-Entwicklerportal](https://alor.dev) an, verknüpfen Sie Ihr Handelskonto, öffnen Sie **API Access Tokens** und tippen Sie auf **Create Token**. Kopieren Sie das Refresh-Token.
2. Fügen Sie es in Capital als **Refresh-Token: ALOR** ein und folgen Sie dann [Ein Konto verbinden](#connect). **Konten abrufen** listet die Portfolios des Kontos auf (Aktienmarkt D…, Devisenmarkt G…, Derivate 7500…); fügen Sie pro Portfolio ein Konto hinzu.

ALOR-Dokumentation: [Refresh token](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [Access token](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

Capital.com-Schlüssel sind standardmäßig ein Jahr gültig oder bis zu dem Datum, das Sie wählen. Sie tragen Handelsrechte (Capital.com hat keine schreibgeschützten Schlüssel); Capital liest nur. Ein Schlüssel hat ein eigenes Passwort, das nicht Ihr Kontopasswort ist.

1. Schalten Sie die Zwei-Faktor-Authentifizierung ein, öffnen Sie dann **Settings → API integrations** und tippen Sie auf **Generate API key**. Geben Sie eine Bezeichnung und ein **eigenes Passwort** an, behalten oder setzen Sie das Ablaufdatum und bestätigen Sie mit dem 2FA-Code. Kopieren Sie den Schlüssel; er wird nur einmal angezeigt.
2. Fügen Sie in Capital **API-Schlüssel: Capital.com**, Ihre Login-E-Mail als **Login-E-Mail: Capital.com** und das eigene Passwort als **API-Schlüssel-Passwort: Capital.com** ein und folgen Sie dann [Ein Konto verbinden](#connect). Gelesen werden nur Live-Konten.

Capital.com-Dokumentation: [Public API](https://open-api.capital.com/)

## Akahu {#akahu}

[Akahu](https://www.akahu.nz) verbindet neuseeländische Banken, Anlageplattformen und KiwiSaver-Programme; eine kostenlose persönliche App liest Ihre eigenen Konten. Akahu aktualisiert die Daten etwa einmal täglich.

1. Registrieren Sie sich auf [my.akahu.nz](https://my.akahu.nz) und verbinden Sie Ihre Anbieter (zum Beispiel Sharesies, Hatch, Kernel, Simplicity, Milford oder Ihr KiwiSaver-Programm).
2. Öffnen Sie die Seite **Developers**, akzeptieren Sie die Entwicklerbedingungen und kopieren Sie das **App ID Token** und das **User Access Token**.
3. Fügen Sie sie in Capital als **App-ID-Token: Akahu** und **Nutzer-Zugriffstoken: Akahu** ein und folgen Sie dann [Ein Konto verbinden](#connect).

Akahu-Dokumentation: [Personal apps](https://developers.akahu.nz/docs/personal-apps) · [Accounts](https://developers.akahu.nz/reference/get_accounts) · [Supported providers](https://developers.akahu.nz/docs/integrations)

## Beliebte Broker nach Markt {#by-market}

Wie sich die meistgenutzten Broker in den Märkten der Sprachen von Capital verbinden lassen, Stand Oktober 2026. *Direkt* bedeutet einen Abschnitt oben; *SnapTrade* bedeutet über [SnapTrade](#snaptrade); andernfalls steht der Grund, warum sich das Konto nicht lesen lässt, und das Guthaben lässt sich als **manuelle** Position erfassen.

| Markt | Broker | Wie |
|---|---|---|
| USA | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | Direkt |
| USA | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| USA | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | Keine öffentliche API |
| Kanada | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| Kanada | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | Keine öffentliche API |
| Vereinigtes Königreich und Irland | Trading 212, eToro, Interactive Brokers | Direkt |
| Vereinigtes Königreich und Irland | AJ Bell | SnapTrade |
| Vereinigtes Königreich und Irland | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | Keine öffentliche API |
| Vereinigtes Königreich und Irland | IG | Nicht möglich: jede Sitzung erfordert das Kontopasswort |
| Europa | Indexa Capital (Spanien), eToro, Trading 212, Interactive Brokers | Direkt |
| Europa | DEGIRO, BUX | SnapTrade |
| Europa | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | Keine öffentliche API für Geldanlagen |
| Europa | XTB | Nicht möglich: die API wurde im März 2025 eingestellt |
| Europa | Saxo, comdirect | Nicht möglich: nur kurzlebige Token oder TAN-Sitzungen |
| Europa | Bitpanda, Freedom24 | Nicht möglich: die API liefert keinen Gesamtwert des Kontos |
| Russland und Kasachstan | T-Invest, ALOR | Direkt |
| Russland und Kasachstan | BCS | Nicht möglich: kein Gesamtwert, und das Token läuft nach 90 Tagen ab |
| Russland und Kasachstan | Finam | Noch nicht: die Währung des Kontowerts ist nicht dokumentiert |
| Russland und Kasachstan | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | Keine öffentliche API, oder darin kein Gesamtwert |
| Indien | Zerodha, Upstox | SnapTrade (SEBI-Regeln beenden API-Sitzungen täglich, die Verbindung muss daher häufig erneuert werden) |
| Indien | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | Nicht möglich: SEBI-Regeln beenden jede API-Sitzung täglich |
| Pakistan und Bangladesch | Alle Börsenmakler | Keine öffentliche API |
| China, Hongkong und Taiwan | moomoo | SnapTrade |
| China, Hongkong und Taiwan | Futu, Tiger Brokers, Longbridge | Noch nicht: Schlüssellaufzeit oder Antwortformat nicht vollständig dokumentiert, oder Schlüssel lassen sich nicht auf Lesezugriff beschränken |
| China, Hongkong und Taiwan | East Money, Huatai, CITIC, Yuanta, Fubon | Keine öffentliche Web-API (nur Desktop-Terminals oder Zertifikat-SDKs) |
| Japan | OANDA Japan (Konten, die für den API-Zugang infrage kommen) | Direkt, als OANDA |
| Japan | SBI Securities, Rakuten Securities, Monex, Matsui | Keine öffentliche API |
| Australien und Neuseeland | CommSec, Stake | SnapTrade |
| Australien und Neuseeland | Sharesies, Hatch, Kernel, Simplicity, KiwiSaver-Programme | Akahu (neuseeländische Konten) |
| Naher Osten und Afrika | eToro | Direkt |
| Naher Osten und Afrika | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | Keine öffentliche API für Privatpersonen |
| Südostasien | Stockbit, Ajaib, Bibit, IPOT, VPS | Keine öffentliche API |
| Südostasien | SSI, TCBS, DNSE | Nicht möglich: 8-Stunden-Token mit Einmalcode, oder nur Barguthaben |
| Lateinamerika | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | Keine öffentliche API für Privatpersonen, oder Anmeldung nur mit Passwort |
| Forex und CFD | OANDA, Capital.com | Direkt |
| Forex und CFD | MetaTrader-Broker (XM, Exness, Pepperstone, IC Markets, Admirals) | Nicht möglich: kein HTTPS-Lesezugriff |
| Forex und CFD | cTrader-Broker, FXCM, Forex.com | Nicht möglich: App-Registrierung, veraltete API oder Passwort-Logins |

## Andere Broker {#other-brokers}

Capital verbindet sich nur mit Schnittstellen, die vom Smartphone über HTTPS funktionieren, ein Token verwenden, das Sie selbst erstellen können, und lesen, ohne handeln zu können. Das schließt vorerst aus:

- **MetaTrader-4- und -5**-Konten. Das Investor-Passwort gewährt Lesezugriff, aber nur innerhalb des MetaTrader-Terminals; Broker bieten dafür keine HTTPS-Schnittstelle.
- Broker, deren API ein auf einem Computer laufendes Programm voraussetzt (zum Beispiel das Gateway der Interactive Brokers Client Portal Web API; Capital nutzt stattdessen den Flex Web Service) oder die Registrierung einer OAuth-Anwendung.
- Broker, deren API nur kurzlebige Token über OAuth ausstellt, zum Beispiel Saxo Bank (Zugriffstoken gelten 20 Minuten; das 24-Stunden-Token des Entwicklerportals gilt nur für die Simulationsumgebung).
- Banken und Broker ohne öffentliche API.

Viele dieser Broker deckt [SnapTrade](#snaptrade) ab. Andernfalls erfassen Sie das Guthaben als **manuelle** Position und aktualisieren Sie die Zahl, wenn Sie Ihren Kontoauszug prüfen. Bietet Ihr Broker einen einfachen tokenbasierten HTTPS-Endpunkt, der den Kontowert liest, [eröffnen Sie ein Issue]({{ site.repo }}/issues) mit einem Link zu dessen Dokumentation. Jeder Broker in Capital ist ein kleines Plugin; Entwickler können einen hinzufügen, indem sie [der Plugin-Anleitung]({{ site.repo }}/blob/main/BROKER-PLUGINS.md) folgen.

## Meldungen und was zu tun ist {#messages}

| Meldung | Was zu tun ist |
|---|---|
| *Interactive Brokers benötigt seine Zugangsdaten auf dem Bildschirm „Broker“* / *OANDA benötigt seine Zugangsdaten …* | Fügen Sie Token, Schlüssel oder Client-ID unter Zugangsdaten im Tab „Broker“ ein. |
| *Token ist abgelaufen; erzeugen Sie im Client Portal einen neuen* | Erzeugen Sie ein neues Flex-Web-Service-Token und fügen Sie es ein. |
| *Token ist ungültig* | Kopieren Sie das Token erneut; ein neues Token ersetzt das alte. |
| *Token ist auf eine andere IP-Adresse beschränkt* | Erzeugen Sie das Token ohne IP-Beschränkung. |
| *Flex-Query-ID ist ungültig* | Prüfen Sie die Zahl; die Query muss eine Activity Flex Query dieses Logins sein. |
| *Fügen Sie der Flex Query den Abschnitt Net Asset Value (NAV) Summary in Base mit Report Date und Total hinzu* | Bearbeiten Sie die Query und fügen Sie Abschnitt und Felder hinzu. |
| *Fügen Sie der Flex Query das Feld Currency aus Account Information hinzu* | Bearbeiten Sie die Query und fügen Sie das Feld hinzu. |
| *Die Abfrage lieferte N Konten; erstellen Sie eine Flex Query pro Konto* | Erstellen Sie eine Query, die ein Konto abdeckt. |
| *Bericht ist noch nicht fertig; aktualisieren Sie in einer Minute erneut* | Interactive Brokers erzeugt den Bericht noch; aktualisieren Sie erneut. |
| *Zugriff verweigert; Anbieterschlüssel oder Kontingent prüfen* | Das OANDA-Token ist falsch oder widerrufen, oder das Trading-212-Schlüsselpaar ist falsch oder hat nicht die Berechtigung Account data. |
| *SnapTrade hat für dieses Konto noch keinen Gesamtwert; synchronisieren Sie die Verbindung und versuchen Sie es erneut* | SnapTrade hat den Broker noch nicht synchronisiert; aktualisieren Sie später erneut. |
| *Noch keine Konten verbunden. Verbinden Sie zuerst einen Broker über SnapTrade.* | Öffnen Sie das Connection Portal im Editor und verbinden Sie einen Broker. |
| *Negativer Kontowert … wird nicht unterstützt* | Das Konto ist im Soll; es trägt nichts zu Ihren Ersparnissen bei. |
| *tastytrade hat das Refresh-Token oder das Client-Secret abgelehnt; erstellen Sie einen neuen Grant* | Erstellen Sie einen neuen Grant für die Anwendung und fügen Sie dessen Refresh-Token ein; prüfen Sie das Client-Secret. |
| *Capital.com hat keine Sitzung geöffnet; prüfen Sie API-Schlüssel, Login und Schlüssel-Passwort* | Der Schlüssel, die E-Mail oder das eigene Passwort des Schlüssels ist falsch, oder der Schlüssel ist abgelaufen. |
| *Konto nicht gefunden; wählen Sie es erneut aus* | Der Broker listet dieses Konto nicht mehr; bearbeiten Sie es und wählen Sie es unter **Konten abrufen** aus. |

Der vorherige Wert bleibt nach jeder dieser Meldungen sichtbar, als veraltet gekennzeichnet.
