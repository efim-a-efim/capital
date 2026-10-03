---
layout: screen
lang: de
base: "/de"
key: "screens/settings"
screen: settings
title: Einstellungen
---
# Einstellungen

**Was es ist.** Alles, was kein Datensatz ist: Voreinstellungen, Datenquellen, Sicherheit und der Ordner. Wird über das Zahnrad in der oberen Leiste geöffnet; auf breiten Bildschirmen ist es ein Tab in der seitlichen Navigationsleiste.

**Allgemein.** Standardwährung und Design (System, Hell, Dunkel). Die Standardwährung gilt für die Übersicht und wird für neue Töpfe und Ziele vorgeschlagen; vorhandene Einträge behalten ihre Währung. **Sprache** wechselt die Oberflächensprache sofort; *Systemstandard* folgt dem Gerät.

**Kostenlose Datenanbieter.** Eine Zeile je Datentyp, jeweils mit dem verwendeten Betreiber: BTC-, ETH-, TON-, TRX-Guthaben; ETH-, TON-, TRX-Tokenlisten; Kryptopreise; Wechselkurse. Tippen Sie auf eine Zeile, um einen anderen Betreiber zu wählen, Token-Abfragen für eine Chain auf **Aus** zu stellen oder einen optionalen API-Schlüssel einzugeben. Schlüssel werden verschlüsselt auf dem Gerät gespeichert und nur an den Betreiber gesendet, der sie ausgegeben hat. **Quellen testen / Portfolio aktualisieren** fragt jeden Betreiber mit den Vermögenswerten ab, die Sie tatsächlich halten, und meldet, was fehlgeschlagen ist. Kein Betreiber wird jemals stillschweigend ersetzt.

**Brokerkonten.** Die Zugangsdaten jedes unterstützten Brokers (Interactive Brokers, OANDA, Trading 212, SnapTrade), verschlüsselt gespeichert wie die Anbieterschlüssel und nur an diesen Broker gesendet. Die App liest damit Kontowerte und erteilt nie Orders. **Einrichtungsanleitung für Brokerkonten** öffnet [Broker- und Forex-Konten]({{ page.base }}/accounts).

**Kurse und Aktualität.** Jeder zwischengespeicherte Kurs mit dem Zeitpunkt, zu dem er ermittelt und abgerufen wurde. Veraltete Kurse bleiben nutzbar und werden in der Übersicht gekennzeichnet.

**Sicherheit.** **Verschlüsselung** verschlüsselt jede Datei im Ordner mit einem Passwort; beim Ausschalten werden sie entschlüsselt. Mit aktiver Verschlüsselung können Sie eine **PIN festlegen**, **Biometrie verwenden** aktivieren, **Zeit im Hintergrund bis zur Sperre** wählen und das **Passwort ändern**. Eine Passwortwiederherstellung gibt es nicht. Zehn falsche PIN-Eingaben entfernen die PIN; das Passwort funktioniert immer. Siehe [Sicherheit]({{ page.base }}/manual#security).

**Speicher.** Der aktuelle Ordner und seine Revision. **Ordner neu verbinden / öffnen** startet die Ordnerauswahl erneut; **Lokale Dateien neu laden** liest den Ordner neu ein, zum Beispiel nachdem Ihr Sync-Tool Änderungen geliefert hat; **Backup exportieren** schreibt eine einzelne portable Datei (unverschlüsselt, wenn die Verschlüsselung aus ist, und entsprechend gekennzeichnet); **Backup wiederherstellen** prüft eine Datei, bevor die Daten ersetzt werden, und behält die vorhandenen Snapshots.

**Quellen / Nachweise.** Links zur Website jedes Betreibers.

**Rechtliches.** Links zur [Datenschutzerklärung]({{ page.base }}/privacy), zu den Erklärungen „[Datensicherheit]({{ page.base }}/data-safety)“ und „[Finanzfunktionen]({{ page.base }}/financial-features)“ auf dieser Website, in der Sprache der Oberfläche. Die App-Version und die Build-Nummer stehen ganz unten.
