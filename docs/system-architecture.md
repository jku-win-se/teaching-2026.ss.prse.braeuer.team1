# Systemdokumentation

## Überblick

SmartHomie ist eine Webanwendung zur zentralen Steuerung und Automatisierung eines Smart Homes.
Das System umfasst die Verwaltung von Benutzerinnen und Benutzern, Räumen, Geräten, Regeln, Zeitplänen, Szenen,
Benachrichtigungen und Energieverbrauchsdaten.

Der fachliche Umfang reicht von manueller Gerätebedienung über regelbasierte Automatisierung bis hin zu
Echtzeit-Statusupdates und Auswertungen für den Energieverbrauch.

## Architektur

- UI: Frontend als React-Anwendung im Ordner frontend (Vite, TypeScript, Tailwind CSS).

- Domänenlogik: Geschäftslogik im Backend mit klarer Ordnerstruktur:

```
resource/   --> REST-Endpunkte (JAX-RS)
service/    --> Business-Logik
repository/ --> Datenzugriff (Panache)
entity/     --> JPA-Entities
dto/        --> Data Transfer Objects
mapper/     --> Entity <-> DTO Mapping
websocket/  --> Echtzeit-Kommunikation
iot/        --> IoT-Integrationsschicht (MQTT)
```

Die Anwendung folgt einer Three-Tier-Architektur:

- Präsentationsschicht: Frontend (React) und REST/WebSocket-Schnittstellen.
- Anwendungsschicht: Quarkus-Services, Regel-Engine, Konflikterkennung, Mapper.
- Persistenzschicht: PostgreSQL mit Hibernate ORM Panache.

## Wichtige Designentscheidungen

- Quarkus als Backend-Framework:
	Schneller Start, gute Produktivität im Dev-Modus und moderne Jakarta-REST-Integration.
- Hibernate ORM Panache für Datenzugriff:
	Weniger Boilerplate bei Repositories und Entities, klare Trennung zwischen Domäne und API.
- WebSocket-Erweiterung für Echtzeit:
	Gerätezustände können ohne Polling an die Oberfläche übertragen werden.
- Docker Compose für Gesamtsystemstart:
	Reproduzierbare lokale Umgebung mit Frontend, Backend und PostgreSQL.
- Rollenmodell Owner/Member:
	Klare Trennung von Verwaltungsrechten und normaler Bedienung.

## Eingesetzte Design Patterns

Die Code-Basis verwendet etablierte Design Patterns, um Wartbarkeit, Erweiterbarkeit
und Testbarkeit zu verbessern.

### Strategy + Factory — IoT-Protokoll-Abstraktion

Die Anbindung an Hardware-Protokolle erfolgt über eine Strategy-Schnittstelle
mit einer zugehörigen Factory.

- **Strategy:** `IoTProtocol` (Interface) definiert die Operationen
  `connect`, `disconnect`, `sendCommand`, `subscribe`, `unsubscribe`.
- **Konkrete Strategien:** `MockProtocolAdapter` (für Tests und Entwicklung) und
  `MqttProtocolAdapter` (für reale MQTT-Broker-Anbindung).
- **Factory:** `IoTProtocolFactory` erzeugt die jeweils konfigurierte Implementierung
  basierend auf einer Property (`iot.protocol`).

**Nutzen:** Austausch des Protokolls per Konfiguration ohne Code-Änderung in der
Geschäftslogik. Tests laufen gegen den Mock-Adapter, Produktion gegen MQTT.

### Repository — Datenzugriff

Der Datenzugriff folgt dem Repository-Pattern (umgesetzt über Hibernate ORM Panache).

- Jede Entity hat ein zugehöriges Repository (z. B. `DeviceRepository`,
  `RuleRepository`, `UserRepository`).
- Repositories kapseln Abfragen wie `findById`, `findActiveByTriggerDevice` und
  `findByEmail`.
- Services arbeiten ausschließlich gegen Repositories, niemals direkt gegen den
  `EntityManager`.

**Nutzen:** Klare Trennung von Persistenz und Geschäftslogik, einfaches Mocking
in Unit-Tests (siehe `RuleEngineServiceTest`).

### DTO + Mapper — Trennung von Entity und Wire-Format

