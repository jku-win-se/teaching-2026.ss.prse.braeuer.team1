package at.jku.se.dto.response;

import at.jku.se.entity.enums.DeviceType;
import java.time.LocalDateTime;

/** Response DTO for a device including its current state (FR-07). */
public class DeviceResponse {

    /** Creates an empty response; fields are populated by the mapping layer. */
    public DeviceResponse() {}

    /** Database identifier of the device. */
    public Long id;
    /** Display name of the device. */
    public String name;
    /** Device category (SWITCH, DIMMER, THERMOSTAT, SENSOR, BLIND). */
    public DeviceType type;
    /** Identifier of the room the device is assigned to. */
    public Long roomId;
    /** Name of the room the device is assigned to. */
    public String roomName;
    /** Whether the device is currently switched on (applies to SWITCH devices). */
    public Boolean switchedOn;
    /** Current analog level (percentage, temperature, sensor value, blind position). */
    public Double level;
    /** Nominal power consumption in Watts. */
    public Double powerConsumptionWatt;
    /** Timestamp of the last state change. */
    public LocalDateTime updatedAt;
}
