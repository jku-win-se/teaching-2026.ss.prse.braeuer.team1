package at.jku.se.mapper;

import at.jku.se.dto.response.ActivityLogResponse;
import at.jku.se.entity.ActivityLog;
import at.jku.se.entity.Device;
import at.jku.se.entity.Room;
import at.jku.se.entity.User;
import at.jku.se.entity.enums.DeviceType;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ActivityLogMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        User owner = new User();
        owner.id = 1L;
        Room room = new Room();
        room.id = 2L;
        room.name = "Hall";
        room.user = owner;
        Device device = new Device();
        device.id = 3L;
        device.name = "Sensor";
        device.type = DeviceType.SENSOR;
        device.room = room;

        ActivityLog log = new ActivityLog();
        log.id = 99L;
        log.device = device;
        log.actor = "alice";
        log.description = "turned on";
        log.timestamp = LocalDateTime.of(2026, 3, 22, 10, 0);

        ActivityLogResponse r = ActivityLogMapper.toResponse(log);
        assertEquals(99L, r.id);
        assertEquals(3L, r.deviceId);
        assertEquals("Sensor", r.deviceName);
        assertEquals("Hall", r.roomName);
        assertEquals("alice", r.actor);
        assertEquals("turned on", r.description);
        assertEquals(LocalDateTime.of(2026, 3, 22, 10, 0), r.timestamp);
    }
}
