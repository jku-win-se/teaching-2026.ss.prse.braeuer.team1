package at.jku.se.dto.request;

import at.jku.se.entity.enums.TriggerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request body for creating or updating an automation rule (FR-10, FR-11). */
public class RuleRequest {

    @NotBlank
    public String name;

    @NotNull
    public TriggerType triggerType;

    /**
     * Cron expression for TIME_BASED, or a human-readable description for THRESHOLD/EVENT.
     * Example: {@code "0 22 * * *"} (every day at 22:00) or {@code "temperature < 18"}.
     */
    public String triggerCondition;

    /** For THRESHOLD and EVENT: the device being monitored. Null for TIME_BASED. */
    public Long triggerDeviceId;

    /** For THRESHOLD: the numeric threshold value that fires the rule. */
    public Double triggerThresholdValue;

    @NotNull
    public Long actionDeviceId;

    /** Value to set on the action device when the rule fires. */
    @NotBlank
    public String actionValue;

    @NotNull
    public Boolean active;

    @NotNull
    public Long userId;
}
