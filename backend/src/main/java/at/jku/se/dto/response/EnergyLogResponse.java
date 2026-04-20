package at.jku.se.dto.response;

import java.time.LocalDateTime;

/** Response DTO for a single energy log entry. */
public class EnergyLogResponse {

    /** Creates an empty response; fields are populated by the mapping layer. */
    public EnergyLogResponse() {}

    /** Database identifier of the log entry. */
    public Long id;
    /** Identifier of the device the entry belongs to. */
    public Long deviceId;
    /** Display name of the device at the time of recording. */
    public String deviceName;
    /** Timestamp the measurement was recorded. */
    public LocalDateTime timestamp;
    /** Energy consumed in watt-hours (Wh) during the measurement period. */
    public Double consumptionWh;
}
