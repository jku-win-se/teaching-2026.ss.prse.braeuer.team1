[![Build](https://img.shields.io/github/actions/workflow/status/jku-win-se/teaching-2026.ss.prse.braeuer.team1/Continuous%20Integration.yaml?branch=main&label=build)](https://github.com/jku-win-se/teaching-2026.ss.prse.braeuer.team1/actions/workflows/Continuous%20Integration.yaml)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/jku-win-se/teaching-2026.ss.prse.braeuer.team1/actions/workflows/Continuous%20Integration.yaml)
[![PMD](https://img.shields.io/github/actions/workflow/status/jku-win-se/teaching-2026.ss.prse.braeuer.team1/Continuous%20Integration.yaml?branch=main&label=pmd)](https://github.com/jku-win-se/teaching-2026.ss.prse.braeuer.team1/actions/workflows/Continuous%20Integration.yaml)

# SmartHome Orchestrator

Eine Heimautomatisierungsanwendung zur zentralen Verwaltung von Smart-Home-Geraeten, Automatisierungsregeln, Zeitplaenen und Energieverbrauch -- entwickelt im Rahmen des Software Engineering Praktikums (SS 2026) an der JKU Linz.

---

## Systemarchitektur

Das Projekt folgt einer klassischen **Three-Tier-Architektur**, vollstaendig containerisiert mit Docker Compose:

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│   Frontend   │──────>│     Backend      │──────>│  PostgreSQL   │
│  React/Vite  │ :3000 │  Quarkus REST    │ :8080 │   Datenbank   │
│  (nginx)     │       │  + WebSocket     │       │               │
└──────────────┘       └──────────────────┘       └──────────────┘
```

| Schicht      | Technologie                          | Beschreibung                                                                 |
|--------------|--------------------------------------|------------------------------------------------------------------------------|
| **Frontend** | React 19, TypeScript, Vite, Tailwind CSS | Single-Page-Application mit shadcn/ui-Komponenten, Recharts fuer Dashboards |
| **Backend**  | Quarkus 3.32, Java 21, Hibernate ORM Panache | RESTful API mit WebSocket-Support fuer Echtzeit-Updates                   |
| **Datenbank**| PostgreSQL 17                        | Persistenz aller Domaendaten mit automatischem Schema-Management             |
| **Infra**    | Docker Compose, nginx                | Multi-Container-Orchestrierung mit Health-Checks und Reverse-Proxy           |

---

## Voraussetzungen

Bevor das Projekt gestartet werden kann, muessen folgende Tools installiert sein:

- [ ] **Docker** (inkl. Docker Compose) -- [docker.com](https://www.docker.com/)
- [ ] **Git** -- zum Klonen des Repositories

Fuer die lokale Entwicklung zusaetzlich:

- [ ] **JDK 21** -- z.B. Eclipse Temurin oder Oracle JDK
- [ ] **Maven 3.9+** -- oder den enthaltenen Maven Wrapper (`./mvnw`) verwenden
- [ ] **Node.js 22+** und **npm** -- fuer das Frontend

---

## Quick Start

### Gesamtes Projekt mit Docker Compose starten

```bash
# Repository klonen
git clone <repository-url>
cd teaching-2026.ss.prse.braeuer.team1

# Alle Services bauen und starten
docker compose up --build
```

Nach dem Start sind folgende Services erreichbar:

| Service    | URL                        | Beschreibung             |
|------------|----------------------------|--------------------------|
| Frontend   | http://localhost:3000       | Web-Oberflaeche          |
| Backend    | http://localhost:8080       | REST API                 |
| PostgreSQL | `localhost:5432`           | Datenbank (User: `prse`) |

> **Hinweis:** Beim Start wird automatisch ein `StartupDataLoader` ausgefuehrt, der initiale Testdaten (Benutzer, Raeume, Geraete) in die Datenbank laedt. Die Datenbank wird bei jedem Neustart zurueckgesetzt (`drop-and-create`).
>
> **Wichtig:** Docker Compose sollte mindestens einmal zuerst gestartet werden, damit Backend und Datenbank initialisieren und die Tabellen angelegt werden.
>
> **Hinweis fuer lokale Entwicklung:** Wenn du mit Docker Compose arbeitest, werden Frontend und Backend bereits gebaut und gestartet. Du musst die Projekte dann nicht zusaetzlich lokal starten.

### Einzelne Services fuer die Entwicklung

Diese Option ist fuer direkte lokale Entwicklung in den Projektordnern gedacht.

1. Datenbank starten (z. B. `docker compose up -d postgres`).
2. Backend lokal starten.
3. Frontend lokal starten.

**Backend (Quarkus Dev Mode):**
```bash
cd backend
./mvnw quarkus:dev
```

**Frontend (Vite Dev Server):**
```bash
cd frontend
npm install
npm run dev
```

### Build und Packaging

**Backend paketieren (JVM):**
```bash
cd backend
./mvnw package
```

Ausfuehrbar als JVM-Anwendung:
```bash
cd backend
java -jar target/quarkus-app/quarkus-run.jar
```

**Backend als Ueber-JAR bauen:**
```bash
cd backend
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

**Backend Native Build (optional):**
```bash
cd backend
./mvnw package -Dnative
```

Container-basierter Native Build (ohne lokale GraalVM):
```bash
cd backend
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

**Frontend Production Build:**
```bash
cd frontend
npm install
npm run build
```

Lokale Vorschau des Frontend-Builds:
```bash
cd frontend
npm run preview
```

> **Hinweis:** Die Quarkus Dev UI ist im Dev Mode unter http://localhost:8080/q/dev/ erreichbar.

---

## Umgesetzte Anforderungen

### Funktionale Anforderungen

| ID    | Anforderung                                | Verantwortlich     | Aufwand |
|-------|--------------------------------------------|--------------------|---------|
| FR-01 | Benutzerregistrierung (E-Mail + Passwort)  | Armin Hamzic       | 2h      |
| FR-02 | Login / Logout                             | Armin Hamzic       | (in FR-01) |
| FR-03 | Raumverwaltung (CRUD)                      | Armin Hamzic (BE), Benjamin Besic (FE) | 4h |
| FR-04 | Geraete zu Raeumen hinzufuegen             | Armin Hamzic (BE), Benjamin Besic (FE) | 6h |
| FR-05 | Geraete entfernen / umbenennen             | Armin Hamzic (BE), Benjamin Besic (FE) | (in FR-04) |
| FR-06 | Manuelle Geraetesteuerung                  | Armin Hamzic (BE), Benjamin Besic (FE) | (in FR-04) |
| FR-07 | Echtzeit-Geraetezustand (WebSocket)        | Felix Rieser       | 6.5h   |
| FR-08 | Aktivitaets-Logging                        | Armin Hamzic (BE), Benjamin Besic (FE) | 3.5h |
| FR-09 | Zeitbasierte Zeitplaene                    | Armin Hamzic (BE), Benjamin Besic (FE) | 4h |
| FR-10 | Regel-Engine (IF-THEN)                     | Armin Hamzic (BE), Benjamin Besic (FE) | 5h |
| FR-11 | Drei Ausloesertypen fuer Regeln            | Armin Hamzic       | (in FR-10) |
| FR-12 | In-App-Benachrichtigungen                  | Armin Hamzic (BE), Benjamin Besic (FE) | 3.5h |
| FR-13 | Rollenbasierte Zugriffskontrolle (Owner/Member) | Armin Hamzic (BE), Felix Rieser (FE) | 5.3h |
| FR-14 | Energieverbrauchs-Dashboard                | Armin Hamzic (BE), Benjamin Besic (FE) | 6h |
| FR-15 | Planungskonflikterkennung                  | Felix Rieser       | 3h     |
| FR-16 | CSV-Export (Aktivitaetslog, Energiedaten)   | Armin Hamzic       | (in FR-14) |
| FR-17 | Szenen-Verwaltung                          | Armin Hamzic (BE), Benjamin Besic (FE) | 6h |
| FR-18 | IoT-Integrationsschicht (MQTT)             | Felix Rieser       | 4h     |
| FR-19 | Zeitraffer-Simulation                      | Felix Rieser       | 3h     |
| FR-20 | Mitglieder einladen / widerrufen           | Armin Hamzic (BE), Felix Rieser (FE) | 5.3h |
| FR-21 | Urlaubsmodus                               | Armin Hamzic (BE), Benjamin Besic (FE) | 4h |

### Nicht-funktionale Anforderungen

| ID     | Kategorie     | Status |
|--------|---------------|--------|
| NFR-01 | Performance   | Erfuellt -- Antwortzeiten < 2s bei normaler Last |
| NFR-02 | Sicherheit    | Erfuellt -- Passwoerter werden mit BCrypt gehasht |
| NFR-03 | Testabdeckung | Erfuellt -- JaCoCo-Integration mit >= 75% Coverage |
| NFR-04 | Codequalitaet | Erfuellt -- PMD-Pruefung im CI-Build integriert |
| NFR-05 | Zuverlaessigkeit | Erfuellt -- Fehlertolerante Geraetebehandlung |
| NFR-06 | Dokumentation | Erfuellt -- Javadoc fuer oeffentliche Klassen und Methoden |

---

## Ueberblick ueber die Applikation aus Benutzersicht

Die vollstaendige Benutzerdokumentation mit Szenarien und Funktionsbeschreibungen ist hier zu finden:

- [Benutzerdokumentation (User Handbook)](docs/user-handbook.md)

---

## Ueberblick ueber die Applikation aus Entwicklersicht

- [Systemarchitektur-Dokumentation](docs/system-architecture.md)

### Architektur

Das Backend folgt einer geschichteten Architektur:

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

### Build und Qualitaetssicherung

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

---

## Weiterfuehrende Dokumentation

- [Domain Model](docs/domain-model.md) -- Zentrales Domaenenmodell mit Mermaid-Klassendiagramm und Entity-Beschreibungen
- [Anforderungsdokument](docs/requirements_smarthome_de.md) -- Vollstaendiges Anforderungsdokument mit allen FR/NFR
- [Systemarchitektur](docs/system-architecture.md) -- Architekturueberblick und Designentscheidungen
- [Benutzerdokumentation](docs/user-handbook.md) -- Installationsanleitung und Funktionsbeschreibungen
- [Tool-Demo](docs/tool-demo.md) -- Uebersicht ueber eingesetzte UI- und Entwicklungstools

### JavaDoc

- [JavaDoc (HTML)](docs/javadoc/index.html) -- Generierte API-Dokumentation fuer alle oeffentlichen Klassen und Methoden

---

## Team

| Mitglied           | Schwerpunkt                                   |
|--------------------|-----------------------------------------------|
| **Armin Hamzic**   | Backend-Entwicklung, Quarkus-Setup, Business-Logik, CI/CD |
| **Benjamin Besic** | Frontend-Entwicklung, React/UI, Docker-Setup  |
| **Felix Rieser**   | WebSocket, IoT-Integration, Simulation, Tests, Code-Qualitaet |
