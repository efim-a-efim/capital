---
layout: default
lang: de
base: "/de"
key: "privacy"
title: Datenschutzerklärung
class: doc
---
# Datenschutzerklärung

<p class="meta">Capital für Android (Paket <code>dev.capital</code>) · Entwickler: {{ site.developer }} · Gültig ab 30. September 2026</p>

## Zusammenfassung

- Capital hat keine Benutzerkonten, keine Analyse, keine Werbung, keine Absturzberichte und keine vom Entwickler betriebenen Server. Der Entwickler erhält Ihre Daten nie.
- Ihre Finanzdaten werden ausschließlich auf Ihrem Gerät gespeichert, in einem Ordner Ihrer Wahl. Sie können sie mit einem Passwort verschlüsseln.
- Der einzige Netzwerkverkehr sind die Anfragen, die die App auf Ihre Veranlassung an die Betreiber von Preis- und Blockchain-Daten sendet, die Sie in den Einstellungen auswählen. Diese Anfragen enthalten die öffentlichen Wallet-Adressen, Token-Verträge und Währungscodes, die Sie erfassen, sowie gegebenenfalls einen API-Schlüssel, den Sie für diesen Betreiber eingegeben haben.

## Was die App auf Ihrem Gerät speichert

**Im von Ihnen gewählten Ordner.** Töpfe, Positionen, Wallet-Adressen, Ziele, Verknüpfungen, geplante Sparbeträge, zwischengespeicherte Kurse und die zu diesen Daten gehörenden Einstellungen. Die Dateien sind Klartext, sofern Sie nicht die Verschlüsselung einschalten (Einstellungen → Sicherheit). Mit aktiver Verschlüsselung wird jede Datei mit AES-256-GCM verschlüsselt, mit einem Schlüssel, der per Argon2id aus Ihrem Passwort abgeleitet wird. Eine Passwortwiederherstellung gibt es nicht.

**Im app-privaten Speicher** (für andere Apps nicht zugänglich):

| Element | Zweck |
|---|---|
| Berechtigung für den gewählten Ordner | Ordner beim nächsten Start wieder öffnen |
| Von Ihnen eingegebene API-Schlüssel der Anbieter | Werden nur an den Betreiber gesendet, der sie ausgegeben hat; mit einem im Android Keystore gehaltenen Schlüssel verschlüsselt; von Snapshots, Exporten und Systembackups ausgeschlossen |
| Sperreinstellungen | Entsperren des verschlüsselten Ordners ohne Passwort: eine Kopie des Datenschlüssels, verschlüsselt mit einem aus Ihrer PIN abgeleiteten und an den Android Keystore gebundenen Schlüssel. Die PIN selbst wird nicht gespeichert |
| Sprach- und Designauswahl | Einstellungen der Benutzeroberfläche |

Android-Backup und die Übertragung von Gerät zu Gerät sind für die App deaktiviert, sodass das System nichts davon zu Google oder auf ein anderes Gerät kopiert.

## Was Ihr Gerät verlässt

Capital kontaktiert nur die Betreiber, die Sie in den Einstellungen wählen, nur über HTTPS und nur, wenn Sie aktualisieren oder Quellen testen. Jede Anfrage wird beantwortet und verworfen; die App speichert die zurückgegebenen Guthaben und Kurse in Ihrem Ordner, nicht die Anfrage.

| Gesendete Daten | An wen | Warum |
|---|---|---|
| Von Ihnen hinzugefügte öffentliche Wallet-Adressen | Der für diese Chain gewählte Betreiber von Blockchain-Daten | Guthaben und Token-Bestände der Adresse abrufen |
| Token-Vertragsadressen und Asset-IDs | Der von Ihnen gewählte Betreiber für Kryptopreise | Vermögenswerte bewerten |
| Währungscodes | Der von Ihnen gewählte Betreiber für Wechselkurse | Zwischen Währungen umrechnen |
| Der API-Schlüssel, den Sie für einen Betreiber eingegeben haben | Nur dieser Betreiber | Ihr eigenes Konto bei diesem Betreiber authentifizieren |

Wie bei jeder Internetanfrage sieht jeder Betreiber auch Ihre IP-Adresse. Die Betreiber sind vom Entwickler unabhängig und verarbeiten die Anfrage nach ihren eigenen Bedingungen und Datenschutzrichtlinien, die in der App unter Einstellungen → Quellen / Nachweise verlinkt sind:

