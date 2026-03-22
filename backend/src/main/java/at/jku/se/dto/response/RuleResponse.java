package at.jku.se.dto.response;

import at.jku.se.entity.enums.TriggerType;

/** Response DTO for an automation rule. */
public class RuleResponse {
    public Long id;
    public String name;
    public TriggerType triggerType;
    public String triggerCondition;
    public Long triggerDeviceId;
    public String triggerDeviceName;
    public Double triggerThresholdValue;
    public Long actionDeviceId;
    public String actionDeviceName;
    public String actionValue;
    public Boolean active;
    public Long userId;
}
