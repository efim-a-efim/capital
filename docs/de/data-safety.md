---
layout: default
lang: de
base: "/de"
key: "data-safety"
title: Angaben zur Datensicherheit
class: doc
---
# Angaben zur Datensicherheit

<p class="meta">Antworten für das Formular in der Google Play Console (Richtlinien und Programme → App-Inhalte → Datensicherheit) mit der jeweiligen Begründung. Geprüft anhand von Version 2.2.1 am 30. September 2026. Die <a href="{{ page.base }}/privacy">Datenschutzerklärung</a> beschreibt dieselben Fakten für Nutzerinnen und Nutzer.</p>

## Wie die App mit Daten umgeht

Capital hat kein Backend. Alles, was der Nutzer eingibt, bleibt in einem Ordner auf dem Gerät. Die einzigen Daten, die das Gerät jemals verlassen, sind die, die die App auf Anweisung des Nutzers an die Datenbetreiber von Drittanbietern sendet, die der Nutzer in den Einstellungen auswählt: öffentliche Wallet-Adressen, Token-Vertrags-IDs, Währungscodes sowie gegebenenfalls ein API-Schlüssel, den der Nutzer für diesen Betreiber eingegeben hat. Die Betreiber beantworten die Anfrage; die App speichert die zurückgegebenen Guthaben und Kurse lokal und behält keine Kopie der Anfrage. Kein SDK in der App sendet Daten nach Hause: Die Abhängigkeiten sind ausschließlich AndroidX, Kotlin, OkHttp, Bouncy Castle und ZXing (QR-Darstellung, offline). Der Trinkgeld-Bildschirm zeigt fest in die App eingebaute, statische Adressen und sendet nichts.

Google Play wertet Daten als *erhoben*, sobald sie vom Gerät übertragen werden – auch wenn kein Server des Entwicklers beteiligt ist und die Verarbeitung nur flüchtig erfolgt. Die Angabe lautet daher nicht „erhebt nichts“. Es handelt sich um einen einzigen, flüchtig verarbeiteten, optionalen Datentyp.

## Antworten im Formular

### Überblick

| Frage | Antwort |
|---|---|
| Does your app collect or share any of the required user data types? (Erhebt oder teilt Ihre App einen der erforderlichen Nutzerdatentypen?) | **Yes** (Ja) |
| Is all of the user data collected by your app encrypted in transit? (Werden alle von Ihrer App erhobenen Nutzerdaten bei der Übertragung verschlüsselt?) | **Yes** (Ja) — nur HTTPS; Klartext-Datenverkehr ist im Manifest deaktiviert |
| Do you provide a way for users to request that their data is deleted? (Bieten Sie Nutzern die Möglichkeit, die Löschung ihrer Daten zu beantragen?) | **Yes** (Ja) — nach Abschluss der Anfrage wird nichts aufbewahrt; das erfüllt die Regel „deleted within 90 days of collection“ (innerhalb von 90 Tagen nach der Erhebung gelöscht) für das Abzeichen. Nutzer löschen die Daten auf dem Gerät, indem sie den Ordner löschen und die App deinstallieren; siehe Datenschutzerklärung. |

### Datentypen

Wählen Sie genau einen Typ aus.

| Kategorie | Datentyp | Collected (Erhoben) | Shared (Weitergegeben) | Ephemeral (Flüchtig verarbeitet) | Required or optional (Erforderlich oder optional) | Purposes (Zwecke) |
|---|---|---|---|---|---|---|
| Finanzinformationen | Sonstige Finanzinformationen | Ja | Nein | **Ja** | **Optional** | App-Funktionen |

Was der Typ umfasst: öffentliche Blockchain-Adressen, die der Nutzer erfasst, die darauf gefundenen Token-Verträge und die Währungscodes der Positionen des Nutzers. Sie werden an den vom Nutzer gewählten Datenbetreiber übertragen, damit Guthaben und Kurse abgerufen werden können, für die Dauer der Anfrage im Arbeitsspeicher gehalten und danach verworfen.

