# Benutzerdokumentation

## Zielgruppe

Die Anwendung richtet sich an Personen, die ein Smart Home zentral verwalten und automatisieren möchten.

Primäre Zielgruppen sind:

- Haushaltsverantwortliche (Owner), die Räume, Geräte, Regeln, Zeitpläne, Szenen und Mitglieder verwalten.
- Haushaltsmitglieder (Member), die freigegebene Geräte bedienen und den aktuellen Zustand des Zuhauses einsehen.
- Technikaffine Nutzerinnen und Nutzer, die Automatisierungen wie IF-THEN-Regeln, Zeitpläne und Urlaubsmodus aktiv nutzen möchten.

Die Anwendung ist besonders geeignet für kleine bis mittlere Haushalte, in denen mehrere Personen gemeinsam auf ein Smart-Home-System zugreifen und dabei eine klare Rollenverteilung benötigen.

## Voraussetzungen

Bevor das Projekt gestartet werden kann, muessen folgende Tools installiert sein:

- [ ] **Docker** (inkl. Docker Compose) -- [docker.com](https://www.docker.com/)
- [ ] **Git** -- zum Klonen des Repositories

Fuer die lokale Entwicklung zusaetzlich:

- [ ] **JDK 21** -- z.B. Eclipse Temurin oder Oracle JDK
- [ ] **Maven 3.9+** -- oder den enthaltenen Maven Wrapper (`./mvnw`) verwenden
- [ ] **Node.js 22+** und **npm** -- fuer das Frontend

## Installation und Start

Es gibt zwei gleichwertige Optionen zum Starten der Anwendung.

### Option A: Nur mit Docker Compose (empfohlen für schnellen Start)

1. Projekt im Repository-Root öffnen.
2. Docker Desktop starten.
3. Mit docker compose up --build alle Services starten.

Hinweis: In Docker Compose werden Frontend und Backend bereits gebaut und gestartet. In diesem Modus müssen die Projekte nicht zusätzlich lokal gestartet werden.

Erreichbare URLs:

- Frontend: http://localhost:3000
- Backend: http://localhost:8080

### Option B: Direkt in den Projekten starten (lokale Entwicklung)

1. Voraussetzungen installieren: Java 21, Maven, Node.js inklusive npm.
2. Das Projekt im Repository-Root öffnen.
3. Datenbank starten, z. B. mit docker compose up -d postgres.
4. Backend starten: in den backend-Ordner wechseln und mvn quarkus:dev ausführen.
5. Frontend starten: in den frontend-Ordner wechseln, npm install und danach npm run dev ausführen.

## Funktionen

- Benutzerverwaltung: Registrierung, Login sowie Rollen Owner und Member.
- Raum- und Geräteverwaltung: Räume anlegen, Geräte hinzufügen, umbenennen, steuern und entfernen.
- Automatisierung: Zeitpläne, Regeln (IF-THEN), Szenen und Urlaubsmodus konfigurieren.
- Transparenz: Aktivitätsprotokolle, In-App-Benachrichtigungen und Energieverbrauchs-Dashboard.
- Echtzeit: Aktuelle Gerätezustände werden per WebSocket aktualisiert.

Die Bedienung erfolgt über die Weboberfläche. Eingaben werden über Formulare gemacht, Ergebnisse werden als Listen, Statusanzeigen und Dashboard-Werte dargestellt.

## Szenarien

### Szenario 1: Neues Gerät einrichten

1. Als Owner anmelden.
2. Raum auswählen oder neu anlegen.
3. Neues Gerät hinzufügen und Namen vergeben.
4. Gerät direkt testen, indem der Zustand geändert wird.

### Szenario 2: Automatische Abendbeleuchtung

1. Zeitplan für eine Lampe erstellen.
2. Startzeit (z. B. täglich 18:00) und Aktion definieren.
3. Zeitplan aktivieren.
4. Anwendung schaltet das Gerät zur geplanten Zeit automatisch.

### Szenario 3: Urlaubsmodus aktivieren

1. Zeitraum für den Urlaub festlegen.
2. Ersatz-Zeitplan auswählen.
3. Urlaubsmodus aktivieren.
4. Normale Zeitpläne werden während des Zeitraums übersteuert.

## Bekannte Einschränkungen

- Die Anwendung ist auf lokale Entwicklung und Lehrprojekt-Einsatz ausgelegt.
- Bei Neustart im Dev-Setup kann die Datenbank neu initialisiert werden (abhängig von der Konfiguration).
- Zugriffskontrolle basiert auf Rollen; eine vollständige produktive Authentifizierungslösung ist nicht Teil dieses Stands.

## FAQ

### Wie starte ich die Anwendung am schnellsten?

Mit docker compose up --build im Repository-Root.

### Welche Rolle sollte ich verwenden?

Owner für Verwaltung und Konfiguration, Member für normale Bedienung.

### Warum sehe ich meine erstellten Daten nach einem Neustart nicht mehr?

Im Entwicklungsmodus kann die Datenbank bei Neustart neu erstellt werden.

### Wo finde ich API-Details?

In der generierten Javadoc-Dokumentation unter docs/javadoc/index.html.

