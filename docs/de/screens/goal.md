---
layout: screen
lang: de
base: "/de"
key: "screens/goal"
screen: goal
title: Ziel
---
# Ziel

**Was es ist.** Ein einzelnes Ziel mit seiner Finanzierung. Sie erreichen es, indem Sie im Tab [Ziele]({{ page.base }}/screens/goals) auf eine Karte tippen; **← Alle Ziele** führt zurück.

**Kopfbereich.** Name und Kennzeichen, finanziert / Zielbetrag, Fälligkeitsdatum, die Prognosezeile und **Noch offen**: Zielbetrag minus das, was heute finanziert ist.

**Schaltflächen.** **Ziel bearbeiten** ändert Name, Währung, Zielbetrag und Fälligkeitsdatum. **Archivieren** behält das Ziel, ohne es zu berücksichtigen; ein archiviertes Ziel zeigt stattdessen **Aktivieren**. **Löschen** entfernt das Ziel samt seinen Verknüpfungen nach einer Bestätigung.

## FIRE {#fire}

Ein Ziel vom Typ **FIRE** (finanzielle Unabhängigkeit, früher Ruhestand) beantwortet zwei Fragen: Ab wann kann das Geld Ihre Lebenshaltungskosten tragen, und wie lange reicht es. Es ist eine Projektion auf Basis Ihrer eigenen Zahlen; sie empfiehlt nichts. Den Typ legen Sie im Editor mit **Zieltyp** (**Sparziel** / **FIRE**) fest.

**Felder im Editor.** Jedes Feld außer Name und Währung darf leer bleiben; die App berechnet, was sie aus dem Rest ableiten kann.

- **Geburtsdatum · YYYY-MM-DD · optional**: zeigt Alter an und setzt den Horizont auf die Lebenserwartung.
- **FIRE-Datum · YYYY-MM-DD · leer = wird berechnet**: der Tag, an dem Sie aufhören zu sparen und anfangen auszugeben.
- **Ich kann investieren · leer = wird berechnet**: ein Betrag pro **Woche**, **2 Wochen**, **Monat**, **Jahr** oder **N Tage** (dann **Tage** eingeben).
- **Beitragswachstum % pro Jahr · leer = Inflation**: um wie viel mehr Sie jedes Jahr investieren können, zum Beispiel eine jährliche Gehaltserhöhung. Leer behält die Kaufkraft des Beitrags.
- **Ausgaben pro Monat nach FIRE · leer = wird berechnet**.
- **Betrag nach FIRE · leer = wird berechnet**: der FIRE-Betrag, der Zielbetrag des Ziels.
- **Erweiterte Annahmen**: **Inflation % · Durchschnitt / optimistisch / pessimistisch**, **Wachstum % · Durchschnitt / optimistisch / pessimistisch**, **Entnahmerate %**, **Arbeiten bis Alter** (Standard 65) und **Lebenserwartung · Jahre**. Jede Bezeichnung endet mit „· jetzt {Wert}“, dem Wert, der gilt, wenn das Feld leer ist.

Alle Beträge verstehen sich in heutiger Kaufkraft der Zielwährung. Der Bildschirm zeigt außerdem den Wert zum FIRE-Datum in künftigem Geld. Das Geld, das das Ziel heute hat, stammt wie bei jedem Ziel aus den verknüpften Töpfen; [Pläne]({{ page.base }}/screens/plans) fließen nicht in die FIRE-Berechnung ein.

**Was berechnet wird.**

- FIRE-Datum: der erste Monat, ab dem die Ausgaben bis zur Lebenserwartung reichen (50 Jahre ohne Geburtsdatum) und, wenn ein Zielbetrag angegeben ist, der Zielbetrag erreicht wird; nicht später als **Arbeiten bis Alter**, sonst *Nicht erreichbar vor* diesem Geburtstag. Ohne Zielbetrag wird der FIRE-Betrag zu dem Betrag, den diese Lebensspanne braucht.
- Beitrag: aus Zielbetrag und FIRE-Datum.
- Zielbetrag: aus den Ausgaben (Ausgaben × 12 ÷ Entnahmerate), wenn das FIRE-Datum angegeben ist, oder aus FIRE-Datum und Beitrag.
- Ausgaben: aus dem Zielbetrag (Zielbetrag × Entnahmerate ÷ 12).

