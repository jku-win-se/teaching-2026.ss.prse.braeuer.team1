# SmartHomie — Domain Model

## Überblick

Dieses Dokument beschreibt das fachliche Domänenmodell sowie die vollständige Klassenstruktur des Backends.
Neben den zentralen Entitäten werden auch API-, Service-, Integrations- und Infrastrukturklassen paketweise erläutert.

## Paketstruktur

- `at.jku.se`: Bootstrap und Initialdaten
- `at.jku.se.dto.request`: Eingabemodelle für REST-Endpunkte
- `at.jku.se.dto.response`: Ausgabemodelle für REST-Endpunkte
- `at.jku.se.entity`: Persistente Domänenobjekte
- `at.jku.se.entity.enums`: Fachliche Enumerationen
- `at.jku.se.mapper`: Mapping zwischen Entity und DTO
- `at.jku.se.repository`: Datenzugriff über Panache
- `at.jku.se.resource`: REST-API-Endpunkte
- `at.jku.se.service`: Geschäftslogik und Regeln
- `at.jku.se.iot`: IoT-Integrationsschicht
- `at.jku.se.websocket`: Echtzeitkommunikation

```mermaid
classDiagram
    direction TB

    %% ─── Enumerations ────────────────────────────────────────────────────────

    class UserRole {
        <<enumeration>>
        OWNER
        MEMBER
    }

    class DeviceType {
        <<enumeration>>
        SWITCH
        DIMMER
        THERMOSTAT
        SENSOR
        BLIND
    }

    class TriggerType {
        <<enumeration>>
        TIME_BASED
        THRESHOLD
        EVENT
    }

    %% ─── Entities ────────────────────────────────────────────────────────────

    class User {
        +Long id
        +String email
        +String passwordHash
        +UserRole role
        +LocalDateTime createdAt
    }

    class Room {
        +Long id
        +String name
    }

    class Device {
        +Long id
        +String name
        +DeviceType type
        +Boolean switchedOn
        +Double level
        +Double powerConsumptionWatt
        +LocalDateTime updatedAt
    }

    class Schedule {
        +Long id
        +String name
        +String cronExpression
        +String actionValue
        +Boolean active
    }

    class Rule {
        +Long id
        +String name
        +TriggerType triggerType
        +String triggerCondition
        +Double triggerThresholdValue
        +String actionValue
        +Boolean active
    }

    class Scene {
        +Long id
        +String name
    }

    class SceneDeviceState {
        +Long id
        +Boolean targetSwitchedOn
        +Double targetLevel
    }

    class ActivityLog {
        +Long id
        +String actor
        +String description
        +LocalDateTime timestamp
    }

    class EnergyLog {
        +Long id
        +LocalDateTime timestamp
        +Double consumptionWh
    }

    class Notification {
        +Long id
        +String message
        +LocalDateTime createdAt
        +Boolean read
    }

    class VacationMode {
        +Long id
        +LocalDate startDate
        +LocalDate endDate
        +Boolean active
    }

    %% ─── Enum Associations ───────────────────────────────────────────────────

    User --> UserRole : role
    Device --> DeviceType : type
    Rule --> TriggerType : triggerType

    %% ─── Entity Relationships ────────────────────────────────────────────────

    User "1" --> "0..*" Room : owns
    User "1" --> "0..*" Schedule : owns
    User "1" --> "0..*" Rule : owns
    User "1" --> "0..*" Scene : owns
    User "1" --> "0..*" Notification : receives
    User "1" --> "0..*" VacationMode : configures

    Room "1" *-- "0..*" Device : contains

    Device "1" --> "0..*" Schedule : targeted by
    Device "1" --> "0..*" ActivityLog : logged by
    Device "1" --> "0..*" EnergyLog : measured by
    Device "1" --> "0..*" SceneDeviceState : referenced by

    Rule "0..1" --> "1" Device : actionDevice
    Rule "0..1" --> "0..1" Device : triggerDevice

    Scene "1" *-- "0..*" SceneDeviceState : composed of
    SceneDeviceState "0..*" --> "1" Device : targets

    VacationMode "0..*" --> "1" Schedule : applies
```

## Entity Descriptions

| Entity | Table | Description |
|---|---|---|
| `User` | `users` | Registered user with OWNER or MEMBER role (FR-01, FR-13) |
| `Room` | `rooms` | Named group of devices belonging to an owner (FR-03) |
| `Device` | `devices` | Virtual smart-home device with typed state (FR-04, FR-07) |
| `Schedule` | `schedules` | Recurring time-based device action via cron expression (FR-09) |
| `Rule` | `rules` | IF-THEN automation: time, threshold, or event trigger (FR-10, FR-11) |
| `Scene` | `scenes` | Named set of device target states activated in one action (FR-17) |
| `SceneDeviceState` | `scene_device_states` | One device's target state within a scene |
| `ActivityLog` | `activity_logs` | Immutable audit record of every device state change (FR-08) |
| `EnergyLog` | `energy_logs` | Periodic energy consumption record per device (FR-14) |
| `Notification` | `notifications` | In-app notification sent on rule execution or scene activation (FR-12) |
| `VacationMode` | `vacation_modes` | Override schedule applied during a vacation date range (FR-21) |

