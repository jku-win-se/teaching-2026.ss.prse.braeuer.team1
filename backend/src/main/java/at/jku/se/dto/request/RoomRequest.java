package at.jku.se.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request body for creating or renaming a room (FR-03). */
public class RoomRequest {

    /** Creates an empty request; fields are populated during deserialization. */
    public RoomRequest() {}

    /** Display name of the room. */
    @NotBlank
    public String name;

    /** Identifier of the user who owns the room. */
    @NotNull
    public Long userId;
}
