package at.jku.se.dto.response;

import java.util.List;

/** Response DTO for a scene including all its device states. */
public class SceneResponse {
    public Long id;
    public String name;
    public Long userId;
    public List<SceneDeviceStateResponse> deviceStates;
}
