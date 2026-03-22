package at.jku.se.dto.response;

import at.jku.se.entity.enums.DeviceType;
import java.time.LocalDateTime;

/** Response DTO for a device including its current state (FR-07). */
public class DeviceResponse {
    public Long id;
    public String name;
    public DeviceType type;
    public Long roomId;
    public String roomName;
    public Boolean switchedOn;
    public Double level;
    public Double powerConsumptionWatt;
    public LocalDateTime updatedAt;
}
