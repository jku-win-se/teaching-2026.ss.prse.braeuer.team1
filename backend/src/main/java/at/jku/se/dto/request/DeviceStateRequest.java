package at.jku.se.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for manually controlling a device's state (FR-06).
 * Set {@code switchedOn} for SWITCH devices.
 * Set {@code level} for DIMMER (0–100 %), THERMOSTAT (°C), SENSOR (value), BLIND (0–100 %).
 */
public class DeviceStateRequest {

    /** Creates an empty request; fields are populated during deserialization. */
    public DeviceStateRequest() {}

    /** Desired on/off state for SWITCH devices. */
    public Boolean switchedOn;

    /** Desired analog level (dimmer %, temperature °C, sensor value, blind position %). */
    public Double level;

    /** The user or system component performing the action — recorded in the activity log. */
    @NotBlank
    public String actor;
}