Sind FIRE-Datum, Beitrag und Zielbetrag alle angegeben, bleibt das Datum erhalten, und der Bildschirm zeigt die Lücke und das früheste erreichbare Datum. Der berechnete Zielbetrag und das berechnete Datum werden als Zielbetrag und Fälligkeitsdatum des Ziels gespeichert; Reihenfolge, Zuordnung, Übersicht und Pläne funktionieren daher wie bei jedem Ziel. Sie werden bei jeder Bearbeitung und jeder Aktualisierung neu berechnet.

**Der Abschnitt FIRE.** Zuerst die Werte, die in jedem Szenario gleich sind (Ihre Eingaben und die **Entnahmerate**), je einer pro Zeile. Dann pro Szenario eine aufklappbare Zeile, **Durchschnitt**, **Optimistisch** und **Pessimistisch**, mit Wachstums- und Inflationssatz, dem berechneten FIRE-Datum, wenn Sie es leer gelassen haben, und einem Abzeichen: **Geld reicht** (grün), wenn das Geld über die Lebenserwartung hinaus reicht, **Geld geht nie aus 🥳** (grün), wenn es nie aufgebraucht wird, **Nicht genug Geld** (gelb), wenn das FIRE-Datum nicht erreichbar ist oder das Geld früher ausgeht. Tippen Sie auf die Zeile, um die Werte aufzuklappen, die sich je Szenario unterscheiden:

| Zeile | Bedeutung |
|---|---|
| **FIRE-Datum** | Das Datum, mit „Alter N“, wenn das Geburtsdatum bekannt ist, oder *Nicht erreichbar vor <Datum>* |
| **Beitrag pro Monat** | Was jeden Monat investiert werden muss |
| **FIRE-Betrag · heutige Kaufkraft** | Der Betrag, den Sie haben sollen, wie ihn der Editor angibt |
| **FIRE-Betrag · zum FIRE-Datum** | Derselbe Betrag nach Inflation |
| **Ausgaben pro Monat · heutige Kaufkraft** | Die monatlichen Ausgaben nach FIRE |
| **Ausgaben pro Monat · zum FIRE-Datum** | Dieselben Ausgaben nach Inflation |
| **Geld reicht** | *bis <Datum> · Alter N*, *N Jahre · über die Lebenserwartung hinaus*, *Geld geht nie aus 🥳* oder *es fehlen …*, wenn das Datum nicht erreichbar ist |

Unter den Zeilen nennt eine Zeile „Sätze: …“ den Anbieter, von dem die Sätze stammen, oder *eingebaute Standardwerte*. Das Diagramm darunter zeigt den Verlauf des Geldes: den Wert in heutiger Kaufkraft von heute bis zum Ende des Geldes oder zum Horizont, mit drei farbigen Kurven, eine pro Szenario. Die gestrichelte Linie markiert das FIRE-Datum. Der Hinweis darunter lautet *Eine Projektion auf Basis Ihrer eigenen Zahlen; sie empfiehlt nichts.* Im Tab [Ziele]({{ page.base }}/screens/goals) zeigt das Ziel *FIRE am <Datum> · Alter N* oder *nicht erreichbar*.

**So wird gerechnet.** In Monatsschritten. Bis zum FIRE-Datum wächst der Beitrag um **Beitragswachstum % pro Jahr** (leer: mit der Inflation, behält also seine Kaufkraft), und das Geld wächst mit dem Satz des Szenarios. Nach dem FIRE-Datum wachsen die Ausgaben mit der Inflation, und der Rest wächst weiter mit dem Satz des Szenarios. Der Horizont ist das Geburtsdatum plus Lebenserwartung (standardmäßig 95) oder, ohne Geburtsdatum, 50 Jahre nach dem FIRE-Datum. *Geld geht nie aus* bedeutet: Das Geld ist in 200 Jahren nicht aufgebraucht und am Ende in heutiger Kaufkraft mindestens so viel wert wie zum FIRE-Datum.

