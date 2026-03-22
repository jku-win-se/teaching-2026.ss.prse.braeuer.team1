package at.jku.se.dto.response;

/** Response DTO for a single device-state entry within a scene. */
public class SceneDeviceStateResponse {
    public Long id;
    public Long deviceId;
    public String deviceName;
    public Boolean targetSwitchedOn;
    public Double targetLevel;
}
