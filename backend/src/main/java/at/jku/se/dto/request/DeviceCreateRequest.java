package at.jku.se.dto.request;

import at.jku.se.entity.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request body for adding a new virtual device to a room (FR-04). */
public class DeviceCreateRequest {

    @NotBlank
    public String name;

    @NotNull
    public DeviceType type;

    /** Nominal power consumption in Watts — used for energy estimation (FR-14). */
    public Double powerConsumptionWatt;
}
