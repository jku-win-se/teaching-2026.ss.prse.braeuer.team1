# Anforderungsdokument: SmartHome Orchestrator

> **📝 Team-Aufgabe:** Ersetze *SmartHome Orchestrator* in diesem Dokument durch euren eigenen Projektnamen. Wählt einen Namen, der die Identität eures Teams und das Produkt, das ihr entwickelt, widerspiegelt.

## 1. Einleitung

Dieses Dokument spezifiziert die Anforderungen an den **SmartHome Orchestrator**, eine Heimautomatisierungsanwendung, die im Rahmen des Software Engineering Praktikums (SS 2026) an der JKU Linz entwickelt wird.

Die Anwendung ermöglicht es Benutzern, Smart-Home-Geräte zu verwalten, Automatisierungsregeln zu definieren, Aktionen zu planen und den Energieverbrauch zu überwachen – alles über eine einheitliche Oberfläche. Das System arbeitet mit **simulierten virtuellen Geräten**, um die Entwicklungsumgebung unabhängig und zugänglich zu halten; die Integration mit realer IoT-Hardware wird als optionale Erweiterung behandelt.

Dieses Dokument bildet die technische und vertragliche Grundlage für die gesamte Sprint-Planung, Akzeptanzkriterien und Release-Bewertungen im Laufe des Semesters. Anforderungen werden entweder als **funktional (FR)** oder **nicht-funktional (NFR)** kategorisiert.

---

## 2. Motivation

Moderne Haushalte sind zunehmend auf ein wachsendes Ökosystem smarter Geräte angewiesen wie Thermostate, smarte Lampen, Bewegungssensoren, Schlösser und Haushaltsgeräte. Diese werden typischerweise jeweils über eigene proprietäre Anwendungen gesteuert. Diese Fragmentierung erzeugt Reibung: Benutzer müssen zwischen mehreren Apps wechseln, können keine geräteübergreifenden Automatisierungsregeln definieren und haben keine einheitliche Übersicht über den Gerätezustand oder den Energieverbrauch.

Der **SmartHome Orchestrator** löst dieses Problem durch eine einzige Plattform, die:

- **Geräteverwaltung zentralisiert** über Räume und Gerätetypen hinweg und den Bedarf an separaten Einzel-Apps entfernt.
- **Nicht-technische Benutzer befähigt** durch eine klare, visuelle Oberfläche, die keine Programmierkenntnisse erfordert, um Automatisierungsregeln zu definieren.
- **Energiebewusstsein fördert** durch das Erfassen und Visualisieren des Geräteverbrauchs und damit nachhaltigere Konsumgewohnheiten unterstützt.
- **Manuellen Aufwand reduziert**, indem Benutzer wiederkehrende Aktionen planen (z. B. alle Lichter um Mitternacht ausschalten) und bedingungsbasierte Auslöser setzen können (z. B. Heizung einschalten, wenn die Temperatur unter 18 °C fällt).
- **Eine sichere Lernumgebung bietet** für Studierende, um professionelles Software Engineering zu üben: Anforderungsengineering, iterative Auslieferung, Architekturdesign, Testing und CI/CD in einer realistischen, domänenreichen Anwendung.

---

## 3. Anforderungen

### Funktionale Anforderungen