REST-Endpunkte arbeiten nie direkt mit JPA-Entities, sondern mit Request- und
Response-DTOs. Die Konvertierung übernehmen dedizierte Mapper-Klassen.

- **Pakete:** `dto.request`, `dto.response`, `mapper`.
- **Beispiele:** `DeviceMapper.toResponse(Device)`,
  `ActivityLogMapper.toResponse(ActivityLog)`.

**Nutzen:** Verhindert ungewollte Datenleckage (z. B. Passwort-Hash in
User-Responses), entkoppelt API-Schema von Datenbankschema und ermöglicht
unabhängige Weiterentwicklung beider Schichten.

### Observer — Echtzeit-Broadcasts via WebSocket

Geräte-Zustandsänderungen werden in Echtzeit an alle verbundenen UI-Clients
gepusht.

- **Subject:** `DeviceEventBroadcaster` verwaltet aktive WebSocket-Sessions.
- **Observer:** Frontend-Clients verbinden sich über `DeviceStateSocket`.
- **Notifikation:** Services rufen `broadcaster.broadcastDeviceUpdate(device)`
  nach jeder Zustandsänderung auf — z. B. aus der `RuleEngineService` heraus.

**Nutzen:** Entkopplung der Geschäftslogik von der UI-Aktualisierung, kein
Polling notwendig, beliebig viele Clients gleichzeitig.

### Dependency Injection (CDI) — durchgehend angewendet

Quarkus' CDI-Container injiziert Repositories, Services, Broadcaster und
Konfigurationswerte über `@Inject` und `@ConfigProperty`. Dadurch werden
Komponenten lose gekoppelt und unabhängig testbar.

### Refactoring-Beispiele aus Sprint 2

Auf statische Code-Analyse (PMD) folgten gezielte Refactorings:

- **Extract Method** in `RuleResource.createRule` / `updateRule`:
  Verschachtelte Null-Checks für Action- und Trigger-Device wurden in
  `resolveDevices(...)` ausgelagert (Reduzierung der zyklomatischen Komplexität).
- **Extract Method** in `SceneResource.createScene` / `updateScene`:
  Inline-Schleife mit Device-Lookup wurde in `applyDeviceStates(...)` extrahiert.

### Refactoring-Backlog für Sprint 3

Bewusst dokumentierte, noch nicht umgesetzte Verbesserungen:

- Service-Schnitt zwischen `RuleEngineService` und `ConflictDetectionService`
  prüfen; mögliche Extraktion einer gemeinsamen `TriggerEvaluation`-Komponente.
- Einheitliches Error-Response-DTO statt `Map.of("error", ...)` in den
  Resource-Klassen.
- Validierung von Request-DTOs konsequent über `@Valid` und Bean-Validation
  statt manueller Null-Checks.


## Erweiterungspunkte

- Zusätzliche Automatisierungsarten in der Regel-Engine ergänzen.
- Neue REST-Endpunkte und DTOs für zusätzliche Fachfunktionen hinzufügen.
- Zusätzliche Unit- und Integrationstests ergänzen.
- Neue IoT-Protokolle als zusätzliche `IoTProtocol`-Implementierungen
  (z. B. Zigbee, Z-Wave, KNX) ergänzen — ohne Eingriff in die Geschäftslogik.

## Build und Qualitaetssicherung

| Tool        | Zweck                                  |
|-------------|----------------------------------------|
| **Maven**   | Build-Management und Dependency-Management |
| **JUnit 5** | Unit- und Integrationstests            |
| **JaCoCo**  | Code-Coverage-Analyse                  |
| **PMD**     | Statische Code-Analyse                 |
| **ESLint**  | Linting fuer das Frontend              |

**Tests ausfuehren:**
```bash
cd backend
./mvnw verify
```

**Frontend Lint:**
```bash
cd frontend
npm run lint
```


## Testfallbeschreibung und Testabdeckung

- Wichtige Testfälle:
	Validierung von CRUD-Abläufen für zentrale Domänenobjekte, Login- und Rollenverhalten,
	Regeln und Zeitpläne, Konflikterkennung sowie Mapper- und Service-Logik.
