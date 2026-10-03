---
layout: default
lang: de
base: "/de"
key: "financial-features"
title: Angaben zu Finanzfunktionen
class: doc
---
# Angaben zu Finanzfunktionen

<p class="meta">Antworten für das Formular in der Google Play Console (Richtlinien und Programme → App-Inhalte → Finanzfunktionen) mit Begründung. Geprüft anhand von Version 2.2.1 am 30. September 2026.</p>

## Antwort im Formular

**Select all of the financial features the app provides** (Wählen Sie alle Finanzfunktionen aus, die die App bietet): **The app does not provide any financial features** (Die App bietet keine Finanzfunktionen).

## Begründung

Capital ist ein persönlicher Spar-Tracker. Die App erfasst, was der Nutzer bereits besitzt, und zeigt, wie sich diese Ersparnisse auf die eigenen Ziele des Nutzers verteilen. Gegenübergestellt mit jeder Funktion im Formular:

| Funktion im Formular | Capital |
|---|---|
| Personal loan direct lender, loan facilitator, payday loans, line of credit, earned wage advances, microfinance, buy now pay later (Direktkreditgeber für Privatkredite, Kreditvermittler, Kurzzeitkredite, Kreditlinie, Lohnvorschüsse, Mikrofinanzierung, „Jetzt kaufen, später bezahlen“) | Keinerlei Kreditvergabe |
| Banking (Bankdienstleistungen) | Keine Konten, keine Einlagen, kein Kontozugriff. Bankguthaben gibt der Nutzer selbst ein. Werte von Depot- und Forex-Konten werden über die eigene Reporting-Schnittstelle des Brokers mit einem vom Nutzer erstellten Token gelesen; die App kann keine Orders erteilen, nichts überweisen und nichts abheben |
| Mobile payments and digital wallets, money transfer and wire services (Mobile Zahlungen und digitale Geldbörsen, Geldtransfer- und Überweisungsdienste) | Kann kein Geld senden, empfangen oder verwahren. Die Zielzuordnung ist eine auf dem Bildschirm angezeigte Berechnung; sie bewegt nichts |
| Cryptocurrency wallet (Kryptowährungs-Wallet) | Liest das Guthaben öffentlicher Adressen, die der Nutzer einfügt. Die App verwahrt niemals private Schlüssel oder Seed-Phrasen und kann keine Transaktionen signieren oder übertragen; sie ist daher keine Wallet |
| Cryptocurrency exchange (Kryptowährungsbörse) | Kein Handel, keine Orderweiterleitung, kein Fiat-Einstieg (On-Ramp) |
| Rewards and incentives, crowdfunding and chit funds, prediction markets (Prämien und Anreize, Crowdfunding und Chit-Fonds, Prognosemärkte) | Keine |
| Credit monitoring and reporting (Bonitätsüberwachung und -berichte) | Keine |
| Financial advice (Finanzberatung) | Keine. Die Prognose gibt lediglich Rechenergebnisse auf Basis der eigenen Zahlen des Nutzers aus („Mit geplanten Sparbeträgen am … finanziert“); sie empfiehlt kein Produkt, keinen Vermögenswert und keine Handlung. Der Rebalancing-Rechner listet die Käufe auf, die nötig sind, um die vom Nutzer selbst festgelegten Prozentsätze zu erreichen |
| Insurance (Versicherungen) | Keine |
| In-App-Käufe, Spenden | Die App wickelt keine ab. Ein Trinkgeld-Bildschirm zeigt die öffentlichen Wallet-Adressen des Entwicklers (dieselben wie auf dieser Website); eine Überweisung erfolgt in der eigenen Wallet-App des Nutzers, schaltet nichts frei und ist für die App unsichtbar |

Die App bietet außerdem keine In-App-Käufe und keine kostenpflichtigen Funktionen.

## Falls der Prüfer anderer Meinung ist

Stuft die Play-Prüfung die App dennoch als Anbieter einer Finanzfunktion ein, ist die passendste Option **Other** (Sonstiges) mit dieser Beschreibung:

> Schreibgeschützter persönlicher Spar-Tracker. Nutzer geben ihre Guthaben ein, fügen öffentliche Blockchain-Adressen ein oder verbinden ein Depotkonto über ein Reporting-Token; die App ruft Guthaben, Kontowerte und Marktpreise von Datenquellen Dritter ab und zeigt, wie die Ersparnisse die eigenen Ziele des Nutzers abdecken. Keine Verwahrung, keine Schlüssel, keine Transaktionen, keine Kreditvergabe, kein Handel, keine Beratung.

Länderspezifische Anforderungen für Privatkredit-Apps und die Fragen zu Kryptowährungen für die Vereinigten Staaten gelten nicht, da keine dieser Funktionen ausgewählt ist.

## Weitere Fakten, nach denen ein Prüfer fragen könnte

- Marktdaten stammen von Drittbetreibern, die der Nutzer auswählt (siehe [Datenschutzerklärung]({{ page.base }}/privacy)). Die App zeigt Namen und Website des Betreibers in den Einstellungen an.
- Wallet-Abfragen nutzen öffentliche, schreibgeschützte Blockchain-APIs.
- Brokerkonten (Interactive Brokers, OANDA, Trading 212, SnapTrade, Alpaca, Tradier, tastytrade, Public.com, eToro, Indexa Capital, T-Invest, ALOR, Capital.com, Akahu) werden mit einem Token oder Schlüssel gelesen, das der Nutzer im eigenen Portal des Brokers erstellt; die App ruft nur Reporting-Endpunkte auf und kann weder Orders erteilen noch Geld bewegen. Die Einrichtung ist unter [Broker- und Forex-Konten]({{ page.base }}/accounts) dokumentiert.
- Die App läuft vollständig auf dem Gerät und hat keinen vom Entwickler betriebenen Server.
