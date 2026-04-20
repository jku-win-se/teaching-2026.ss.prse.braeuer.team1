package at.jku.se.dto.response;

import at.jku.se.entity.enums.TriggerType;

/** Response DTO for an automation rule. */
public class RuleResponse {

    /** Creates an empty response; fields are populated by the mapping layer. */
    public RuleResponse() {}

    /** Database identifier of the rule. */
    public Long id;
    /** Display name of the rule. */
    public String name;
    /** Trigger category (TIME_BASED, THRESHOLD, EVENT). */
    public TriggerType triggerType;
    /** Textual trigger condition (e.g., cron expression or description). */
    public String triggerCondition;
    /** Identifier of the device being monitored, or null for TIME_BASED. */
    public Long triggerDeviceId;
    /** Name of the device being monitored, or null for TIME_BASED. */
    public String triggerDeviceName;
    /** Numeric threshold value for THRESHOLD triggers. */
    public Double triggerThresholdValue;
    /** Identifier of the device acted upon when the rule fires. */
    public Long actionDeviceId;
    /** Name of the device acted upon when the rule fires. */
    public String actionDeviceName;
    /** Value applied to the action device when the rule fires. */
    public String actionValue;
    /** Whether the rule is currently active. */
    public Boolean active;
    /** Identifier of the owning user. */
    public Long userId;
}