## Key Business Rules

- A **Room** is deleted together with all its **Devices** (cascade).
- A **Device** deletion cascades to its **Schedules**, **ActivityLogs**, **EnergyLogs**, and **SceneDeviceStates**.
- A **Schedule** deletion cascades to any **VacationMode** that references it.
- Only users with role **OWNER** may invite or revoke members (FR-20).
- Two active schedules targeting the **same device** with the **same cron expression** are a conflict (FR-15) → rejected with HTTP 409.
- A **VacationMode** `endDate` must not be before `startDate`.
- Passwords are stored as **BCrypt** hashes — never in plain text (NFR-02).

## Vollständige Klassenübersicht

### Bootstrap

| Klasse | Beschreibung |
|---|---|
| `StartupDataLoader` | Initialisiert beim Start Test- und Beispieldaten für Benutzer, Räume, Geräte, Regeln und Logs. |

### DTOs — Request

| Klasse | Beschreibung |
|---|---|
| `DeviceCreateRequest` | Eingabedaten zum Anlegen eines neuen Geräts. |
| `DeviceRenameRequest` | Eingabedaten zum Umbenennen eines Geräts. |
| `DeviceStateRequest` | Eingabedaten zur manuellen Zustandsänderung eines Geräts. |
| `EnergyLogRequest` | Eingabedaten zum Erfassen von Energieverbrauchswerten. |
| `InviteRequest` | Eingabedaten zum Einladen eines Mitglieds durch einen Owner. |
| `RoomRequest` | Eingabedaten zum Anlegen oder Aktualisieren eines Raums. |
| `RuleRequest` | Eingabedaten zum Erstellen oder Aktualisieren einer Automatisierungsregel. |
| `SceneDeviceStateRequest` | Eingabedaten für den Zielzustand eines Geräts innerhalb einer Szene. |
| `SceneRequest` | Eingabedaten zum Anlegen oder Bearbeiten einer Szene. |
| `ScheduleRequest` | Eingabedaten zum Anlegen oder Aktualisieren eines Zeitplans. |
| `UserLoginRequest` | Eingabedaten für den Login (E-Mail und Passwort). |
| `UserRegisterRequest` | Eingabedaten für die Benutzerregistrierung. |
| `VacationModeRequest` | Eingabedaten für den Urlaubsmodus und dessen Zeitraum. |

### DTOs — Response

| Klasse | Beschreibung |
|---|---|
| `ActivityLogResponse` | Ausgabemodell für Aktivitätsprotokolle. |
| `ConflictResponse` | Ausgabemodell für erkannte Planungskonflikte. |
| `DeviceResponse` | Ausgabemodell für Gerätedaten und Gerätezustand. |
| `EnergyDashboardResponse` | Aggregierte Ausgabedaten für das Energie-Dashboard. |
| `EnergyLogResponse` | Ausgabemodell für einzelne Energieverbrauchseinträge. |
| `NotificationResponse` | Ausgabemodell für In-App-Benachrichtigungen. |
| `RoomResponse` | Ausgabemodell für Raumdaten. |
| `RuleResponse` | Ausgabemodell für Automatisierungsregeln. |
| `SceneDeviceStateResponse` | Ausgabemodell für Gerätezielzustände in Szenen. |
| `SceneResponse` | Ausgabemodell für Szenen inklusive enthaltenen Zielzuständen. |
| `ScheduleResponse` | Ausgabemodell für Zeitpläne. |
| `UserResponse` | Ausgabemodell für Benutzerdaten ohne Passwort-Hash. |
| `VacationModeResponse` | Ausgabemodell für Urlaubsmodus-Konfigurationen. |

### Entitäten

| Klasse | Beschreibung |
|---|---|
| `ActivityLog` | Persistiert Änderungen und Aktionen an Geräten als Audit-Log. |
| `Device` | Persistiert Smart-Home-Geräte mit Typ, Schaltzustand und Messwerten. |
| `EnergyLog` | Persistiert zeitbezogene Energieverbrauchswerte pro Gerät. |
| `Notification` | Persistiert Benachrichtigungen für Benutzer. |
| `Room` | Persistiert Räume als Gruppierung für Geräte. |
| `Rule` | Persistiert IF-THEN-Regeln für die Automatisierung. |
| `Scene` | Persistiert benannte Szenen mit mehreren Gerätezielzuständen. |
| `SceneDeviceState` | Persistiert den Zielzustand eines Geräts innerhalb einer Szene. |
| `Schedule` | Persistiert zeitbasierte Aktionen auf Basis von Cron-Ausdrücken. |
| `User` | Persistiert Benutzerkonto, Rolle und Sicherheitsinformationen. |
| `VacationMode` | Persistiert Urlaubsmodus-Zeiträume und zugewiesene Ersatzzeitpläne. |

### Enumerationen

| Klasse | Beschreibung |
|---|---|
| `DeviceType` | Definiert unterstützte Gerätetypen (z. B. SWITCH, DIMMER, THERMOSTAT). |
| `TriggerType` | Definiert Auslösertypen für Regeln (TIME_BASED, THRESHOLD, EVENT). |
| `UserRole` | Definiert Rollenmodell mit OWNER und MEMBER. |

