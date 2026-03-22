package at.jku.se.mapper;

import at.jku.se.dto.response.VacationModeResponse;
import at.jku.se.entity.Device;
import at.jku.se.entity.Room;
import at.jku.se.entity.Schedule;
import at.jku.se.entity.User;
import at.jku.se.entity.VacationMode;
import at.jku.se.entity.enums.DeviceType;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class VacationModeMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        User user = new User();
        user.id = 1L;
        Room room = new Room();
        room.id = 1L;
        room.name = "Room";
        room.user = user;
        Device device = new Device();
        device.id = 1L;
        device.name = "Light";
        device.type = DeviceType.SWITCH;
        device.room = room;

        Schedule schedule = new Schedule();
        schedule.id = 2L;
        schedule.name = "Morning";
        schedule.device = device;
        schedule.user = user;

        VacationMode vm = new VacationMode();
        vm.id = 8L;
        vm.user = user;
        vm.startDate = LocalDate.of(2026, 7, 1);
        vm.endDate = LocalDate.of(2026, 7, 14);
        vm.schedule = schedule;
        vm.active = true;

        VacationModeResponse r = VacationModeMapper.toResponse(vm);
        assertEquals(8L, r.id);
        assertEquals(1L, r.userId);
        assertEquals(LocalDate.of(2026, 7, 1), r.startDate);
        assertEquals(LocalDate.of(2026, 7, 14), r.endDate);
        assertEquals(2L, r.scheduleId);
        assertEquals("Morning", r.scheduleName);
        assertTrue(r.active);
    }
}
