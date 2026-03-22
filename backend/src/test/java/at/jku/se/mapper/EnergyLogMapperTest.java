package at.jku.se.mapper;

import at.jku.se.dto.response.EnergyLogResponse;
import at.jku.se.entity.Device;
import at.jku.se.entity.EnergyLog;
import at.jku.se.entity.Room;
import at.jku.se.entity.User;
import at.jku.se.entity.enums.DeviceType;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EnergyLogMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        User owner = new User();
        owner.id = 1L;
        Room room = new Room();
        room.id = 2L;
        room.name = "Kitchen";
        room.user = owner;
        Device device = new Device();
        device.id = 5L;
        device.name = "Oven";
        device.type = DeviceType.SWITCH;
        device.room = room;

        EnergyLog log = new EnergyLog();
        log.id = 77L;
        log.device = device;
        log.timestamp = LocalDateTime.of(2026, 3, 22, 14, 0);
        log.consumptionWh = 120.5;

        EnergyLogResponse r = EnergyLogMapper.toResponse(log);
        assertEquals(77L, r.id);
        assertEquals(5L, r.deviceId);
        assertEquals("Oven", r.deviceName);
        assertEquals(LocalDateTime.of(2026, 3, 22, 14, 0), r.timestamp);
        assertEquals(120.5, r.consumptionWh);
    }
}
