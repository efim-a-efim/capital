---
layout: screen
lang: de
base: "/de"
key: "screens/brokers"
screen: brokers
title: Broker
---
# Broker

**Was es ist.** Ihre reinen Lesezugriffsverbindungen zu Depot- und Forex-Konten. Ein Brokerkonto besteht aus dem Broker, der Konto- oder Query-ID und den Zugangsdaten des Brokers; beim Aktualisieren liest die App den Gesamtwert des Kontos in dessen Basiswährung. Konten liegen auf diesem Bildschirm, nicht in Töpfen: Ein Topf verknüpft sich nur mit einem Konto, und Töpfe bleiben der Ort, an dem Ihre Ersparnisse gezählt werden.

**Was jede Zeile zeigt.** Den Namen des Kontos, den Broker und die ID, den zuletzt gelesenen Wert in der Währung des Kontos und in Ihrer Standardwährung, wann er ermittelt und wann er abgerufen wurde, sowie den Topf, mit dem das Konto verknüpft ist: **Verknüpft mit …** öffnet diesen Topf, *Mit keinem Topf verknüpft* bedeutet, dass es noch nichts zählt. **Bearbeiten** ändert Namen, Broker oder ID; **Löschen** entfernt das Konto und, falls verknüpft, die Position, die es verknüpft hat.

**Konto hinzufügen.** Tippen Sie auf **+**, geben Sie einen Namen ein, wählen Sie den Broker und geben Sie die ID ein, die dieser Broker verwendet: die Flex-Query-ID bei Interactive Brokers, die Konto-ID bei OANDA, die Kontonummer bei Trading 212. Bei SnapTrade tippen Sie auf **Broker über SnapTrade verbinden**, kehren zurück, tippen auf **Konten abrufen** und wählen eines aus. Speichern Sie. Währung und Wert erscheinen nach der nächsten Aktualisierung.

**Salden unter diesem Betrag ignorieren.** Aktivieren Sie das Feld und geben Sie einen Betrag in Ihrer Standardwährung ein (standardmäßig 1), um Kleinstbeträge aus Ihren Ersparnissen herauszuhalten: Liegt der Wert des Kontos, umgerechnet mit den zwischengespeicherten Kursen, unter diesem Betrag, zählt die verknüpfte Position als 0 und die Zeile zeigt *Zählt als 0: unter …*. Der tatsächliche Wert bleibt auf diesem Bildschirm sichtbar. Ohne Kurs für die Währung des Kontos wird nichts ignoriert.

**Mit einem Topf verknüpfen.** Öffnen Sie den Topf, tippen Sie auf **Position hinzufügen**, stellen Sie **Erfassung** auf **Brokerkonto** und wählen Sie das Konto; lassen Sie den Namen leer, um den Kontonamen zu verwenden. Ein Konto kann jeweils in einem Topf sein. **Bearbeiten / verschieben** an der Position verschiebt sie in einen anderen Topf; das Löschen der Position löst die Verknüpfung, ohne das Konto zu löschen.

**Zugangsdaten.** Das Token oder der Schlüssel jedes unterstützten Brokers (Interactive Brokers, OANDA, Trading 212, SnapTrade); ein Satz pro Broker gilt für alle Konten dieses Brokers. Sie werden mit einem im Android Keystore gehaltenen Schlüssel verschlüsselt, nie in den Datenordner geschrieben, von Exporten und Systembackups ausgeschlossen und nur an den Broker gesendet, der sie ausgestellt hat. **Einrichtungsanleitung für Brokerkonten** öffnet [Broker- und Forex-Konten]({{ page.base }}/accounts), wo die Schritte für jeden Broker stehen.

**Aktualisieren.** Das Aktualisieren-Symbol auf diesem Bildschirm liest jedes Konto; die Aktualisierung in einem Topf liest nur die Konten, die mit diesem Topf verknüpft sind. Ein Konto, das nicht gelesen werden kann, behält seinen letzten Wert und zeigt die Meldung des Brokers unter seiner Zeile. Die App liest nur: Sie erteilt nie Orders und bewegt kein Geld.
