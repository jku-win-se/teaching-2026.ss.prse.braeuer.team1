package at.jku.se.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request body for creating or updating a time-based schedule (FR-09). */
public class ScheduleRequest {

    /** Creates an empty request; fields are populated during deserialization. */
    public ScheduleRequest() {}

    /** Display name of the schedule. */
    @NotBlank
    public String name;

    /**
     * Cron expression (5-field: minute hour day month weekday).
     * Example: {@code "0 7 * * MON-FRI"} = every weekday at 07:00.
     */
    @NotBlank
    public String cronExpression;

    /** Identifier of the target device. */
    @NotNull
    public Long deviceId;

    /**
     * Value to apply when the schedule fires.
     * Use {@code "true"}/{@code "false"} for SWITCH, a number string for DIMMER/THERMOSTAT/BLIND/SENSOR.
     */
    @NotBlank
    public String actionValue;

    /** Whether the schedule is active and should fire. */
    @NotNull
    public Boolean active;

    /** Identifier of the owning user. */
    @NotNull
    public Long userId;
}
