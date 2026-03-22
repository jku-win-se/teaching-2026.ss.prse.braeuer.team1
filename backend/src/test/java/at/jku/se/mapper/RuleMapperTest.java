package at.jku.se.mapper;

import at.jku.se.dto.response.RuleResponse;
import at.jku.se.entity.Device;
import at.jku.se.entity.Room;
import at.jku.se.entity.Rule;
import at.jku.se.entity.User;
import at.jku.se.entity.enums.DeviceType;
import at.jku.se.entity.enums.TriggerType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RuleMapperTest {

    private Device createDevice(Long id, String name) {
        User owner = new User();
        owner.id = 1L;
        Room room = new Room();
        room.id = 1L;
        room.name = "Room";
        room.user = owner;
        Device d = new Device();
        d.id = id;
        d.name = name;
        d.type = DeviceType.SENSOR;
        d.room = room;
        return d;
    }

    @Test
    void toResponse_withTriggerDevice_mapsAllFields() {
        User user = new User();
        user.id = 1L;
        Device trigger = createDevice(10L, "Sensor");
        Device action = createDevice(20L, "Light");

        Rule rule = new Rule();
        rule.id = 5L;
        rule.name = "Auto Light";
        rule.triggerType = TriggerType.THRESHOLD;
        rule.triggerCondition = "value >= 1";
        rule.triggerDevice = trigger;
        rule.triggerThresholdValue = 1.0;
        rule.actionDevice = action;
        rule.actionValue = "true";
        rule.active = true;
        rule.user = user;

        RuleResponse r = RuleMapper.toResponse(rule);
        assertEquals(5L, r.id);
        assertEquals("Auto Light", r.name);
        assertEquals(TriggerType.THRESHOLD, r.triggerType);
        assertEquals(10L, r.triggerDeviceId);
        assertEquals("Sensor", r.triggerDeviceName);
        assertEquals(1.0, r.triggerThresholdValue);
        assertEquals(20L, r.actionDeviceId);
        assertEquals("Light", r.actionDeviceName);
        assertEquals("true", r.actionValue);
        assertTrue(r.active);
        assertEquals(1L, r.userId);
    }

    @Test
    void toResponse_withoutTriggerDevice_leavesFieldsNull() {
        User user = new User();
        user.id = 1L;
        Device action = createDevice(20L, "Light");

        Rule rule = new Rule();
        rule.id = 6L;
        rule.name = "Time Rule";
        rule.triggerType = TriggerType.TIME_BASED;
        rule.triggerCondition = "0 0 * * *";
        rule.triggerDevice = null;
        rule.triggerThresholdValue = null;
        rule.actionDevice = action;
        rule.actionValue = "false";
        rule.active = false;
        rule.user = user;

        RuleResponse r = RuleMapper.toResponse(rule);
        assertNull(r.triggerDeviceId);
        assertNull(r.triggerDeviceName);
    }
}
