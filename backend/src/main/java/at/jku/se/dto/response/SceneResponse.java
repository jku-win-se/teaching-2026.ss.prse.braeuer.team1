package at.jku.se.dto.response;

import java.util.List;

/** Response DTO for a scene including all its device states. */
public class SceneResponse {

    /** Creates an empty response; fields are populated by the mapping layer. */
    public SceneResponse() {}

    /** Database identifier of the scene. */
    public Long id;
    /** Display name of the scene. */
    public String name;
    /** Identifier of the owning user. */
    public Long userId;
    /** Target device states that make up the scene. */
    public List<SceneDeviceStateResponse> deviceStates;
}
