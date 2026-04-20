package at.jku.se.dto.response;

import java.time.LocalDateTime;

/** Response DTO for a single activity log entry (FR-08). */
public class ActivityLogResponse {

    /** Creates an empty response; fields are populated by the mapping layer. */
    public ActivityLogResponse() {}

    /** Database identifier of the log entry. */
    public Long id;
    /** Identifier of the device the action was performed on. */
    public Long deviceId;
    /** Human-readable name of the device at the time of the action. */
    public String deviceName;
    /** Name of the room the device belongs to. */
    public String roomName;
    /** User or system component that performed the action. */
    public String actor;
    /** Human-readable description of the action. */
    public String description;
    /** Timestamp when the action occurred. */
    public LocalDateTime timestamp;
}
