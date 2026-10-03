---
layout: default
lang: de
base: "/de"
key: "manual"
title: Benutzerhandbuch
class: doc
---
# Benutzerhandbuch

<p class="meta">Capital 2.2 · Android 8.0 und neuer</p>

## Die Idee

Sie bewahren Geld an mehreren Orten auf: auf einem Sparkonto, als Bargeld, in einem Depot, in einer Krypto-Wallet. Capital nennt jeden dieser Orte einen **Topf**. Sie brauchen dieses Geld für mehrere Dinge: einen Notgroschen, eine Reise, einen Laptop. Capital nennt jedes davon ein **Ziel**. Sie verknüpfen Töpfe mit Zielen, und die App berechnet, wie weit jedes Ziel mit dem, was Sie heute haben, finanziert ist. Tragen Sie die Beträge ein, die Sie zu sparen **planen**, dann zeigt sie Ihnen auch, an welchem Datum jedes Ziel finanziert ist.

Die App bewegt kein Geld. Sie ist ein Spiegel dessen, was Sie besitzen, und ein Rechner dafür, was es abdeckt.

## Erster Start {#first-launch}

1. **Ordner wählen.** Wählen Sie einen eigenen Ordner auf dem Gerät, zum Beispiel `Documents/Capital`. Dort wird jeder Datensatz gespeichert. Ein Ordner, der bereits Capital-Daten enthält, wird direkt geöffnet.
2. **Einstellungen → Standardwährung.** Summen und die Übersicht werden in dieser Währung angezeigt.
3. Optional können Sie in den Einstellungen **Anbieterschlüssel** für Betreiber hinterlegen, die mit einem kostenlosen Schlüssel höhere Limits bieten (Alchemy, TronGrid, TON Center, CoinGecko). Für jede Chain und jede Preisquelle gibt es eine Standardquelle ohne Schlüssel. Brokerkonten benötigen ein **Zugriffstoken** des Brokers; siehe [Broker- und Forex-Konten]({{ page.base }}/accounts).

## Töpfe {#buckets}

Tab „Töpfe“ → **+**. Geben Sie dem Topf einen Namen und eine Währung. Öffnen Sie ihn, um Positionen hinzuzufügen:

- **Manuelle Position**: ein Name, ein Währungs- oder Vermögenswert-Code (EUR, USD, BTC, ein Aktienticker, den Sie selbst bewerten …) und eine Menge. Verwenden Sie sie für Bankguthaben, Bargeld und alles, was die App nicht selbst auslesen kann.
- **Wallet-Position**: Wählen Sie die Chain (BTC, ETH, TON, TRX) und fügen Sie eine öffentliche Adresse ein. Beim Aktualisieren liest die App das native Guthaben und bei ETH, TON und TRX auch die fungiblen Token auf der Adresse.
- **Brokerkonto**: Wählen Sie den Broker (Interactive Brokers, OANDA, Trading 212, SnapTrade) und geben Sie die Flex-Query-ID oder die Konto-ID ein. Beim Aktualisieren liest die App den Gesamtwert des Kontos in dessen Basiswährung. Das Zugriffstoken geben Sie einmal unter Einstellungen → Brokerkonten ein. Die Einrichtungsschritte mit Links zur Dokumentation der Broker finden Sie unter [Broker- und Forex-Konten]({{ page.base }}/accounts).

Beträge akzeptieren Punkt oder Komma als Dezimaltrennzeichen, jedoch keine Tausendertrennzeichen. Jeder Topf zeigt native Mengen und deren Wert in Ihrer Standardwährung. Fehlt ein Kurs, wird die Summe als unvollständig gekennzeichnet; ein veralteter zwischengespeicherter Wert bleibt mit einem Hinweis nutzbar.

**Token.** Ein Token wird über seine Vertragsadresse identifiziert, nie über seinen Namen. Er zählt nur, wenn Ihre gewählte Preisquelle genau diesen Vertrag listet; alles andere erscheint als *Unbekannter Token · nicht gezählt* und fließt nicht in die Summen ein. Öffnen Sie den Editor einer Wallet-Position, um ihre Token abzurufen und diejenigen auszuschalten, die Sie nicht möchten.

Der **Portfoliomodus** (Einstellungen des Topfs) behandelt einen Topf als Anlageportfolio: Legen Sie je Vermögenswert einen Zielanteil fest, vergleichen Sie den Ist-Anteil mit dem Ziel und lassen Sie sich mit **Rebalancing** auflisten, was Sie für einen bestimmten Betrag kaufen sollten. Verkäufe werden nur vorgeschlagen, wenn *Verkäufe beim Rebalancing erlauben* aktiviert ist. Es ist ein Rechner; er ändert nichts.

## Ziele {#goals}

Tab „Ziele“ → **+**. Ein Ziel hat einen Namen, eine Währung, einen Zielbetrag und ein Fälligkeitsdatum. Öffnen Sie das Ziel und wählen Sie **Topf verknüpfen**, um festzulegen, welche Töpfe es finanzieren dürfen – optional mit einem Limit: einem festen Betrag, einem Prozentsatz des Topfs oder einem Prozentsatz des Ziels.

So wird das Geld zugeordnet:

