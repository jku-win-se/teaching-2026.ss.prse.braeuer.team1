package at.jku.se.mapper;

import at.jku.se.dto.response.SceneResponse;
import at.jku.se.entity.Device;
import at.jku.se.entity.Room;
import at.jku.se.entity.Scene;
import at.jku.se.entity.SceneDeviceState;
import at.jku.se.entity.User;
import at.jku.se.entity.enums.DeviceType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SceneMapperTest {

    @Test
    void toResponse_mapsSceneAndDeviceStates() {
        User user = new User();
        user.id = 1L;
        Room room = new Room();
        room.id = 1L;
        room.name = "Room";
        room.user = user;
        Device device = new Device();
        device.id = 10L;
        device.name = "Dimmer";
        device.type = DeviceType.DIMMER;
        device.room = room;

        SceneDeviceState state = new SceneDeviceState();
        state.id = 100L;
        state.device = device;
        state.targetSwitchedOn = null;
        state.targetLevel = 50.0;

        Scene scene = new Scene();
        scene.id = 5L;
        scene.name = "Test Scene";
        scene.user = user;
        scene.deviceStates.add(state);
        state.scene = scene;

        SceneResponse r = SceneMapper.toResponse(scene);
        assertEquals(5L, r.id);
        assertEquals("Test Scene", r.name);
        assertEquals(1L, r.userId);
        assertEquals(1, r.deviceStates.size());
        assertEquals(100L, r.deviceStates.get(0).id);
        assertEquals(10L, r.deviceStates.get(0).deviceId);
        assertEquals("Dimmer", r.deviceStates.get(0).deviceName);
        assertNull(r.deviceStates.get(0).targetSwitchedOn);
        assertEquals(50.0, r.deviceStates.get(0).targetLevel);
    }

    @Test
    void toResponse_emptyDeviceStates() {
        User user = new User();
        user.id = 1L;
        Scene scene = new Scene();
        scene.id = 6L;
        scene.name = "Empty";
        scene.user = user;

        SceneResponse r = SceneMapper.toResponse(scene);
        assertTrue(r.deviceStates.isEmpty());
    }
}
