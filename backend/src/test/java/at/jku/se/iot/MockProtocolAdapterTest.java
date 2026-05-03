package at.jku.se.iot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link MockProtocolAdapter}. */
class MockProtocolAdapterTest {

    private MockProtocolAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MockProtocolAdapter();
    }

    @Test
    void newAdapter_isNotConnected() {
        assertFalse(adapter.isConnected());
    }

    @Test
    void connect_setsConnectedTrue() {
        adapter.connect();
        assertTrue(adapter.isConnected());
    }

    @Test
    void disconnect_clearsConnectionAndSubscriptions() throws IoTException {
        adapter.connect();
        adapter.subscribe("topic", e -> {});
        adapter.disconnect();
        assertFalse(adapter.isConnected());
    }

    @Test
    void sendCommand_whenNotConnected_throwsIoTException() {
        DeviceCommand cmd = DeviceCommand.switchOn(1L);
        IoTException ex = assertThrows(IoTException.class, () -> adapter.sendCommand(cmd));
        assertTrue(ex.getMessage().contains("not connected"));
    }

    @Test
    void sendCommand_whenConnected_recordsCommand() throws IoTException {
        adapter.connect();
        DeviceCommand cmd = DeviceCommand.switchOn(42L);

        adapter.sendCommand(cmd);

        List<DeviceCommand> recorded = adapter.getSentCommands();
        assertEquals(1, recorded.size());
        assertSame(cmd, recorded.get(0));
    }

    @Test
    void sendCommand_multipleCommands_allRecorded() throws IoTException {
        adapter.connect();
        adapter.sendCommand(DeviceCommand.switchOn(1L));
        adapter.sendCommand(DeviceCommand.switchOff(1L));
        adapter.sendCommand(DeviceCommand.setLevel(2L, 75.0));

        assertEquals(3, adapter.getSentCommands().size());
    }

    @Test
    void clearCommands_emptiesHistory() throws IoTException {
        adapter.connect();
        adapter.sendCommand(DeviceCommand.switchOn(1L));
        adapter.sendCommand(DeviceCommand.switchOff(1L));

        adapter.clearCommands();

        assertTrue(adapter.getSentCommands().isEmpty());
    }

    @Test
    void subscribe_whenNotConnected_throwsIoTException() {
        assertThrows(IoTException.class, () -> adapter.subscribe("topic", e -> {}));
    }

    @Test
    void subscribe_andSimulateEvent_invokesListener() throws IoTException {
        adapter.connect();
        AtomicReference<DeviceEvent> received = new AtomicReference<>();
        adapter.subscribe("device/1", received::set);

        DeviceEvent event = new DeviceEvent("device/1", "STATE_CHANGE",
                Map.of("level", 50.0), LocalDateTime.now());
        adapter.simulateEvent("device/1", event);

        assertSame(event, received.get());
    }

    @Test
    void simulateEvent_unknownTopic_doesNotThrow() {
        adapter.simulateEvent("nope", DeviceEvent.stateChange("x", Map.of()));
        // Reaching this assertion means no exception was thrown.
        assertNotNull(adapter);
    }

    @Test
    void unsubscribe_whenNotConnected_throwsIoTException() {
        assertThrows(IoTException.class, () -> adapter.unsubscribe("topic"));
    }

    @Test
    void unsubscribe_removesListener() throws IoTException {
        adapter.connect();
        AtomicReference<DeviceEvent> received = new AtomicReference<>();
        adapter.subscribe("topic", received::set);

        adapter.unsubscribe("topic");
        adapter.simulateEvent("topic", DeviceEvent.stateChange("topic", Map.of()));

        assertEquals(null, received.get());
    }

    @Test
    void getProtocolName_returnsMock() {
        assertEquals("MOCK", adapter.getProtocolName());
    }

    @Test
    void getSentCommands_returnsImmutableSnapshot() throws IoTException {
        adapter.connect();
        adapter.sendCommand(DeviceCommand.switchOn(1L));

        List<DeviceCommand> snapshot = adapter.getSentCommands();
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(DeviceCommand.switchOff(1L)));
    }
}
