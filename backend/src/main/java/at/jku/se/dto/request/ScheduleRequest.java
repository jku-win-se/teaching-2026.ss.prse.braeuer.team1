package at.jku.se.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request body for creating or updating a time-based schedule (FR-09). */
public class ScheduleRequest {

    @NotBlank
    public String name;

    /**
     * Cron expression (5-field: minute hour day month weekday).
     * Example: {@code "0 7 * * MON-FRI"} = every weekday at 07:00.
     */
    @NotBlank
    public String cronExpression;

    @NotNull
    public Long deviceId;

    /**
     * Value to apply when the schedule fires.
     * Use {@code "true"}/{@code "false"} for SWITCH, a number string for DIMMER/THERMOSTAT/BLIND/SENSOR.
     */
    @NotBlank
    public String actionValue;

    @NotNull
    public Boolean active;

    @NotNull
    public Long userId;
}
