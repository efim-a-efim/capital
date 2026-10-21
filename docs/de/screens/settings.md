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

**Hinweis.** Eine gelbe Leiste oben zeigt an, wenn zwischengespeicherte Werte veraltet sind (aktualisieren Sie, sobald Sie online sind) oder wenn eine Bewertung unvollständig ist, weil ein Guthaben oder ein Kurs fehlt. Sie erscheint nur hier.

**Allgemein.** Standardwährung und Design (System, Hell, Dunkel). Die Standardwährung gilt für die Übersicht und wird für neue Töpfe und Ziele vorgeschlagen; vorhandene Einträge behalten ihre Währung. **Sprache** wechselt die Oberflächensprache sofort; *Systemstandard* folgt dem Gerät.

**Kostenlose Datenanbieter.** Eine Zeile je Datentyp, jeweils mit dem verwendeten Betreiber: BTC-, ETH-, TON-, TRX-Guthaben; ETH-, TON-, TRX-Tokenlisten; Kryptopreise; Wechselkurse; **Inflation** (**World Bank**, **ECB**, **OECD**, **Aus**) und **Marktwachstum** (**World Bank**, **Yahoo Finance · inoffiziell**, **Aus**), die [FIRE-Ziele]({{ page.base }}/screens/goal#fire) speisen und bei **Aus** die eingebauten Standardwerte beibehalten; **Kursverlauf** (**Frankfurter + DefiLlama**, **Aus**) füllt den täglichen Kursverlauf jedes Vermögenswerts, den Sie halten, bis zum ältesten Datum Ihrer Einträge, sodass vergangene Werte berechnet werden können. Tippen Sie auf eine Zeile, um einen anderen Betreiber zu wählen, Token-Abfragen für eine Chain auf **Aus** zu stellen oder einen optionalen API-Schlüssel einzugeben. Schlüssel werden verschlüsselt auf dem Gerät gespeichert und nur an den Betreiber gesendet, der sie ausgegeben hat. **Quellen testen / Portfolio aktualisieren** fragt jeden Betreiber mit den Vermögenswerten ab, die Sie tatsächlich halten, und meldet, was fehlgeschlagen ist. Kein Betreiber wird jemals stillschweigend ersetzt.

**Broker.** Brokerkonten und ihre Zugangsdaten haben einen eigenen Bildschirm: [Broker]({{ page.base }}/screens/brokers).

**Kurse automatisch aktualisieren.** **Aus**, **Alle 24 Std.**, **Alle 12 Std.**, **Alle 6 Std.**, **Alle 3 Std.** oder **Alle 1 Std.** Wenn aktiviert, ruft ein Hintergrundjob in diesem Intervall den aktuellen Kurs jedes Vermögenswerts ab, den Sie halten, mit den oben gewählten Betreibern, und speichert die Stichproben im Kursverlauf. Er berührt den Datenordner nie: Beim nächsten Öffnen der App oder Aktualisieren werden die Stichproben in die zwischengespeicherten Kurse übernommen, sodass die Summen auch offline mit den neuesten Kursen starten. Er benötigt eine Netzwerkverbindung und verbraucht etwas Akku und Daten. Der letzte Lauf steht unter **Kurse und Aktualität** als *Letzte Hintergrundaktualisierung*.

**Kurse und Aktualität.** Jeder zwischengespeicherte Kurs und jede abgerufene Inflations- und Wachstumsreihe, mit Quelle und dem Zeitpunkt, zu dem er ermittelt und abgerufen wurde. Veraltete Kurse bleiben nutzbar und werden durch den Hinweis oben auf diesem Bildschirm gekennzeichnet.

**Sicherheit.** **Verschlüsselung** verschlüsselt jede Datei im Ordner mit einem Passwort; beim Ausschalten werden sie entschlüsselt. Mit aktiver Verschlüsselung können Sie eine **PIN festlegen**, **Biometrie verwenden** aktivieren, **Zeit im Hintergrund bis zur Sperre** wählen und das **Passwort ändern**. Eine Passwortwiederherstellung gibt es nicht. Zehn falsche PIN-Eingaben entfernen die PIN; das Passwort funktioniert immer. Siehe [Sicherheit]({{ page.base }}/manual#security).

**Speicher.** Der aktuelle Ordner und seine Revision. **Ordner neu verbinden / öffnen** startet die Ordnerauswahl erneut; **Lokale Dateien neu laden** liest den Ordner neu ein, zum Beispiel nachdem Ihr Sync-Tool Änderungen geliefert hat; **Backup exportieren** schreibt eine einzelne portable Datei (unverschlüsselt, wenn die Verschlüsselung aus ist, und entsprechend gekennzeichnet); **Backup wiederherstellen** prüft eine Datei, bevor die Daten ersetzt werden, und behält die vorhandenen Snapshots.

**Quellen / Nachweise.** Links zur Website jedes Betreibers.

**Rechtliches.** Links zur [Datenschutzerklärung]({{ page.base }}/privacy), zu den Erklärungen „[Datensicherheit]({{ page.base }}/data-safety)“ und „[Finanzfunktionen]({{ page.base }}/financial-features)“ auf dieser Website, in der Sprache der Oberfläche. Die App-Version und die Build-Nummer stehen ganz unten.