**Standardwerte und Quellen.** Ohne eigenen Wert und ohne abgerufene Sätze nutzt die App Wachstum 7 % (optimistisch 10 %, pessimistisch 4 %), Inflation 3 % (2 %, 5 %) und eine Entnahmerate von 4 %. Sie stammen aus dem UBS Global Investment Returns Yearbook 2026 (Welt, 1900–2024: Aktien 9,8 % pro Jahr nominal, Anleihen 4,6 %, Inflation 2,9 %; 7 % entspricht etwa einer 60/40-Mischung), den 2-%-Inflationszielen der Zentralbanken sowie Bengen (1994) und der Trinity-Studie (1998) für die 4-%-Regel. Siehe auch [FIRE]({{ page.base }}/manual#fire) im Handbuch.

**Aktuelle Sätze.** **Aktualisieren** ruft Inflation und Marktwachstum für die Währung jedes FIRE-Ziels ab, von den Anbietern, die Sie in den [Einstellungen]({{ page.base }}/screens/settings) gewählt haben. Die Reihenfolge lautet: Ihr Wert unter **Erweiterte Annahmen**, dann der abgerufene Wert, dann der Standardwert. Ein fehlgeschlagener Abruf behält die vorherige Reihe und zeigt den Fehler an. Beide Wachstumsquellen sind Kursindizes ohne Dividenden und unterschätzen daher die Gesamtrendite.

**Meldungen.**

| Meldung | Was zu tun ist |
|---|---|
| *Geben Sie einen Zielbetrag, monatliche Ausgaben oder FIRE-Datum und Beitrag zusammen ein* | Füllen Sie eines der drei aus; sonst lässt sich nichts berechnen. |
| *Geben Sie ein FIRE-Datum oder einen Beitrag ein* | Der Zielbetrag ist bekannt, aber weder Datum noch Beitrag; geben Sie eines von beiden an. |
| *Das FIRE-Datum liegt in der Vergangenheit* | Wählen Sie ein Datum in der Zukunft. |
| *Das Geburtsdatum liegt in der Zukunft* | Korrigieren Sie das Geburtsdatum. |
| *Die Entnahmerate muss größer als null sein* | Geben Sie unter **Erweiterte Annahmen** eine Entnahmerate über 0 ein oder leeren Sie das Feld, um den Standardwert zu nutzen. |
| *Nicht erreichbar vor <Datum>* | Mit diesem Beitrag und diesen Sätzen wird der Zielbetrag vor diesem Datum (dem Ende des Horizonts) nicht erreicht; erhöhen Sie den Beitrag, senken Sie Zielbetrag oder Ausgaben, oder wählen Sie ein anderes Szenario. |
| *Keine Inflations- oder Wachstumsdaten für <Währung>* | Der Inflations- oder Wachstumsanbieter hat für diese Währung keine Reihe; wählen Sie einen anderen Anbieter oder geben Sie die Sätze unter **Erweiterte Annahmen** ein. |
| *ECB deckt <Währung> nicht ab* | Die EZB liefert nur die Inflation des Euroraums; wählen Sie in den Einstellungen einen anderen **Inflation**-Anbieter. |
| *OECD deckt <Währung> nicht ab* | Die OECD hat für diese Währung keine Reihe (zum Beispiel RUB); wählen Sie in den Einstellungen einen anderen **Inflation**-Anbieter. |

**Finanzierungsquellen.** Die Töpfe, die dieses Ziel finanzieren dürfen. **Topf verknüpfen** fügt einen hinzu, mit einem Beitragslimit:

- **Auto – bis zum Restbedarf**: Der Topf gibt, was das Ziel noch braucht, nachdem frühere Ziele ihren Anteil erhalten haben.
- **Fester Betrag in Zielwährung**.
- **% des Topfs**: höchstens dieser Anteil am Wert des Topfs.
- **% des Ziels**: höchstens dieser Anteil am Zielbetrag.

Die Vorschau im Editor zeigt, was die Verknüpfung heute beitragen würde. Limits sind Obergrenzen: Zielreihenfolge, verfügbare Ersparnisse und andere Verknüpfungen können den Beitrag verringern. Jede aufgeführte Quelle zeigt, was sie jetzt beiträgt und warum nicht mehr; **Verknüpfung bearbeiten** ändert das Limit, **Trennen** entfernt die Verknüpfung.

**Geplante Sparbeträge.** Die Pläne, die diesem Ziel zufließen, jeweils mit dem Betrag, den die Prognose ihm zuweist. Ein Plan, der von früheren Zielen vollständig aufgebraucht wird, erscheint hier nicht.

**Die Prognose lesen.** „Mit geplanten Sparbeträgen am 20.12.2026 finanziert · rechtzeitig“ bedeutet: Die bis zu diesem Datum aufsummierten Pläne decken den Restbedarf vor dem Fälligkeitsdatum. „Bis … decken geplante Sparbeträge bis zu … · es fehlen …“ bedeutet, dass sie das nicht tun; fügen Sie einen Plan hinzu, verschieben Sie das Datum oder senken Sie den Zielbetrag.
