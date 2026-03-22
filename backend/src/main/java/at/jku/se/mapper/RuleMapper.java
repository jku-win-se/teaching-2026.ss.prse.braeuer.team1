package at.jku.se.mapper;

import at.jku.se.dto.response.RuleResponse;
import at.jku.se.entity.Rule;

/** Maps {@link Rule} entities to response DTOs. */
public class RuleMapper {

    private RuleMapper() {
    }

    /**
     * Converts a Rule entity to a RuleResponse DTO.
     *
     * @param rule the entity to convert
     * @return the response DTO
     */
    public static RuleResponse toResponse(Rule rule) {
        RuleResponse r = new RuleResponse();
        r.id = rule.id;
        r.name = rule.name;
        r.triggerType = rule.triggerType;
        r.triggerCondition = rule.triggerCondition;
        if (rule.triggerDevice != null) {
            r.triggerDeviceId = rule.triggerDevice.id;
            r.triggerDeviceName = rule.triggerDevice.name;
        }
        r.triggerThresholdValue = rule.triggerThresholdValue;
        r.actionDeviceId = rule.actionDevice.id;
        r.actionDeviceName = rule.actionDevice.name;
        r.actionValue = rule.actionValue;
        r.active = rule.active;
        r.userId = rule.user.id;
        return r;
    }
}
