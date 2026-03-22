package at.jku.se.mapper;

import at.jku.se.dto.response.RoomResponse;
import at.jku.se.entity.Room;

/** Maps {@link Room} entities to response DTOs. */
public class RoomMapper {

    private RoomMapper() {
    }

    /**
     * Converts a Room entity to a RoomResponse DTO.
     *
     * @param room the entity to convert
     * @return the response DTO
     */
    public static RoomResponse toResponse(Room room) {
        RoomResponse r = new RoomResponse();
        r.id = room.id;
        r.name = room.name;
        r.userId = room.user.id;
        r.ownerEmail = room.user.email;
        return r;
    }
}
