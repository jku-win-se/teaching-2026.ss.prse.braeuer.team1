package at.jku.se.dto.request;

import at.jku.se.entity.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request body for adding a new virtual device to a room (FR-04). */
public class DeviceCreateRequest {

    /** Creates an empty request; fields are populated during deserialization. */
    public DeviceCreateRequest() {}

    /** Display name for the new device. */
    @NotBlank
    public String name;

    /** Device category (SWITCH, DIMMER, THERMOSTAT, SENSOR, BLIND). */
    @NotNull
    public DeviceType type;

    /** Nominal power consumption in Watts — used for energy estimation (FR-14). */
    public Double powerConsumptionWatt;
}
