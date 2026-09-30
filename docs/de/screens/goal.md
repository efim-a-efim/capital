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

**Finanzierungsquellen.** Die Töpfe, die dieses Ziel finanzieren dürfen. **Topf verknüpfen** fügt einen hinzu, mit einem Beitragslimit:

- **Auto – bis zum Restbedarf**: Der Topf gibt, was das Ziel noch braucht, nachdem frühere Ziele ihren Anteil erhalten haben.
- **Fester Betrag in Zielwährung**.
- **% des Topfs**: höchstens dieser Anteil am Wert des Topfs.
- **% des Ziels**: höchstens dieser Anteil am Zielbetrag.

Die Vorschau im Editor zeigt, was die Verknüpfung heute beitragen würde. Limits sind Obergrenzen: Zielreihenfolge, verfügbare Ersparnisse und andere Verknüpfungen können den Beitrag verringern. Jede aufgeführte Quelle zeigt, was sie jetzt beiträgt und warum nicht mehr; **Verknüpfung bearbeiten** ändert das Limit, **Trennen** entfernt die Verknüpfung.

**Geplante Sparbeträge.** Die Pläne, die diesem Ziel zufließen, jeweils mit dem Betrag, den die Prognose ihm zuweist. Ein Plan, der von früheren Zielen vollständig aufgebraucht wird, erscheint hier nicht.

**Die Prognose lesen.** „Mit geplanten Sparbeträgen am 20.12.2026 finanziert · rechtzeitig“ bedeutet: Die bis zu diesem Datum aufsummierten Pläne decken den Restbedarf vor dem Fälligkeitsdatum. „Bis … decken geplante Sparbeträge bis zu … · es fehlen …“ bedeutet, dass sie das nicht tun; fügen Sie einen Plan hinzu, verschieben Sie das Datum oder senken Sie den Zielbetrag.
