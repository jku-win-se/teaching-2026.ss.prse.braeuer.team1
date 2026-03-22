package at.jku.se.mapper;

import at.jku.se.dto.response.ScheduleResponse;
import at.jku.se.entity.Device;
import at.jku.se.entity.Room;
import at.jku.se.entity.Schedule;
import at.jku.se.entity.User;
import at.jku.se.entity.enums.DeviceType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        User user = new User();
        user.id = 1L;
        Room room = new Room();
        room.id = 1L;
        room.name = "Room";
        room.user = user;
        Device device = new Device();
        device.id = 3L;
        device.name = "Light";
        device.type = DeviceType.SWITCH;
        device.room = room;

        Schedule s = new Schedule();
        s.id = 7L;
        s.name = "Morning";
        s.cronExpression = "0 7 * * MON-FRI";
        s.device = device;
        s.actionValue = "true";
        s.active = true;
        s.user = user;

        ScheduleResponse r = ScheduleMapper.toResponse(s);
        assertEquals(7L, r.id);
        assertEquals("Morning", r.name);
        assertEquals("0 7 * * MON-FRI", r.cronExpression);
        assertEquals(3L, r.deviceId);
        assertEquals("Light", r.deviceName);
        assertEquals("true", r.actionValue);
        assertTrue(r.active);
        assertEquals(1L, r.userId);
    }
}
