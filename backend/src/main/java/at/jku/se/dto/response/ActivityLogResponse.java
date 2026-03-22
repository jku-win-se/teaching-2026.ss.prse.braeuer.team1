package at.jku.se.dto.response;

import java.time.LocalDateTime;

/** Response DTO for a single activity log entry (FR-08). */
public class ActivityLogResponse {
    public Long id;
    public Long deviceId;
    public String deviceName;
    public String roomName;
    public String actor;
    public String description;
    public LocalDateTime timestamp;
}
