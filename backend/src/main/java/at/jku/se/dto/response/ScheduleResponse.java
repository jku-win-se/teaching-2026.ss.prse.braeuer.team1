package at.jku.se.dto.response;

/** Response DTO for a schedule. */
public class ScheduleResponse {
    public Long id;
    public String name;
    public String cronExpression;
    public Long deviceId;
    public String deviceName;
    public String actionValue;
    public Boolean active;
    public Long userId;
}