| Daten | Betreiber |
|---|---|
| Bitcoin | [Blockstream](https://blockstream.info), [mempool.space](https://mempool.space) |
| Ethereum und ERC-20-Token | [PublicNode](https://publicnode.com), [Alchemy](https://www.alchemy.com), [Blockscout](https://www.blockscout.com), [Ethplorer](https://ethplorer.io) |
| TON und Jettons | [TON Center](https://toncenter.com), [TonAPI](https://tonapi.io) |
| TRON und TRC-20-Token | [TronGrid](https://www.trongrid.io), [PublicNode](https://publicnode.com) |
| Kryptopreise | [DefiLlama](https://defillama.com), [CoinGecko](https://www.coingecko.com), [CoinPaprika](https://coinpaprika.com) |
| Wechselkurse | [Frankfurter](https://frankfurter.dev), [Europäische Zentralbank](https://www.ecb.europa.eu) |

Nichts wird anderswohin gesendet. Keine Daten werden verkauft, für Werbung weitergegeben oder zur Profilbildung verwendet. Öffentliche Blockchain-Abfragen verraten, dass sich jemand unter Ihrer IP-Adresse für die erfasste Adresse interessiert; verwenden Sie ein VPN, wenn Ihnen das wichtig ist.

## Was die App niemals tut

- Sie fragt niemals nach privaten Schlüsseln oder Seed-Phrasen, speichert oder überträgt sie nicht. Sie kann keine Transaktionen signieren oder senden.
- Sie überweist niemals Geld. Zielzuordnungen sind Berechnungen, die Ihnen angezeigt werden, und nichts weiter.
- Sie kontaktiert niemals den Entwickler. Es gibt keine Telemetrie, keine Update-Prüfung in der App und keine Push-Benachrichtigungen.

## Berechtigungen

| Berechtigung | Verwendung |
|---|---|
| Internet | Anfragen an die oben aufgeführten Betreiber |
| Ordnerzugriff | Von Ihnen über die Android-Ordnerauswahl für den gewählten Ordner erteilt; die App kann keine anderen Ordner lesen |
| Biometrie | Entsperren per Fingerabdruck oder Gesicht über den systemeigenen Android-Dialog; die App erhält nur Erfolg oder Fehlschlag, niemals biometrische Daten |

## Synchronisierung und Backups

Capital synchronisiert selbst nichts. Wenn Sie den Ordner in ein Sync-Tool legen (Syncthing, Nextcloud, Google Drive, …), gelten für die von diesem Tool erstellten Kopien dessen Datenschutzbestimmungen. Die Dateien sind Klartext, sofern die Verschlüsselung nicht aktiv ist; frühere unverschlüsselte Kopien, die vor dem Einschalten der Verschlüsselung entstanden sind, bleiben für jeden lesbar, der sie besitzt.

**Backup exportieren** in den Einstellungen schreibt eine einzelne Datei an einen Ort Ihrer Wahl. Sie enthält dieselben Daten und ist nur so gut geschützt wie dieser Ort.

## Löschen Ihrer Daten

Löschen Sie den gewählten Ordner (und alle Kopien, die Ihr Sync-Tool angelegt hat) und deinstallieren Sie die App. Bei der Deinstallation wird der app-private Speicher entfernt, einschließlich Anbieterschlüsseln und Sperreinstellungen. Der Entwickler besitzt keine Daten, die gelöscht werden könnten, und kann nichts in Ihrem Namen löschen. Die von Ihnen abgefragten Betreiber können Anfrageprotokolle nach ihren eigenen Aufbewahrungsregeln speichern.

## Spenden {#donations}

Die App (Herz-Schaltfläche in der Übersicht) und diese Website zeigen die Wallet-Adressen des Entwicklers für freiwillige Trinkgelder. Ein Trinkgeld ist eine Überweisung, die Sie aus Ihrer eigenen Wallet an eine dieser Adressen vornehmen, zu den Bedingungen Ihrer Wallet und des von Ihnen genutzten Netzwerks. Capital ist daran nicht beteiligt: Die App wickelt keine Zahlung ab, kann nicht sehen, ob Sie etwas gesendet haben, zeichnet nichts darüber auf und ändert nichts – keine Funktion wird freigeschaltet oder verändert. Ein Trinkgeld dient einzig dazu, den Entwickler zu unterstützen. Wie bei jeder Blockchain-Transaktion legt das Senden an eine öffentliche Adresse Ihre Absenderadresse in diesem Netzwerk offen.

## Kinder

Capital ist ein Werkzeug für die persönlichen Finanzen von Erwachsenen. Es richtet sich nicht an Kinder unter 13 Jahren und erhebt wissentlich keine Daten von ihnen.

## Änderungen dieser Erklärung

Die aktuelle Fassung finden Sie stets unter [{{ site.url }}{{ page.base }}/privacy]({{ page.base }}/privacy). Wesentliche Änderungen werden in den Versionshinweisen der Version aufgeführt, mit der sie eingeführt werden.

## Kontakt

{{ site.developer }} · [{{ site.contact }}](mailto:{{ site.contact }}) · [Issue-Tracker]({{ site.repo }}/issues)
