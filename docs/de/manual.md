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
3. Optional können Sie in den Einstellungen **Anbieterschlüssel** für Betreiber hinterlegen, die mit einem kostenlosen Schlüssel höhere Limits bieten (Alchemy, TronGrid, TON Center, CoinGecko). Für jede Chain und jede Preisquelle gibt es eine Standardquelle ohne Schlüssel. Brokerkonten benötigen ein **Zugriffstoken** des Brokers, das Sie im Tab „Broker“ eingeben; siehe [Broker- und Forex-Konten]({{ page.base }}/accounts).

## Töpfe {#buckets}

Tab „Töpfe“ → **+**. Geben Sie dem Topf einen Namen und eine Währung. Öffnen Sie ihn, um Positionen hinzuzufügen:

- **Manuelle Position**: ein Name, ein Währungs- oder Vermögenswert-Code (EUR, USD, BTC, ein Aktienticker, den Sie selbst bewerten …) und eine Menge. Verwenden Sie sie für Bankguthaben, Bargeld und alles, was die App nicht selbst auslesen kann.
- **Wallet-Position**: Wählen Sie die Chain (BTC, ETH, TON, TRX) und fügen Sie eine öffentliche Adresse ein. Beim Aktualisieren liest die App das native Guthaben und bei ETH, TON und TRX auch die fungiblen Token auf der Adresse.
- **Brokerkonto**: Wählen Sie eines der Konten, die Sie im Tab „Broker“ hinzugefügt haben. Beim Aktualisieren liest die App den Gesamtwert des Kontos in dessen Basiswährung. Ein Konto kann mit einem Topf verknüpft werden.

Beträge akzeptieren Punkt oder Komma als Dezimaltrennzeichen, jedoch keine Tausendertrennzeichen. Jeder Topf zeigt native Mengen und deren Wert in Ihrer Standardwährung. Fehlt ein Kurs, wird die Summe als unvollständig gekennzeichnet; ein veralteter zwischengespeicherter Wert bleibt mit einem Hinweis nutzbar.

**Token.** Ein Token wird über seine Vertragsadresse identifiziert, nie über seinen Namen. Er zählt nur, wenn Ihre gewählte Preisquelle genau diesen Vertrag listet; alles andere erscheint als *Unbekannter Token · nicht gezählt* und fließt nicht in die Summen ein. Öffnen Sie den Editor einer Wallet-Position, um ihre Token abzurufen und diejenigen auszuschalten, die Sie nicht möchten.

Der **Portfoliomodus** (Einstellungen des Topfs) behandelt einen Topf als Anlageportfolio: Legen Sie je Vermögenswert einen Zielanteil fest, vergleichen Sie den Ist-Anteil mit dem Ziel und lassen Sie sich mit **Rebalancing** auflisten, was Sie für einen bestimmten Betrag kaufen sollten. Verkäufe werden nur vorgeschlagen, wenn *Verkäufe beim Rebalancing erlauben* aktiviert ist. Es ist ein Rechner; er ändert nichts.

## Anlage-PnL {#pnl}

Schalten Sie **Anlage-PnL** im Editor eines Topfs ein, um zu sehen, was der Topf verdient hat, getrennt von dem, was Sie eingezahlt haben. Der Topf-Bildschirm zeigt dann Gewinn oder Verlust und die zeitgewichtete Rendite für den gesamten Zeitraum, das letzte Jahr, den letzten Monat, die letzte Woche und den letzten Tag, dazu ein Wertdiagramm und ein Gewinn-und-Verlust-Diagramm mit Zeitraum-Chips (Alle, 1Y, 1M, 1W, 1D).

