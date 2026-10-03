---
layout: screen
lang: de
base: "/de"
key: "screens/bucket"
screen: bucket
title: Topf
---
# Topf

**Was es ist.** Ein einzelner Topf mit seinen Positionen. Sie erreichen ihn, indem Sie im Tab [Töpfe]({{ page.base }}/screens/buckets) auf eine Karte tippen; **← Alle Töpfe** führt zurück.

**Kopfbereich.** Der Wert des Topfs, dann zwei Zahlen, die nur zusammen Sinn ergeben: **Zugeordnet**, der Teil, den verknüpfte Ziele beanspruchen, und **Verfügbar**, der Rest. **Topf bearbeiten** öffnet Name, Währung und die Portfolio-Schalter. **Topf löschen** entfernt den Topf samt Positionen nach einer Bestätigung.

**Positionen.** Jede Position zeigt ihren Namen, ihren Wert in der Währung des Topfs, die Art der Erfassung (*Manuell*, *Wallet* oder *Brokerkonto*), die native Menge, den Zeitpunkt, zu dem der Wert ermittelt wurde, und wann er zuletzt abgerufen wurde. **Bearbeiten / verschieben** ändert sie oder verschiebt sie in einen anderen Topf; **Löschen** entfernt sie.

**Position hinzufügen** öffnet den Positionseditor:

- **Manuell**: ein Name, ein Währungs- oder Vermögenswert-Code und die Menge. Verwenden Sie diese Art für alles, was die App nicht selbst auslesen kann.
- **Wallet**: Wählen Sie die Chain (BTC, ETH, TON, TRX) und fügen Sie eine öffentliche Adresse ein. Beim Aktualisieren liest die App das native Guthaben und bei ETH, TON und TRX auch die fungiblen Token auf dieser Adresse. Öffnen Sie den Editor erneut und tippen Sie auf **Token abrufen**, um sie zu sehen und diejenigen auszuschalten, die nicht gezählt werden sollen.
- **Brokerkonto**: Wählen Sie eines der Konten, die Sie auf dem Bildschirm [Broker]({{ page.base }}/screens/brokers) hinzugefügt haben; lassen Sie den Namen leer, um den Kontonamen zu verwenden. Beim Aktualisieren liest die App den Gesamtwert des Kontos in dessen Basiswährung. Ein Konto kann mit einem Topf verknüpft werden; das Löschen der Position löst die Verknüpfung, ohne das Konto zu löschen.

**Token und „nicht gezählt“.** Ein Token wird über seine Vertragsadresse identifiziert. Er zählt nur, wenn Ihre Preisquelle genau diesen Vertrag listet; andernfalls wird er als *Unbekannter Token · nicht gezählt* aufgeführt und fließt nicht in die Summen ein. So bleibt etwa ein per Airdrop zugeschickter gefälschter „USDT“ aus Ihren Ersparnissen heraus.

**Portfoliomodus.** Ist er aktiviert, zeigt der Bildschirm zusätzlich eine Tabelle mit Wert, Ist-Anteil, Zielanteil und Abweichung je Vermögenswert sowie eine Schaltfläche **Rebalancing**, die nach einem Betrag fragt und auflistet, was Sie kaufen sollten. Verkäufe erscheinen nur, wenn *Verkäufe beim Rebalancing erlauben* aktiviert ist. Es wird nichts gehandelt.
