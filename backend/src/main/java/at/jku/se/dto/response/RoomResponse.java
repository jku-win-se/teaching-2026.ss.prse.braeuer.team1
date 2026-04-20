package at.jku.se.dto.response;

/** Response DTO for a room. */
public class RoomResponse {

    /** Creates an empty response; fields are populated by the mapping layer. */
    public RoomResponse() {}

    /** Database identifier of the room. */
    public Long id;
    /** Display name of the room. */
    public String name;
    /** Identifier of the owning user. */
    public Long userId;
    /** Email of the owning user. */
    public String ownerEmail;
}
