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

Capital verbindet sich nur mit Schnittstellen, deren Zugangsdaten langlebig sind: ein Token oder Schlüssel, den Sie einmal erstellen und der gültig bleibt, bis Sie ihn widerrufen (bei Interactive Brokers bis zum von Ihnen gewählten Ablauf, höchstens ein Jahr). Derzeit unterstützt:

| Broker | Verwendete Schnittstelle | Was gelesen wird |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (nur Berichtsabruf) | Nettovermögenswert am letzten Geschäftstag, in der Basiswährung des Kontos |
| [OANDA](#oanda) | v20 REST API, fxTrade-Live-Konten | Nettovermögenswert zum Zeitpunkt der Aktualisierung, in der Währung des Kontos |
| [Trading 212](#trading-212) | Public API, Invest- und Stocks-ISA-Konten | Gesamtwert des Kontos zum Zeitpunkt der Aktualisierung, in der Hauptwährung des Kontos |
| [SnapTrade](#snaptrade) | SnapTrade Personal, ein Aggregator für viele Broker | Gesamtwert des Kontos, wie der Broker ihn an SnapTrade meldet, in der Währung des Kontos |

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

Der vorherige Wert bleibt nach jeder dieser Meldungen sichtbar, als veraltet gekennzeichnet.
