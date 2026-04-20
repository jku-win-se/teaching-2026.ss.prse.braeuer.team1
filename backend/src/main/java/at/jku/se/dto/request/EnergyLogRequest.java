package at.jku.se.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/** Request body for manually recording an energy consumption entry (for testing / FR-14). */
public class EnergyLogRequest {

    /** Creates an empty request; fields are populated during deserialization. */
    public EnergyLogRequest() {}

    /** Identifier of the device the entry applies to. */
    @NotNull
    public Long deviceId;

    /** Timestamp of the measurement. */
    @NotNull
    public LocalDateTime timestamp;

    /** Energy consumed in watt-hours (Wh) during the measurement period. */
    @NotNull
    public Double consumptionWh;
}
