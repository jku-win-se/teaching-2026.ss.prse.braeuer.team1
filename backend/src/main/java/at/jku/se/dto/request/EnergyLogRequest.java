package at.jku.se.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/** Request body for manually recording an energy consumption entry (for testing / FR-14). */
public class EnergyLogRequest {

    @NotNull
    public Long deviceId;

    @NotNull
    public LocalDateTime timestamp;

    /** Energy consumed in watt-hours (Wh) during the measurement period. */
    @NotNull
    public Double consumptionWh;
}
