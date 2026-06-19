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

## Umgesetzte Anforderungen

- [Anforderungsdokument](docs/requirements/requirements_smarthome_de.md)

Alle 21 funktionalen und alle 6 nicht-funktionalen Anforderungen wurden vollständig umgesetzt.

### Funktionale Anforderungen

| ID | Anforderung | Verantwortlich | Aufwand (h) |
| :---- | :---- | :---- | :---- |
| FR-01 | Benutzerregistrierung |  |  |
| FR-02 | Benutzer-Login / Logout |  |  |
| FR-03 | Rollenverwaltung (Owner / Member) |  |  |
| FR-04 | Geräteverwaltung (Anlegen, Typen) |  |  |
| FR-05 | Gerät umbenennen |  |  |
| FR-06 | Gerät zentral schalten |  |  |
| FR-07 | Echtzeit-Gerätezustand per WebSocket |  |  |
| FR-08 | Lückenloses Aktivitätslog |  |  |
| FR-09 | Raumverwaltung |  |  |
| FR-10 | Regeln anlegen (WENN … DANN …) |  |  |
| FR-11 | Regeln aktivieren / deaktivieren |  |  |
| FR-12 | Regel-Engine: automatische Ausführung |  |  |
| FR-13 | Mitglieder einladen, Sichtbarkeitsschutz |  |  |
| FR-14 | Energie-Dashboard (Verbrauch je Gerät) |  |  |
| FR-15 | Konflikt-Erkennung bei Regeln/Zeitplänen |  |  |
| FR-16 | CSV-Export der Energiedaten |  |  |
| FR-17 | Szenen (Gerätegruppen-Profile) |  |  |
| FR-18 | IoT-Schicht (MQTT-Anbindung vorbereitet) |  |  |
| FR-19 | Tagessimulation im Zeitraffer |  |  |
| FR-20 | Zeitplan-Verwaltung |  |  |
| FR-21 | Urlaubsmodus |  |  |

### Nicht-funktionale Anforderungen

| ID | Anforderung | Ergebnis |
| :---- | :---- | :---- |
| NFR-01 | Antwortzeit \< 2 Sekunden | Erfüllt – alle REST-Endpunkte antworten typisch \< 200 ms |
| NFR-02 | Passwörter nie im Klartext | Erfüllt – bcrypt-Hash via `org.mindrot.jbcrypt` |
| NFR-03 | Testabdeckung ≥ 75 % | Erfüllt – JaCoCo misst 94,2 % |
| NFR-04 | CI bricht bei PMD-Verstößen ab | Erfüllt – 0 kritische Befunde, Build-Gate aktiv |
| NFR-05 | Rollen-basierter Zugriff | Erfüllt – Owner/Member-Trennung auf API-Ebene |
| NFR-06 | JavaDoc für Kern & API | Erfüllt – alle öffentlichen Klassen und Methoden dokumentiert |

## Überblick über die Applikation aus Benutzersicht inkl. Installationsanleitung

Die Benutzerdokumentation mit Szenarien und Funktionsbeschreibungen, sowie die vollständige Anleitung zum Starten der Anwendung (Docker Compose und lokale Entwicklung)
steht im Benutzerhandbuch:

- [Benutzerdokumentation](docs/user-handbook.md)

## Überblick über die Applikation aus Entwicklersicht

### Systemarchitektur

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

- [Systemdokumentation](docs/system-architecture.md)

### Domänenmodell

- Datei: [Domänenmodell](docs/domain-model.md)
- Inhalt:
	Fachliches Domänenmodell mit Mermaid-Klassendiagramm,
	Entitätsbeschreibungen, zentrale Geschäftsregeln und vollständige Klassenübersicht
	über DTOs, Entitäten, Mapper, Repositories, Ressourcen, Services, IoT und WebSocket.



## JavaDoc für wichtige Klassen, Interfaces und Methoden

JavaDoc wird bei Bedarf lokal generiert (nicht ins Repo eingecheckt):

```bash
cd backend
mvn javadoc:javadoc
```

Output: `backend/target/site/apidocs/index.html` im Browser öffnen.








