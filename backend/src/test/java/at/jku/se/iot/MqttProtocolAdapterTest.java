package at.jku.se.iot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link MqttProtocolAdapter}. */
class MqttProtocolAdapterTest {

    private static final String BROKER_URL = "tcp://localhost:1883";

    private MqttProtocolAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MqttProtocolAdapter(BROKER_URL);
    }

    @Test
    void newAdapter_isNotConnected() {
        assertFalse(adapter.isConnected());
    }

    @Test
    void connect_setsConnectedTrue() throws IoTException {
        adapter.connect();
        assertTrue(adapter.isConnected());
    }

    @Test
    void disconnect_clearsConnection() throws IoTException {
        adapter.connect();
        adapter.disconnect();
        assertFalse(adapter.isConnected());
    }

    @Test
    void sendCommand_whenNotConnected_throwsIoTException() {
        DeviceCommand cmd = DeviceCommand.switchOn(1L);
        IoTException ex = assertThrows(IoTException.class, () -> adapter.sendCommand(cmd));
        assertTrue(ex.getMessage().contains("Not connected"));
    }

    @Test
    void sendCommand_whenConnected_doesNotThrow() throws IoTException {
        adapter.connect();
        adapter.sendCommand(DeviceCommand.switchOn(1L));
        // No assertion required: reaching this point means no exception was thrown.
    }

    @Test
    void subscribe_whenNotConnected_throwsIoTException() {
        assertThrows(IoTException.class, () -> adapter.subscribe("1", e -> {}));
    }

    @Test
    void unsubscribe_whenNotConnected_throwsIoTException() {
        assertThrows(IoTException.class, () -> adapter.unsubscribe("1"));
    }

    @Test
    void subscribe_usesSmartHomeTopicPrefix() throws IoTException {
        adapter.connect();
        AtomicReference<DeviceEvent> received = new AtomicReference<>();
        adapter.subscribe("42", received::set);

        DeviceEvent event = DeviceEvent.stateChange("42", Map.of("level", 50.0));
        // The full topic constructed by subscribe must include the prefix and "/state".
        adapter.simulateMessage("smarthome/devices/42/state", event);

        assertSame(event, received.get());
    }

    @Test
    void simulateMessage_unknownTopic_doesNotInvokeAnyListener() throws IoTException {
        adapter.connect();
        AtomicReference<DeviceEvent> received = new AtomicReference<>();
        adapter.subscribe("42", received::set);

        adapter.simulateMessage("smarthome/devices/99/state", DeviceEvent.stateChange("99", Map.of()));

        assertNull(received.get());
    }

    @Test
    void unsubscribe_removesListener() throws IoTException {
        adapter.connect();
        AtomicReference<DeviceEvent> received = new AtomicReference<>();
        adapter.subscribe("42", received::set);
        adapter.unsubscribe("42");

        adapter.simulateMessage("smarthome/devices/42/state",
                DeviceEvent.stateChange("42", Map.of()));

        assertNull(received.get());
    }

    @Test
    void getProtocolName_returnsMqtt() {
        assertEquals("MQTT", adapter.getProtocolName());
    }

    @Test
    void getBrokerUrl_returnsConfiguredUrl() {
        assertEquals(BROKER_URL, adapter.getBrokerUrl());
    }
}
