package at.jku.se.iot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class IoTProtocolTest {

    private MockProtocolAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MockProtocolAdapter();
    }

    @Test
    void testConnectDisconnect() {
        assertFalse(adapter.isConnected());
        adapter.connect();
        assertTrue(adapter.isConnected());
        adapter.disconnect();
        assertFalse(adapter.isConnected());
    }

    @Test
    void testSendCommand() throws IoTException {
        adapter.connect();

        DeviceCommand cmd = DeviceCommand.switchOn(1L);
        adapter.sendCommand(cmd);

        assertEquals(1, adapter.getSentCommands().size());
        assertEquals("SWITCH_ON", adapter.getSentCommands().get(0).action());
        assertEquals(1L, adapter.getSentCommands().get(0).deviceId());
    }

    @Test
    void testSendCommandNotConnected() {
        assertThrows(IoTException.class, () ->
                adapter.sendCommand(DeviceCommand.switchOn(1L)));
    }

    @Test
    void testSetLevelCommand() throws IoTException {
        adapter.connect();

        DeviceCommand cmd = DeviceCommand.setLevel(2L, 75.0);
        adapter.sendCommand(cmd);

        assertEquals("SET_LEVEL", adapter.getSentCommands().get(0).action());
        assertEquals(75.0, adapter.getSentCommands().get(0).parameters().get("level"));
    }

    @Test
    void testSubscribeAndReceiveEvent() throws IoTException {
        adapter.connect();

        AtomicReference<DeviceEvent> received = new AtomicReference<>();
        adapter.subscribe("device/1", received::set);

        DeviceEvent event = DeviceEvent.stateChange("sensor", Map.of("temperature", 22.5));
        adapter.simulateEvent("device/1", event);

        assertNotNull(received.get());
        assertEquals("STATE_CHANGE", received.get().eventType());
        assertEquals(22.5, received.get().payload().get("temperature"));
    }

    @Test
    void testUnsubscribe() throws IoTException {
        adapter.connect();

        AtomicReference<DeviceEvent> received = new AtomicReference<>();
        adapter.subscribe("device/1", received::set);
        adapter.unsubscribe("device/1");

        adapter.simulateEvent("device/1",
                DeviceEvent.stateChange("sensor", Map.of("value", 1)));

        assertNull(received.get());
    }

    @Test
    void testClearCommands() throws IoTException {
        adapter.connect();
        adapter.sendCommand(DeviceCommand.switchOn(1L));
        adapter.sendCommand(DeviceCommand.switchOff(1L));
        assertEquals(2, adapter.getSentCommands().size());

        adapter.clearCommands();
        assertEquals(0, adapter.getSentCommands().size());
    }

    @Test
    void testProtocolName() {
        assertEquals("MOCK", adapter.getProtocolName());

        MqttProtocolAdapter mqtt = new MqttProtocolAdapter("tcp://localhost:1883");
        assertEquals("MQTT", mqtt.getProtocolName());
    }

    @Test
    void testDeviceCommandFactoryMethods() {
        DeviceCommand on = DeviceCommand.switchOn(5L);
        assertEquals(5L, on.deviceId());
        assertEquals("SWITCH_ON", on.action());
        assertTrue(on.parameters().isEmpty());

        DeviceCommand off = DeviceCommand.switchOff(5L);
        assertEquals("SWITCH_OFF", off.action());

        DeviceCommand level = DeviceCommand.setLevel(5L, 50.0);
        assertEquals("SET_LEVEL", level.action());
        assertEquals(50.0, level.parameters().get("level"));
    }

    @Test
    void testDeviceEventFactoryMethod() {
        DeviceEvent event = DeviceEvent.stateChange("mqtt", Map.of("on", true));
        assertEquals("STATE_CHANGE", event.eventType());
        assertEquals("mqtt", event.source());
        assertNotNull(event.timestamp());
    }
}
