package at.jku.se.dto.request;

import at.jku.se.entity.enums.TriggerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request body for creating or updating an automation rule (FR-10, FR-11). */
public class RuleRequest {

    /** Creates an empty request; fields are populated during deserialization. */
    public RuleRequest() {}

    /** Display name of the rule. */
    @NotBlank
    public String name;

    /** How the rule is triggered (TIME_BASED, THRESHOLD, EVENT). */
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

    /** Identifier of the device the rule's action applies to. */
    @NotNull
    public Long actionDeviceId;

    /** Value to set on the action device when the rule fires. */
    @NotBlank
    public String actionValue;

    /** Whether the rule is active and should be evaluated. */
    @NotNull
    public Boolean active;

    /** Identifier of the user who owns the rule. */
    @NotNull
    public Long userId;
}
