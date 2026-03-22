package at.jku.se.entity.enums;

/**
 * The three supported trigger types for automation rules (FR-11).
 */
public enum TriggerType {
    /** Fires at a specific time or on a recurring schedule. */
    TIME_BASED,
    /** Fires when a sensor value crosses a defined threshold. */
    THRESHOLD,
    /** Fires when a device changes to a specific state. */
    EVENT
}
