# Screenshots der Benutzeroberfläche

Diese Bilder werden im [Benutzerhandbuch](../user-handbook.md) eingebunden und zeigen jede Hauptansicht der Anwendung.

## Aufnahme reproduzieren

Die Screenshots werden mit einem Playwright-Skript erzeugt, das die laufende Anwendung automatisiert durchklickt und jeden Bildschirm aufnimmt.

### Voraussetzungen

- Die Anwendung muss laufen — am einfachsten via Docker Compose im Repo-Root:
  ```bash
  docker compose up -d
  ```
- Frontend muss unter `http://localhost:3000` erreichbar sein.
- Backend muss unter `http://localhost:8080` erreichbar sein.
- Die `StartupDataLoader`-Klasse legt einen Seed-Nutzer an:
  - E-Mail: `alice@example.com`
  - Passwort: `password123`

### Skript ausführen

```bash
# einmalige Vorbereitung
mkdir -p /tmp/userdoc-screenshots && cd /tmp/userdoc-screenshots
npm init -y
npm install --save-dev playwright
npx playwright install chromium

# Skript aus dem Repo holen
cp <repo>/docs/screenshots/take-screenshots.js .

# ausführen, Output in docs/screenshots/ schreiben
node take-screenshots.js <repo>/docs/screenshots
```

### Was passiert

Das Skript meldet sich mit dem Seed-Owner an und ruft nacheinander alle Routen auf. Pro Route wird ein PNG mit 1440×900 Pixeln und Retina-Skalierung (2×) erzeugt.

| Datei | Ansicht |
|-------|---------|
| `01-login.png`         | Anmeldung |
| `02-register.png`      | Registrierung |
| `03-dashboard.png`     | Dashboard |
| `04-rooms.png`         | Räume |
| `05-devices.png`       | Geräte |
| `06-scenes.png`        | Szenen |
| `07-rules.png`         | Regeln |
| `08-schedules.png`     | Zeitpläne |
| `09-energy.png`        | Energie |
| `10-activity.png`      | Aktivitäten |
| `11-notifications.png` | Benachrichtigungen |
| `12-vacation.png`      | Urlaubsmodus |
| `13-members.png`       | Mitglieder |
| `14-simulation.png`    | Simulation |
