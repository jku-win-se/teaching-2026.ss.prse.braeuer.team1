package at.jku.se.dto.request;

import jakarta.validation.constraints.NotNull;

/** The desired state for one device within a scene (FR-17). */
public class SceneDeviceStateRequest {

    /** Creates an empty request; fields are populated during deserialization. */
    public SceneDeviceStateRequest() {}

    /** Identifier of the target device. */
    @NotNull
    public Long deviceId;

    /** Target on/off state for SWITCH. Null = do not change this aspect. */
    public Boolean targetSwitchedOn;

    /** Target numeric level. Null = do not change this aspect. */
    public Double targetLevel;
}