Warum **nicht weitergegeben**: Die Übertragung erfolgt direkt vom Gerät an den vom Nutzer gewählten Betreiber, bei einer vom Nutzer gestarteten Aktualisierung, nachdem die App den Nutzer in den Einstellungen darüber informiert hat, welcher Betreiber abgefragt wird und dass die Anfrage diesem Betreiber die Adresse und die IP-Adresse offenlegt. Das ist die Ausnahme für eine „vom Nutzer initiierte Aktion, bei der der Nutzer vernünftigerweise erwartet, dass die Daten weitergegeben werden“. Der Entwickler erhält nichts und hat keine Dienstleister.

Warum **optional**: Die App ist allein mit manuellen Positionen vollständig nutzbar. Adressen und API-Schlüssel werden freiwillig eingegeben.

Vom Nutzer eingegebene API-Schlüssel werden nur an den Betreiber gesendet, der sie ausgegeben hat. Sie sind die Zugangsdaten des Nutzers für den eigenen Dienst dieses Betreibers und werden nicht als eigener Nutzerdatentyp angegeben; falls ein Prüfer nachfragt, beschreiben Sie sie wie oben.

### Typen, die **nicht** erhoben werden

Jede andere Kategorie lautet „Nein“: kein Standort, keine personenbezogenen Daten, keine Kontakte, keine Nachrichten, keine Fotos und Videos, keine Dateien und Dokumente, keine App-Aktivitäten, kein Web-Browsing, keine App-Informationen und -Leistung (keine Absturzprotokolle, keine Diagnosedaten), keine Geräte- oder anderen IDs. IP-Adressen erreichen die Betreiber als Teil jeder HTTPS-Anfrage und werden von der App für keinen Zweck verwendet.

Die Finanzdaten des Nutzers (Positionen, Ziele, Pläne) werden ausschließlich auf dem Gerät verarbeitet und fallen nicht in den Geltungsbereich des Formulars.

### Sicherheitspraktiken

| Punkt | Antwort |
|---|---|
| Independent security review (MASA) (Unabhängige Sicherheitsüberprüfung) | Nein |
| Committed to follow the Families policy (Verpflichtung zur Einhaltung der Richtlinien für familienfreundliche Apps) | Nein (keine App für Kinder) |

## Weitere Angaben auf der Seite „App-Inhalte“

| Angabe | Antwort |
|---|---|
| Datenschutzerklärung (URL) | `{{ site.url }}/privacy` |
| Werbung | Nein, die App enthält keine Werbung |
| App-Zugriff | Alle Funktionen sind ohne besonderen Zugriff verfügbar. Keine Anmeldung. Anbieter-API-Schlüssel sind optional; für jeden Anbieter gibt es eine Standardquelle ohne Schlüssel. |
| Einstufung des Inhalts (IARC) | Fragebogen „Utility / productivity“ (Dienstprogramm / Produktivität); keine Gewalt, keine sexuellen Inhalte, kein Glücksspiel, keine kontrollierten Substanzen, keine Nutzerinteraktion und keine Standortfreigabe. Erwartetes Ergebnis: Everyone / PEGI 3. |
| Zielgruppe und Inhalt | 18 Jahre und älter (Werkzeug für persönliche Finanzen; nicht für Kinder konzipiert) |
| News app (Nachrichten-App) | Nein |
| COVID-19 contact tracing and status (COVID-19-Kontaktverfolgung und -Status) | Nein |
| Datensicherheit | Wie oben |
| Government app (Behörden-App) | Nein |
| Finanzfunktionen | Siehe [Angaben zu Finanzfunktionen]({{ page.base }}/financial-features) |
| Health apps (Gesundheits-Apps) | Keine Gesundheitsfunktionen |

## Was bei Änderungen an der App zu aktualisieren ist

Prüfen Sie diese Seite erneut, wenn eine Version Analysefunktionen, Absturzberichte, Konten, einen vom Entwickler betriebenen Server, ein neues SDK mit Netzwerkzugriff oder die Weitergabe von Daten an eine andere App auf dem Gerät hinzufügt. Jede dieser Änderungen wirkt sich auf das Formular aus.
