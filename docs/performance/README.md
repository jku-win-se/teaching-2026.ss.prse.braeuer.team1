# Performance verification — NFR-01

NFR-01 fordert eine Antwortzeit unter 2 Sekunden bei normaler Last (bis zu 10 Geräte).

## Methode

Das Skript [`run-perf-test.sh`](./run-perf-test.sh) misst die Antwortzeit aller
benutzerorientierten REST-Endpunkte mit `curl`.

1. **Anmeldung** als Seed-Owner (`alice@example.com`).
2. **Setup**: Sicherstellen, dass mindestens 10 Geräte in der Datenbank
   existieren. Fehlende Geräte werden temporär angelegt.
3. **Messung**: Pro Endpunkt **30 Iterationen**, kompletter HTTP-Roundtrip
   (Verbindung + Request + Server-Verarbeitung + Response).
4. **Statistik**: min / avg / p95 / max in Millisekunden.
5. **Bewertung**: p95 ≤ 2000 ms → `PASS`, sonst `FAIL`.
6. **Cleanup**: Temporär angelegte Test-Geräte werden am Ende entfernt
   (auch bei Skript-Abbruch via `trap`).

## Endpunkte im Test

| Methode | URL | Bedeutung |
|---------|-----|-----------|
| `POST` | `/api/users/login` | Anmeldung mit BCrypt-Hash-Prüfung — typisch teuerste Operation |
| `GET`  | `/api/rooms?userId=1` | Raumliste laden (Dashboard, Räume-Seite) |
| `GET`  | `/api/rooms/{id}/devices` | Geräteliste eines Raums (mit 10 Geräten) |
| `GET`  | `/api/rules?userId=1` | Alle Regeln laden |
| `GET`  | `/api/schedules?userId=1` | Alle Zeitpläne |
| `GET`  | `/api/scenes?userId=1` | Alle Szenen mit Device-States |
| `GET`  | `/api/energy/dashboard?userId=1` | Energie-Dashboard inkl. Aggregation |
| `GET`  | `/api/activity-logs?userId=1` | Aktivitätsprotokoll |
| `GET`  | `/api/notifications?userId=1` | In-App-Benachrichtigungen |
| `PUT`  | `/api/devices/{id}/state` | Geräte-Zustand ändern (löst Rule-Engine + WebSocket-Broadcast aus) |

## Reproduzieren

```bash
docker compose up -d
./docs/performance/run-perf-test.sh
```

Das Skript schreibt einen Markdown-Report nach `docs/performance/results.md`.
Konfigurierbar per Umgebungsvariable:

```bash
ITERATIONS=100 \
TARGET_DEVICE_COUNT=20 \
LIMIT_SECONDS=1.0 \
./docs/performance/run-perf-test.sh
```

## Aktuelles Ergebnis

Siehe [`results.md`](./results.md). Stand der letzten Messung:

- **10 Endpunkte getestet**
- **30 Iterationen pro Endpunkt**
- **10 Geräte in der Datenbank**
- **0 Endpunkte überschreiten das 2-Sekunden-Limit**
- Langsamster Endpunkt: `POST /api/users/login` mit p95 ≈ 84 ms
  (≈ 24× unter dem Limit; Hauptlast durch BCrypt-Hash-Verifikation —
  bewusst nicht weiter optimiert, weil das die Security-Eigenschaft schwächen
  würde, siehe NFR-02).
- Alle anderen Endpunkte: p95 ≤ 10 ms (≈ 200× unter dem Limit).

## Einschränkungen

- Lokale Messung auf dem Entwicklungs-Host, nicht in einer produktionsnahen
  Cloud-Umgebung.
- Sequenzielle Anfragen (ein Client). Lasttests mit hoher Parallelität sind
  nicht Teil dieses Tests — NFR-01 spricht explizit von "normaler Last".
- Die DB ist mit Seed-Daten + bis zu 10 Geräten gefüllt, nicht mit
  jahrelang gewachsenen Activity-Logs.

## Optimierungs-Hooks (für eine zukünftige Skalierung)

- Datenbank-Indizes auf `ActivityLog(timestamp DESC)` und
  `Schedule(nextRun)` sind bereits vorhanden (siehe Migration).
- Hibernate-Query-Caching nicht aktiv — bei größeren Datenmengen
  ein erster Optimierungs-Hebel.
- Energie-Dashboard berechnet jedes Mal frisch — denkbar wäre eine
  zeitfenster-basierte Cache-Schicht.