| ID    | Beschreibung |
|-------|--------------|
| FR-01 | Das System soll es einem Benutzer ermöglichen, sich mit einer eindeutigen E-Mail-Adresse und einem Passwort zu registrieren. |
| FR-02 | Das System soll es einem registrierten Benutzer ermöglichen, sich sicher ein- und auszuloggen. |
| FR-03 | Das System soll es authentifizierten Benutzern ermöglichen, Räume zu erstellen, umzubenennen und zu löschen (z. B. Wohnzimmer, Küche). |
| FR-04 | Das System soll es Benutzern ermöglichen, virtuelle Smart-Geräte einem Raum hinzuzufügen, mit Angabe von Typ (Schalter, Dimmer, Thermostat, Sensor, Jalousie/Rollo) und Name. |
| FR-05 | Das System soll es Benutzern ermöglichen, vorhandene Geräte zu entfernen oder umzubenennen. |
| FR-06 | Das System soll es Benutzern ermöglichen, ein Gerät manuell zu steuern: Schalter ein-/ausschalten, Dimmer-Helligkeit einstellen (0–100 %), Soll-Temperatur des Thermostats setzen, Jalousien öffnen/schließen und einen aktuellen Wert in einen Sensor einspeisen (z. B. zu Testzwecken). |
| FR-07 | Das System soll den aktuellen Zustand jedes Geräts in der Benutzeroberfläche in Echtzeit pflegen und anzeigen. |
| FR-08 | Das System soll für jede manuelle oder automatisierte Zustandsänderung einen Aktivitätslog-Eintrag erfassen, inklusive Zeitstempel, Gerät und Akteur (Benutzer oder Regel). |
| FR-09 | Das System soll es Benutzern ermöglichen, bedingungslose zeitbasierte Zeitpläne für Geräteaktionen zu konfigurieren (z. B. Lampe jeden Werktag um 07:00 Uhr einschalten). Im Gegensatz zu Regelauslösern (FR-11) führen Zeitpläne eine feste Aktion nach einem wiederkehrenden Zeitmuster aus, ohne eine zusätzliche Bedingung auszuwerten. |
| FR-10 | Das System soll eine Regel-Engine bereitstellen, die Bedingungs-Aktions-Regeln der Form auswertet: WENN \<Auslöser\> DANN \<Aktion\> (z. B. WENN Bewegungssensor aktiv DANN Flurbeleuchtung einschalten). |
| FR-11 | Das System soll mindestens drei Auslösertypen für Regeln unterstützen: zeitbasiert, schwellenwertbasiert (Sensorwert) und ereignisbasiert (Gerätezustandsänderung). |
| FR-12 | Das System soll den Benutzer (In-App-Benachrichtigung) informieren, wenn eine Regel ausgeführt wird oder wenn die Ausführung einer Regel fehlschlägt. |
| FR-13 | Das System soll zwei Benutzerrollen unterstützen: **Eigentümer** (vollständiger Zugriff) und **Mitglied** (nur Steuerung, keine Geräte-/Regelverwaltung). |
| FR-14 | Das System soll ein Energieverbrauchs-Dashboard anzeigen, das den geschätzten Stromverbrauch pro Gerät, pro Raum und als Haushalt gesamt ausweist, aggregiert nach Tag und Woche. |
| FR-15 | Das System soll Planungskonflikte erkennen und den Benutzer warnen (zwei Regeln, die dasselbe Gerät gleichzeitig in widersprüchliche Zustände versetzen würden). |
| FR-16 | Das System soll es Benutzern ermöglichen, das Aktivitätslog und die Energieverbrauchszusammenfassung als CSV-Datei zu exportieren. |
| FR-17 | Das System soll eine „Szenen"-Funktion bereitstellen: eine benannte Gruppe von Gerätezuständen, die mit einer einzigen Aktion aktiviert werden kann (z. B. „Filmabend" dimmt die Lichter und schließt die Jalousien). |
| FR-18 | Das System soll eine optionale Integrationsschicht für ein physisches IoT-Protokoll unterstützen (z. B. MQTT), um echte Geräte anzubinden. |
| FR-19 | Das System soll es Benutzern ermöglichen, einen vollständigen Tag zu simulieren, indem Bedingungen festgelegt werden (z. B. initiale Sensorwerte, Startzeit, aktive Regeln) und die resultierenden Gerätezustandsänderungen im beschleunigten Zeitraffer wiedergegeben werden – ohne das Live-System zu beeinflussen. |
| FR-20 | Das System soll es dem Eigentümer ermöglichen, weitere Mitglieder per E-Mail-Adresse einzuladen und deren Zugang jederzeit zu widerrufen. |
| FR-21 | Das System soll es Benutzern ermöglichen, einen „Urlaubsmodus" zu aktivieren, der für einen festgelegten Zeitraum automatisch einen benannten Zeitplan (definiert über FR-09) anwendet und dabei die normalen Tages-Zeitpläne für diesen Zeitraum überschreibt. |

### Nicht-funktionale Anforderungen

| ID     | Kategorie       | Beschreibung |
|--------|-----------------|--------------|
| NFR-01 | Performance     | Das System soll auf jede Benutzerinteraktion (Schaltfläche, Formularabsendung) innerhalb von **2 Sekunden** unter normaler Last (bis zu 10 gleichzeitig simulierten Geräten) reagieren. |
| NFR-02 | Sicherheit      | Alle Benutzerpasswörter sollen mit einem starken Einweg-Hashing-Algorithmus gespeichert werden (z. B. bcrypt); Passwörter im Klartext dürfen niemals gespeichert oder protokolliert werden. |
| NFR-03 | Test            | Die Testsuite soll eine minimale **Code-coverage von 75 %** über alle nicht-UI-Klassen erreichen. |
| NFR-04 | Codequalität    | Die Codebasis soll **keine kritischen oder hohen PMD-Verstöße** enthalten; die Einhaltung wird bei jedem CI-Lauf automatisch geprüft, und ein Build mit kritischen oder hohen Befunden soll fehlschlagen. |
| NFR-05 | Zuverlässigkeit | Das System soll einzelne Gerätefehler und ungültige Gerätezustände fehlertolerant behandeln, den Fehler protokollieren und den Betrieb aller nicht betroffenen Geräte ohne Neustart fortsetzen. |
| NFR-06 | Dokumentation   | Alle öffentlichen Klassen, Interfaces und Methoden, die Teil der Kern-Domäne oder der API-Schicht sind, sollen Javadoc-Kommentare enthalten, die ihren Zweck, ihre Parameter und Rückgabewerte beschreiben. |

---

## 4. Nicht im Projektumfang

Die folgenden Punkte sind ausdrücklich **nicht** im Umfang dieses Projekts enthalten, sofern nicht anderweitig im Release-Plan festgelegt:

- Native mobile Anwendung (iOS / Android).
- Echtzeit-Push-Benachrichtigungen per SMS oder E-Mail.
- Integration mit kommerziellen Smart-Home-Plattformen (Google Home, Amazon Alexa, Apple HomeKit).
- Multi-Haushalt- / Multi-Mandanten-Unterstützung.
- Hardwarebeschaffung oder physische Geräteinstallation.
