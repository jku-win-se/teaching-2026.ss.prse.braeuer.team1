package at.jku.se.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/** Request body for creating or updating a scene (FR-17). */
public class SceneRequest {

    @NotBlank
    public String name;

    @NotNull
    public Long userId;

    public List<SceneDeviceStateRequest> deviceStates = new ArrayList<>();
}
