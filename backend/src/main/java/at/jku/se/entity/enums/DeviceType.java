package at.jku.se.entity.enums;

/**
 * Supported virtual smart device types.
 */
public enum DeviceType {
    /** On/off switch. Uses {@code switchedOn} field. */
    SWITCH,
    /** Dimmable light. Uses {@code level} (0–100 %). */
    DIMMER,
    /** Thermostat. Uses {@code level} as target temperature in °C. */
    THERMOSTAT,
    /** Sensor (e.g., motion, temperature). Uses {@code level} for the sensor value. */
    SENSOR,
    /** Blind / roller shutter. Uses {@code level} (0 = closed, 100 = fully open). */
    BLIND
}
