package at.jku.se.mapper;

import at.jku.se.dto.response.DeviceResponse;
import at.jku.se.entity.Device;
import at.jku.se.entity.Room;
import at.jku.se.entity.User;
import at.jku.se.entity.enums.DeviceType;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DeviceMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        User owner = new User();
        owner.id = 1L;
        Room room = new Room();
        room.id = 10L;
        room.name = "Living Room";
        room.user = owner;

        Device d = new Device();
        d.id = 5L;
        d.name = "Lamp";
        d.type = DeviceType.SWITCH;
        d.room = room;
        d.switchedOn = true;
        d.level = null;
        d.powerConsumptionWatt = 60.0;
        d.updatedAt = LocalDateTime.of(2026, 3, 1, 12, 0);

        DeviceResponse r = DeviceMapper.toResponse(d);
        assertEquals(5L, r.id);
        assertEquals("Lamp", r.name);
        assertEquals(DeviceType.SWITCH, r.type);
        assertEquals(10L, r.roomId);
        assertEquals("Living Room", r.roomName);
        assertTrue(r.switchedOn);
        assertNull(r.level);
        assertEquals(60.0, r.powerConsumptionWatt);
        assertEquals(LocalDateTime.of(2026, 3, 1, 12, 0), r.updatedAt);
    }
}
