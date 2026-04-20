# Systemdokumentation

## Überblick

Der SmartHome Orchestrator ist eine Webanwendung zur zentralen Steuerung und Automatisierung eines Smart Homes.
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





