package at.jku.se.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for manually controlling a device's state (FR-06).
 * Set {@code switchedOn} for SWITCH devices.
 * Set {@code level} for DIMMER (0–100 %), THERMOSTAT (°C), SENSOR (value), BLIND (0–100 %).
 */
public class DeviceStateRequest {

    public Boolean switchedOn;

    public Double level;

    /** The user or system component performing the action — recorded in the activity log. */
    @NotBlank
    public String actor;
}
