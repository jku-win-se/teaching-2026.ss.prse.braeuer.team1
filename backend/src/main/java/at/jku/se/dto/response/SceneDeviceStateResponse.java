package at.jku.se.dto.response;

/** Response DTO for a single device-state entry within a scene. */
public class SceneDeviceStateResponse {

    /** Creates an empty response; fields are populated by the mapping layer. */
    public SceneDeviceStateResponse() {}

    /** Database identifier of the entry. */
    public Long id;
    /** Identifier of the target device. */
    public Long deviceId;
    /** Display name of the target device. */
    public String deviceName;
    /** Target on/off state for SWITCH devices, or null to skip. */
    public Boolean targetSwitchedOn;
    /** Target analog level, or null to skip. */
    public Double targetLevel;
}
