package at.jku.se.dto.response;

/** Response DTO for a schedule. */
public class ScheduleResponse {
    /** Unique identifier of the schedule. */
    public Long id;

    /** Display name of the schedule. */
    public String name;

    /** Cron expression defining when the schedule runs. */
    public String cronExpression;

    /** Identifier of the target device. */
    public Long deviceId;

    /** Human-readable name of the target device. */
    public String deviceName;

    /** Action payload to apply to the target device. */
    public String actionValue;

    /** Whether the schedule is currently enabled. */
    public Boolean active;

    /** Identifier of the owning user. */
    public Long userId;

    /** Default constructor for serialization frameworks. */
    public ScheduleResponse() {
    }
}
