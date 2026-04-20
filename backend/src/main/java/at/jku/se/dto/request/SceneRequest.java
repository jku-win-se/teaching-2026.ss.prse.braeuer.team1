package at.jku.se.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/** Request body for creating or updating a scene (FR-17). */
public class SceneRequest {

    /** Creates an empty request; fields are populated during deserialization. */
    public SceneRequest() {}

    /** Display name of the scene. */
    @NotBlank
    public String name;

    /** Identifier of the user who owns the scene. */
    @NotNull
    public Long userId;

    /** Device target states that make up the scene. */
    public List<SceneDeviceStateRequest> deviceStates = new ArrayList<>();
}
