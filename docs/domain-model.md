# SmartHome Orchestrator — Domain Model

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
