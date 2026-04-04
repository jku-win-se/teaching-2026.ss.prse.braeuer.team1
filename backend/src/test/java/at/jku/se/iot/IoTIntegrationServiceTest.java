package at.jku.se.iot;

import at.jku.se.entity.Device;
import at.jku.se.entity.enums.DeviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IoTIntegrationServiceTest {

    private MockProtocolAdapter mockAdapter;
    private IoTIntegrationService service;

    @BeforeEach
    void setUp() {
        mockAdapter = new MockProtocolAdapter();
        mockAdapter.connect();
        service = new IoTIntegrationService();
        service.setProtocol(mockAdapter);
    }

    @Test
    void testPushSwitchOnCommand() {
        Device device = createDevice(1L, DeviceType.SWITCH);
        device.switchedOn = true;

        service.pushStateToHardware(device);

        assertEquals(1, mockAdapter.getSentCommands().size());
        assertEquals("SWITCH_ON", mockAdapter.getSentCommands().get(0).action());
    }

    @Test
    void testPushSwitchOffCommand() {
        Device device = createDevice(1L, DeviceType.SWITCH);
        device.switchedOn = false;

        service.pushStateToHardware(device);

        assertEquals(1, mockAdapter.getSentCommands().size());
        assertEquals("SWITCH_OFF", mockAdapter.getSentCommands().get(0).action());
    }

    @Test
    void testPushDimmerLevel() {
        Device device = createDevice(2L, DeviceType.DIMMER);
        device.level = 75.0;

        service.pushStateToHardware(device);

        assertEquals(1, mockAdapter.getSentCommands().size());
        assertEquals("SET_LEVEL", mockAdapter.getSentCommands().get(0).action());
        assertEquals(75.0, mockAdapter.getSentCommands().get(0).parameters().get("level"));
    }

    @Test
    void testPushThermostatLevel() {
        Device device = createDevice(3L, DeviceType.THERMOSTAT);
        device.level = 21.5;

        service.pushStateToHardware(device);

        assertEquals(1, mockAdapter.getSentCommands().size());
        assertEquals("SET_LEVEL", mockAdapter.getSentCommands().get(0).action());
    }

    @Test
    void testPushBlindLevel() {
        Device device = createDevice(4L, DeviceType.BLIND);
        device.level = 50.0;

        service.pushStateToHardware(device);

        assertEquals(1, mockAdapter.getSentCommands().size());
        assertEquals("SET_LEVEL", mockAdapter.getSentCommands().get(0).action());
    }

    @Test
    void testNoPushWhenDisconnected() {
        mockAdapter.disconnect();
        Device device = createDevice(1L, DeviceType.SWITCH);
        device.switchedOn = true;

        service.pushStateToHardware(device);

        assertEquals(0, mockAdapter.getSentCommands().size());
    }

    @Test
    void testSubscribeAndUnsubscribe() {
        service.subscribeToDevice(1L);
        service.unsubscribeFromDevice(1L);
        // no exception = success
    }

    @Test
    void testIsConnected() {
        assertTrue(service.isConnected());
        mockAdapter.disconnect();
        assertFalse(service.isConnected());
    }

    @Test
    void testGetProtocolName() {
        assertEquals("MOCK", service.getProtocolName());
    }

    @Test
    void testSensorWithNullLevel() {
        Device device = createDevice(5L, DeviceType.SENSOR);
        device.level = null;

        service.pushStateToHardware(device);

        assertEquals(0, mockAdapter.getSentCommands().size());
    }

    private Device createDevice(Long id, DeviceType type) {
        Device device = new Device();
        device.id = id;
        device.type = type;
        device.name = "Test Device";
        device.updatedAt = LocalDateTime.now();
        return device;
    }
}
