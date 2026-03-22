package at.jku.se.dto.response;

import java.time.LocalDateTime;

/** Response DTO for a single energy log entry. */
public class EnergyLogResponse {
    public Long id;
    public Long deviceId;
    public String deviceName;
    public LocalDateTime timestamp;
    public Double consumptionWh;
}
