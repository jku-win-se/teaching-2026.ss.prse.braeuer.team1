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

## Erweiterungspunkte

- Zusätzliche Automatisierungsarten in der Regel-Engine ergänzen.
- Neue REST-Endpunkte und DTOs für zusätzliche Fachfunktionen hinzufügen.
- Zusätzliche Unit- und Integrationstests ergänzen.

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
- Aktuelle Test Coverage: [![Coverage](.github/badges/jacoco.svg)](https://github.com/jku-win-se/teaching-2026.ss.prse.braeuer.team1/actions/workflows/Continuous%20Integration.yaml)


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