- Aktuelle Test Coverage: [![Coverage](https://raw.githubusercontent.com/jku-win-se/teaching-2026.ss.prse.braeuer.team1/main/.github/badges/jacoco.svg)](https://github.com/jku-win-se/teaching-2026.ss.prse.braeuer.team1/actions/workflows/Continuous%20Integration.yaml)

## Performance-Verifikation

Die Antwortzeit aller benutzerorientierten REST-Endpunkte wird mit einem
reproduzierbaren Skript gemessen — siehe [`performance/`](performance/).
Aktueller Stand bei 10 Geräten: alle 10 getesteten Endpunkte liegen mit
ihrer p95-Latenz deutlich unter dem in NFR-01 geforderten 2-Sekunden-Limit.
Der langsamste Endpunkt ist der Login mit ≈ 84 ms p95 (BCrypt-Hash-Prüfung),
alle weiteren Endpunkte ≤ 10 ms p95.


## Sicherheit

Die Sicherheitsmaßnahmen sind über mehrere Ebenen verteilt — von der Code-Analyse
über Authentifizierung bis hin zur Repository-Hygiene.

### Authentifizierung und Autorisierung

- **Passwort-Hashing mit BCrypt** (NFR-02):
	Passwörter werden niemals im Klartext gespeichert. Das Hashing erfolgt über die
	BCrypt-Implementierung beim Login- und Register-Flow im `UserResource`.
- **Rollenbasierte Zugriffskontrolle** (FR-13):
	Zwei Rollen mit klarer Trennung der Rechte:
	- **Owner**: Vollzugriff auf Geräte-, Regel-, Szenen- und Mitgliederverwaltung.
	- **Member**: Eingeschränkter Zugriff — Bedienung erlaubt, keine
	  Verwaltungsoperationen.
	Geschützte Endpunkte prüfen die Rolle des aufrufenden Benutzers serverseitig.

### Statische Security-Analyse

- **GitHub CodeQL Security Scanning** (Issue #65):
	Eigene Pipeline (`.github/workflows/codeql.yml`) scannt sowohl Java-Backend als
	auch JavaScript/TypeScript-Frontend bei jedem Push und Pull Request.
	Findings werden im Security-Tab des Repos sichtbar gemacht.

- **Praxisbeispiel** (Issue #80):
	Beim ersten CodeQL-Lauf in Sprint 2 wurde eine HIGH-Severity XSS-Schwachstelle
	(CWE-79) in einer auto-generierten JavaDoc-Datei gefunden. Reaktion:
	Build-Artefakt aus dem Repository entfernt, `.gitignore` ergänzt, JavaDoc-
	Generierung erfolgt jetzt on-demand. Damit wurde die Ursache und nicht nur das
	Symptom beseitigt.

- **PMD Statische Analyse** (NFR-04):
	Ergänzend zur Security-Sicht prüft PMD den Code auf typische Code-Smells und
	Fehlerquellen. Der Build bricht ab, sobald eine Violation auftritt
	(`mvn pmd:check pmd:cpd-check`).

### Repository-Hygiene

- **Keine Secrets im Repository:**
	`.env`, `.env.local` sowie alle entsprechenden Varianten sind in `.gitignore`
	enthalten. Konfiguration sensibler Daten erfolgt über Umgebungsvariablen oder
	lokale Konfigurationsdateien, die nicht eingecheckt werden.
- **Keine generierten Build-Artefakte:**
	`target/`, `node_modules/`, `dist/` und auch `docs/javadoc/` sind ignoriert,
	damit kein unbeabsichtigt verwundbarer Code mitcommitet wird (siehe XSS-Beispiel oben).

### CORS-Konfiguration

Eine explizite CORS-Konfiguration im Backend erlaubt während der Entwicklung den
Zugriff vom Frontend-Dev-Server. Für den Produktionsbetrieb sollte diese
Konfiguration auf die produktive Frontend-Domain eingeschränkt werden.

### Empfehlungen für den Produktivbetrieb

Folgende Punkte sind im Rahmen der LVA out of scope, aber für einen
tatsächlichen Produktivbetrieb empfehlenswert:

- HTTPS-Terminierung am Reverse-Proxy (nginx)
- Rate-Limiting auf Login- und API-Endpunkten
- Token-basierte Authentifizierung mit kurzer Lebensdauer (JWT + Refresh-Token)
- Audit-Logging für Verwaltungsoperationen (Owner-Aktionen)
- Regelmäßige Dependency-Scans (`npm audit`, OWASP Dependency-Check für Maven)





