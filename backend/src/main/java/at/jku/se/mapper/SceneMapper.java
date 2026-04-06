package at.jku.se.mapper;

import at.jku.se.dto.response.SceneDeviceStateResponse;
import at.jku.se.dto.response.SceneResponse;
import at.jku.se.entity.Scene;
import at.jku.se.entity.SceneDeviceState;

/** Maps {@link Scene} entities to response DTOs. */
public class SceneMapper {

    private SceneMapper() {
    }

    /**
     * Converts a Scene entity to a SceneResponse DTO including all device states.
     *
     * @param scene the entity to convert
     * @return the response DTO
     */
    public static SceneResponse toResponse(Scene scene) {
        SceneResponse r = new SceneResponse();
        r.id = scene.id;
        r.name = scene.name;
        r.userId = scene.user.id;
        r.deviceStates = scene.deviceStates.stream()
                .map(SceneMapper::toStateResponse)
                .toList();
        return r;
    }

    private static SceneDeviceStateResponse toStateResponse(SceneDeviceState state) {
        SceneDeviceStateResponse r = new SceneDeviceStateResponse();
        r.id = state.id;
        r.deviceId = state.device.id;
        r.deviceName = state.device.name;
        r.targetSwitchedOn = state.targetSwitchedOn;
        r.targetLevel = state.targetLevel;
        return r;
    }
}
