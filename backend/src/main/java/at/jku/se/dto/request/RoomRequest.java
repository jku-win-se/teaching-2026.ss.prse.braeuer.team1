package at.jku.se.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request body for creating or renaming a room (FR-03). */
public class RoomRequest {

    @NotBlank
    public String name;

    @NotNull
    public Long userId;
}
