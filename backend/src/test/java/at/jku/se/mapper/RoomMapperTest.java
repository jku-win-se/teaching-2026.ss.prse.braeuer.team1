package at.jku.se.mapper;

import at.jku.se.dto.response.RoomResponse;
import at.jku.se.entity.Room;
import at.jku.se.entity.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoomMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        User u = new User();
        u.id = 7L;
        u.email = "owner@test.com";

        Room room = new Room();
        room.id = 3L;
        room.name = "Kitchen";
        room.user = u;

        RoomResponse r = RoomMapper.toResponse(room);
        assertEquals(3L, r.id);
        assertEquals("Kitchen", r.name);
        assertEquals(7L, r.userId);
        assertEquals("owner@test.com", r.ownerEmail);
    }
}
