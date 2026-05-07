[![Build](https://img.shields.io/github/actions/workflow/status/jku-win-se/teaching-2026.ss.prse.braeuer.team1/Continuous%20Integration.yaml?branch=main&label=build)](https://github.com/jku-win-se/teaching-2026.ss.prse.braeuer.team1/actions/workflows/Continuous%20Integration.yaml)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/jku-win-se/teaching-2026.ss.prse.braeuer.team1/actions/workflows/Continuous%20Integration.yaml)
[![PMD](https://img.shields.io/github/actions/workflow/status/jku-win-se/teaching-2026.ss.prse.braeuer.team1/Continuous%20Integration.yaml?branch=main&label=pmd)](https://github.com/jku-win-se/teaching-2026.ss.prse.braeuer.team1/actions/workflows/Continuous%20Integration.yaml)
[![CodeQL](https://img.shields.io/github/actions/workflow/status/jku-win-se/teaching-2026.ss.prse.braeuer.team1/codeql.yml?branch=main&label=codeql)](https://github.com/jku-win-se/teaching-2026.ss.prse.braeuer.team1/actions/workflows/codeql.yml)

# SmartHomie

Eine Heimautomatisierungsanwendung zur zentralen Verwaltung von Smart-Home-Geräten, Automatisierungsregeln, Zeitplänen und Energieverbrauch -- entwickelt im Rahmen des Software Engineering Praktikums (SS 2026) an der JKU Linz.



## Team

| Mitglied           | Schwerpunkt                                   |
|--------------------|-----------------------------------------------|
| **Armin Hamzic**   | Backend-Entwicklung, Quarkus-Setup, Business-Logik, CI/CD |
| **Benjamin Besic** | Frontend-Entwicklung, React/UI, Docker-Setup  |
| **Felix Rieser**   | WebSocket, IoT-Integration, Simulation, Tests, Code-Qualität |



## Systemarchitektur

Das Projekt folgt einer klassischen **Three-Tier-Architektur**, vollständig containerisiert mit Docker Compose:

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│   Frontend   │──────>│     Backend      │──────>│  PostgreSQL  │
│  React/Vite  │ :3000 │  Quarkus REST    │ :8080 │   Datenbank  │
│  (nginx)     │       │  + WebSocket     │       │              │
└──────────────┘       └──────────────────┘       └──────────────┘
```

| Schicht      | Technologie                          | Beschreibung                                                                 |
|--------------|--------------------------------------|------------------------------------------------------------------------------|
| **Frontend** | React 19, TypeScript, Vite, Tailwind CSS | Single-Page-Application mit shadcn/ui-Komponenten, Recharts für Dashboards |
| **Backend**  | Quarkus 3.32, Java 21, Hibernate ORM Panache | RESTful API mit WebSocket-Support für Echtzeit-Updates                   |
| **Datenbank**| PostgreSQL 17                        | Persistenz aller Domänedaten mit automatischem Schema-Management             |
| **Infrastruktur**    | Docker Compose, nginx                | Multi-Container-Orchestrierung mit Health-Checks und Reverse-Proxy           |



## Startanleitung

Die Benutzerdokumentation mit Szenarien und Funktionsbeschreibungen, sowie die vollständige Anleitung zum Starten der Anwendung (Docker Compose und lokale Entwicklung)
steht im Benutzerhandbuch:

- [Benutzerdokumentation](docs/user-handbook.md)


## Überblick über die Applikation aus Entwicklersicht

- [Systemdokumentation](docs/system-architecture.md)


## Domänenmodell

- Datei: [Domänenmodell](docs/domain-model.md)
- Inhalt:
	Fachliches Domänenmodell mit Mermaid-Klassendiagramm,
	Entitätsbeschreibungen, zentrale Geschäftsregeln und vollständige Klassenübersicht
	über DTOs, Entitäten, Mapper, Repositories, Ressourcen, Services, IoT und WebSocket.


## Generierte API-Dokumentation

JavaDoc wird bei Bedarf lokal generiert (nicht ins Repo eingecheckt):

```bash
cd backend
mvn javadoc:javadoc
```

Output: `backend/target/site/apidocs/index.html` im Browser öffnen.


## Dokumentation der Anforderungen

- [Anforderungsdokument](docs/requirements/requirements_smarthome_de.md)