### Mapper

| Klasse | Beschreibung |
|---|---|
| `ActivityLogMapper` | Wandelt `ActivityLog` in `ActivityLogResponse` um. |
| `DeviceMapper` | Wandelt `Device` in `DeviceResponse` um. |
| `EnergyLogMapper` | Wandelt `EnergyLog` in `EnergyLogResponse` um. |
| `NotificationMapper` | Wandelt `Notification` in `NotificationResponse` um. |
| `RoomMapper` | Wandelt `Room` in `RoomResponse` um. |
| `RuleMapper` | Wandelt `Rule` in `RuleResponse` um. |
| `SceneMapper` | Wandelt `Scene` in `SceneResponse` um. |
| `ScheduleMapper` | Wandelt `Schedule` in `ScheduleResponse` um. |
| `UserMapper` | Wandelt `User` in `UserResponse` um. |
| `VacationModeMapper` | Wandelt `VacationMode` in `VacationModeResponse` um. |

### Repositories

| Klasse | Beschreibung |
|---|---|
| `ActivityLogRepository` | Panache-Repository für Aktivitätslog-Abfragen und Persistenz. |
| `DeviceRepository` | Panache-Repository für Geräteabfragen und Gerätepersistenz. |
| `EnergyLogRepository` | Panache-Repository für Energieprotokolle und Auswertungen. |
| `NotificationRepository` | Panache-Repository für Benachrichtigungen pro Benutzer. |
| `RoomRepository` | Panache-Repository für Räume und zugehörige Abfragen. |
| `RuleRepository` | Panache-Repository für Regeln und Filter nach Benutzer/Gerät. |
| `SceneRepository` | Panache-Repository für Szenen und zugehörige Abfragen. |
| `ScheduleRepository` | Panache-Repository für Zeitpläne und Konfliktabfragen. |
| `UserRepository` | Panache-Repository für Benutzer, inklusive Suche nach E-Mail. |
| `VacationModeRepository` | Panache-Repository für Urlaubsmodus-Datensätze. |

### REST-Ressourcen

| Klasse | Beschreibung |
|---|---|
| `ActivityLogResource` | REST-Endpunkte zum Abrufen von Aktivitätsprotokollen und CSV-Export. |
| `ConflictResource` | REST-Endpunkte zur Prüfung von Planungs- und Regelkonflikten. |
| `DeviceResource` | REST-Endpunkte für Geräteverwaltung und Gerätesteuerung. |
| `EnergyResource` | REST-Endpunkte für Energieverbrauchsdaten und Dashboard-Werte. |
| `IoTResource` | REST-Endpunkte für IoT-bezogene Aktionen und Integrationszugriffe. |
| `NotificationResource` | REST-Endpunkte für Benachrichtigungen eines Benutzers. |
| `RoomResource` | REST-Endpunkte für Raum-CRUD-Operationen. |
| `RuleResource` | REST-Endpunkte für Regel-CRUD und Regelaktivierung. |
| `SceneResource` | REST-Endpunkte für Szenen-CRUD und Szenenaktivierung. |
| `ScheduleResource` | REST-Endpunkte für Zeitplan-CRUD mit Konflikterkennung. |
| `UserResource` | REST-Endpunkte für Registrierung, Login und Mitgliederverwaltung. |
| `VacationModeResource` | REST-Endpunkte für Urlaubsmodus-CRUD und Statusverwaltung. |

### Services

| Klasse | Beschreibung |
|---|---|
| `ConflictDetectionService` | Enthält Regeln zur Erkennung von Konflikten in Zeitplänen und Automatisierungen. |
| `RuleEngineService` | Führt Regeln aus, bewertet Triggerbedingungen und löst Geräteaktionen aus. |

### IoT-Integration

| Klasse | Beschreibung |
|---|---|
| `DeviceCommand` | Modelliert auszuführende IoT-Befehle für Geräteaktionen. |
| `DeviceEvent` | Modelliert eingehende IoT-Ereignisse aus externen Quellen. |
| `IoTException` | Fachspezifische Exception für Fehler in der IoT-Integrationsschicht. |
| `IoTIntegrationService` | Koordiniert IoT-Kommunikation zwischen Domäne und Protokolladaptern. |
| `IoTProtocol` | Abstraktion für unterstützte IoT-Protokollimplementierungen. |
| `IoTProtocolFactory` | Erstellt und liefert passende Protokolladapter zur Laufzeit. |
| `MockProtocolAdapter` | Simulierter Adapter für lokale Entwicklung und Tests. |
| `MqttProtocolAdapter` | MQTT-Adapter für echte oder emulierte Gerätekommunikation. |

### WebSocket

| Klasse | Beschreibung |
|---|---|
| `DeviceEventBroadcaster` | Verteilt Geräteereignisse an verbundene WebSocket-Clients. |
| `DeviceStateSocket` | WebSocket-Endpunkt für Echtzeit-Updates von Gerätezuständen. |