- Ziele mit früherem Fälligkeitsdatum werden zuerst finanziert. Ziele mit gleichem Datum werden in der angezeigten Reihenfolge finanziert; ziehen Sie am Griff, um sie neu anzuordnen.
- Ein Topf, der mit mehreren Zielen verknüpft ist, wird entsprechend den Limits zwischen ihnen aufgeteilt und nie doppelt gezählt.
- Das Ergebnis wird als *finanziert / Zielbetrag* und *Noch offen* angezeigt. In der Übersicht sehen Sie die Gesamtsumme, den Zielen zugeordneten Betrag und den Rest.

**Kennzeichen.** *Finanziert* (grün), wenn Ihre heutigen Ersparnisse das Ziel bereits decken. *Wird rechtzeitig finanziert* (grün), wenn geplante Sparbeträge es spätestens zum Fälligkeitsdatum decken. Andernfalls *Nicht finanziert* (gelb). Der Text unter dem Ziel zeigt, wann es finanziert ist oder wie viel fehlt.

**Archivieren** Sie ein Ziel, um es zu behalten, ohne es zu berücksichtigen. Archivierte Ziele stehen am Ende der Liste.

## Pläne {#plans}

Tab „Pläne“ → **+**. Ein geplanter Sparbetrag ist ein Betrag, den Sie zu einem bestimmten Datum hinzufügen möchten, zum Beispiel die monatliche Sparrate vom Gehalt am Monatsende. Pläne gehören nicht zu Ihren Ersparnissen; sie verlängern nur die Prognose: „Mit geplanten Sparbeträgen am 30.10.2026 finanziert · rechtzeitig“.

Geld aus Plänen wird nach den heutigen Töpfen angerechnet, und zwar auf die Ziele in der Reihenfolge ihrer Fälligkeit. So füllt es nur auf, was noch offen ist. Ist das Datum eines Plans verstrichen, wandert er in den Bereich **Archiviert** und wird nicht mehr berücksichtigt: Entweder haben Sie das Geld bereits in einen Topf überführt und die App sieht es dort, oder der Plan wurde nicht umgesetzt. Setzen Sie das Datum in die Zukunft, um ihn wieder zu aktivieren; löschen Sie ihn, wenn er hinfällig ist.

## Aktualisieren

Das Aktualisieren-Symbol oben lädt alle Wallet-Guthaben, Brokerkontowerte und Kurse neu. Ein Topf lässt sich auch einzeln aktualisieren. Die App aktualisiert einmal bei einem Kaltstart; bei der Rückkehr aus dem Hintergrund werden nur die lokalen Dateien neu geladen. Zum Aktualisieren ist eine Internetverbindung nötig; ohne sie bleiben die bisherigen Werte erhalten und werden als veraltet gekennzeichnet.

## Sicherheit {#security}

Einstellungen → Sicherheit.

- **Verschlüsselung** verschlüsselt jede Datei im Ordner, auch ältere Revisionen, mit einem Passwort. Beim Ausschalten werden sie wieder entschlüsselt. Es gibt **keine Passwortwiederherstellung**: Wer das Passwort verliert, kann die Daten nicht mehr öffnen. Unverschlüsselte Backups, die vor dem Einschalten der Verschlüsselung erstellt wurden, bleiben lesbar; die App warnt davor, kann sie aber nicht löschen.
- **PIN** und **Biometrie** sind verfügbar, solange die Verschlüsselung aktiv ist. *Passwort verwenden* steht auf dem PIN-Bildschirm immer zur Verfügung. Nach 10 falschen PIN-Eingaben wird die PIN entfernt und nur noch das Passwort funktioniert. Falsche Eingaben löschen nie Daten.
- Solange die Verschlüsselung aktiv ist, sind Screenshots und die Vorschau in der Übersicht der zuletzt verwendeten Apps gesperrt.

## Synchronisierung, Backup, Wiederherstellung {#sync-backup-recovery}

Capital schreibt Snapshot-Dateien mit Revisions-IDs und Prüfsummen in Ihren Ordner und führt nie selbst eine Synchronisierung durch. Legen Sie den Ordner in ein beliebiges Sync-Tool, das Sie bereits verwenden. Bearbeiten zwei Geräte gleichzeitig, zeigt die App einen Konfliktbildschirm und lässt Sie eine Version wählen; beide Originale bleiben auf dem Datenträger erhalten.

- **Backup exportieren** (Einstellungen) schreibt eine einzelne portable Datei. **Wiederherstellen** prüft sie, bevor irgendetwas geändert wird.
- Schlägt das Speichern fehl, bleiben Ihre Änderungen im Arbeitsspeicher, mit *Erneut speichern* und *Kopie im Ordner speichern*.
- Geht die Ordnerberechtigung verloren, verbinden Sie denselben Ordner erneut.
- Eine Datei, die von einer neueren App-Version geschrieben wurde, wird von einer älteren abgelehnt; aktualisieren Sie die App.

## Sprache {#language}

Die App startet in der Gerätesprache, sofern diese eine der 15 unterstützten Sprachen ist, andernfalls auf Englisch. Sie können sie unter Einstellungen → Sprache ändern.

## Installation außerhalb von Google Play

Laden Sie die APK aus der [neuesten Version]({{ site.repo }}/releases/latest) herunter und öffnen Sie sie; erlauben Sie die Installation aus dieser Quelle, wenn Android danach fragt. Jede Version ist mit demselben Schlüssel signiert, daher lassen sich neue Versionen über alte installieren und Ihre Einstellungen bleiben erhalten. Der Ordner mit Ihren Daten wird von einem Update oder einer Deinstallation nie angetastet.