- **Daten.** Jede Aktualisierung erfasst die Menge jeder Position des Topfs und fragt die verknüpften Broker nach Ein- und Auszahlungen; Mengenänderungen bei manuellen Positionen und Wallets zählen als Ein- oder Auszahlung zum Kurs dieses Tages. Vergangene Werte werden mit dem täglichen [Kursverlauf]({{ page.base }}/screens/settings) bewertet.
- **Nachberechnung.** Beim Einschalten baut die App den Verlauf aus den Snapshots in Ihrem Ordner auf (einer pro Tag), ruft die fehlenden Kurse ab und fragt jeden Broker nach seinem Einzahlungsverlauf und, sofern der Broker ihn anbietet, nach dem täglichen Kontowert. Welche Broker was liefern, steht auf der [Broker-Seite]({{ page.base }}/accounts#pnl).
- **Lesen.** Gewinn oder Verlust ist die Wertänderung minus Nettoeinzahlungen. Der Prozentwert ist zeitgewichtet, sodass der Zeitpunkt von Einzahlungen ihn nicht verschiebt. Beginnt ein Zeitraum vor Ihrem Verlauf, steht statt einer Zahl *seit <Datum>*.

## Ziele {#goals}

Tab „Ziele“ → **+**. Ein Ziel hat einen Namen, eine Währung, einen Zielbetrag und ein Fälligkeitsdatum. Öffnen Sie das Ziel und wählen Sie **Topf verknüpfen**, um festzulegen, welche Töpfe es finanzieren dürfen – optional mit einem Limit: einem festen Betrag, einem Prozentsatz des Topfs oder einem Prozentsatz des Ziels.

So wird das Geld zugeordnet:

- Ziele mit früherem Fälligkeitsdatum werden zuerst finanziert. Ziele mit gleichem Datum werden in der angezeigten Reihenfolge finanziert; ziehen Sie am Griff, um sie neu anzuordnen.
- Ein Topf, der mit mehreren Zielen verknüpft ist, wird entsprechend den Limits zwischen ihnen aufgeteilt und nie doppelt gezählt.
- Das Ergebnis wird als *finanziert / Zielbetrag* und *Noch offen* angezeigt. In der Übersicht sehen Sie die Gesamtsumme, den Zielen zugeordneten Betrag und den Rest.

**Kennzeichen.** *Finanziert* (grün), wenn Ihre heutigen Ersparnisse das Ziel bereits decken. *Wird rechtzeitig finanziert* (grün), wenn geplante Sparbeträge es spätestens zum Fälligkeitsdatum decken. Andernfalls *Nicht finanziert* (gelb). Der Text unter dem Ziel zeigt, wann es finanziert ist oder wie viel fehlt.

**Archivieren** Sie ein Ziel, um es zu behalten, ohne es zu berücksichtigen. Archivierte Ziele stehen am Ende der Liste.

## FIRE {#fire}

Ein Ziel vom Typ **FIRE** zeigt, wann Ihre Ersparnisse beginnen können, Ihre Lebenshaltungskosten zu tragen, und wie lange sie reichen. Es ist Rechnerei mit Ihren eigenen Zahlen; es empfiehlt nichts. Der Bildschirm ist unter [Ziel]({{ page.base }}/screens/goal#fire) beschrieben.

**Einrichten.**

1. Tab „Ziele“ → **+**, **Zieltyp** auf **FIRE** stellen, Name und Währung wählen.
2. Geben Sie ein, was Sie wissen, in heutiger Kaufkraft: **Geburtsdatum**, **FIRE-Datum**, **Ich kann investieren** (pro Woche, 2 Wochen, Monat, Jahr oder N Tage), **Beitragswachstum % pro Jahr** (eine jährliche Erhöhung dessen, was Sie investieren; leer = Inflation), **Ausgaben pro Monat nach FIRE**, **Betrag nach FIRE**. Lassen Sie leer, was die App berechnen soll.
3. Öffnen Sie das Ziel und wählen Sie **Topf verknüpfen** für die Ersparnisse, die Sie schon haben, wie bei jedem Ziel.

**So lesen Sie es.** Die Tabelle hat drei Spalten, **Durchschnitt**, **Optimistisch** und **Pessimistisch**, für drei Sätze von Wachstums- und Inflationsraten. Wenn Sie das FIRE-Datum leer lassen, berechnet jedes Szenario es als das erste Datum, ab dem die Ausgaben bis zur Lebenserwartung reichen, und zeigt es in seiner Beschriftung. **Geld reicht** nennt, bis zu welchem Datum und Alter das Geld die Ausgaben deckt; *über die Lebenserwartung hinaus* und *Geld geht nie aus 🥳* sind die guten Fälle; *es fehlen …* heißt, dass das Datum nicht erreichbar ist. Das Diagramm zeigt das Geld in heutiger Kaufkraft: steigend bis zum gestrichelten FIRE-Datum und fallend danach. Verstehen Sie die drei Kurven als Spanne, nicht als Prognose. **Ausgaben jetzt · pro Monat** und **· pro Jahr** zeigen, wie viel Sie jetzt entnehmen können: 0 vor dem FIRE-Datum; danach das Geld, das das Ziel heute zu den neuesten Preisen hat, verteilt bis zur Lebenserwartung. Fallen die Märkte, fällt es mit Ihrem Geld.

**Annahmen.** Die Ausgaben behalten ihre Kaufkraft (sie wachsen mit der Inflation); der Beitrag wächst um seinen eigenen jährlichen Prozentsatz, bei leerem Feld mit der Inflation. Steuern, Renten, Sozialleistungen und Einkommen nach FIRE werden nicht modelliert. Standardwerte: Wachstum 7 % (optimistisch 10 %, pessimistisch 4 %), Inflation 3 % (2 %, 5 %), Entnahmerate 4 %, Arbeiten bis Alter 65 (ein berechnetes FIRE-Datum wird nicht darüber hinaus gesucht), Lebenserwartung 95. Ändern Sie jeden davon unter **Erweiterte Annahmen**; Ihr Wert hat immer Vorrang.

**Quellen der Standardwerte.** UBS Global Investment Returns Yearbook 2026 (Welt, 1900–2024: Aktien 9,8 % pro Jahr nominal, Anleihen 4,6 %, Inflation 2,9 %; 7 % entspricht etwa einer 60/40-Mischung); die 2-%-Inflationsziele der Zentralbanken; Bengen (1994) und die Trinity-Studie (1998) für die 4-%-Entnahmeregel.

**Aktuelle Sätze.** Einstellungen → Kostenlose Datenanbieter → **Inflation** (**World Bank**, **ECB**, **OECD**, **Aus**) und **Marktwachstum** (**World Bank**, **Yahoo Finance · inoffiziell**, **Aus**). **Aktualisieren** ruft sie für die Währung jedes FIRE-Ziels ab. Inflation: Verbraucherpreise der World Bank (jährlich, nach Land oder Weltdurchschnitt), HVPI des Euroraums der EZB (nur EUR), OECD (Mitgliedsländer, nicht RUB). Wachstum: Veränderung des S&P-Global-Aktienindex der World Bank je Land (EUR nutzt den US-Index) oder 30 Jahre Monatsschlusskurse vom inoffiziellen Endpunkt von Yahoo Finance (^GSPC, ^GDAXI, ^FTSE, ^N225, ^BSESN, ^BVSP, 000001.SS; andere Währungen nutzen ^GSPC). Aus der Reihe: Die durchschnittliche Inflation ist der Mittelwert der letzten 20 Jahre, optimistisch und pessimistisch der niedrigste und der höchste 5-Jahres-Mittelwert; das durchschnittliche Wachstum ist die jährliche Zinseszinsrate über bis zu 30 Jahre, optimistisch und pessimistisch die beste und die schlechteste 10-Jahres-Rate. Beide Wachstumsquellen sind Kursindizes ohne Dividenden und unterschätzen daher die Gesamtrendite. **Aus** behält die eingebauten Standardwerte. Ein fehlgeschlagener Abruf behält die vorherige Reihe und zeigt den Fehler an.

## Pläne {#plans}

Tab „Pläne“ → **+**. Ein geplanter Sparbetrag ist ein Betrag, den Sie zu einem bestimmten Datum hinzufügen möchten, zum Beispiel die monatliche Sparrate vom Gehalt am Monatsende. Pläne gehören nicht zu Ihren Ersparnissen; sie verlängern nur die Prognose: „Mit geplanten Sparbeträgen am 30.10.2026 finanziert · rechtzeitig“.

Geld aus Plänen wird nach den heutigen Töpfen angerechnet, und zwar auf die Ziele in der Reihenfolge ihrer Fälligkeit. So füllt es nur auf, was noch offen ist. Ist das Datum eines Plans verstrichen, wandert er in den Bereich **Archiviert** und wird nicht mehr berücksichtigt: Entweder haben Sie das Geld bereits in einen Topf überführt und die App sieht es dort, oder der Plan wurde nicht umgesetzt. Setzen Sie das Datum in die Zukunft, um ihn wieder zu aktivieren; löschen Sie ihn, wenn er hinfällig ist.

## Broker

Tab „Broker“ → **+**. Ein Brokerkonto ist eine reine Lesezugriffsverbindung: Wählen Sie den Broker (die Liste steht unter [Broker- und Forex-Konten]({{ page.base }}/accounts)), geben Sie die Konto- oder Query-ID ein und speichern Sie; das Token oder der Schlüssel des Brokers wird einmal unter **Zugangsdaten** im selben Tab eingegeben und gilt für alle Konten dieses Brokers. Der Tab listet jedes Konto mit seinem letzten Wert und dem Topf, mit dem es verknüpft ist. **Salden unter diesem Betrag ignorieren** mit einem Betrag in Ihrer Standardwährung (standardmäßig 1) lässt einen Kleinstsaldo im Topf als 0 zählen. Verknüpfen Sie es mit einem Topf über **Position hinzufügen → Brokerkonto**; löschen Sie die Position, um die Verknüpfung zu lösen, und löschen Sie das Konto, um die Verbindung zu entfernen. Die Einrichtungsschritte mit Links zur Dokumentation der Broker finden Sie unter [Broker- und Forex-Konten]({{ page.base }}/accounts).

## Aktualisieren

Das Aktualisieren-Symbol oben lädt alle Wallet-Guthaben, Brokerkontowerte und Kurse neu. Ein Topf lässt sich auch einzeln aktualisieren. Die App aktualisiert einmal bei einem Kaltstart; bei der Rückkehr aus dem Hintergrund werden nur die lokalen Dateien neu geladen. Zum Aktualisieren ist eine Internetverbindung nötig; ohne sie bleiben die bisherigen Werte erhalten und werden als veraltet gekennzeichnet. Jede Aktualisierung hält außerdem die Kurse des Tages in einem Kursverlauf fest; **Einstellungen → Kurse automatisch aktualisieren** hält diesen Verlauf im Hintergrund aktuell (alle 1 bis 24 Stunden) und liefert Ihnen beim Öffnen der App die neuesten Kurse.

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
