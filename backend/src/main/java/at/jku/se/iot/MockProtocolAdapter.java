package at.jku.se.iot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mock IoT protocol adapter for development and testing.
 * Records all sent commands and allows simulating incoming events.
 */
public class MockProtocolAdapter implements IoTProtocol {

    /** Creates a disconnected mock adapter instance. */
    public MockProtocolAdapter() {}

    private static final Logger LOG = Logger.getLogger(MockProtocolAdapter.class.getName());

    private boolean connected = false;
    private final List<DeviceCommand> sentCommands = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Consumer<DeviceEvent>> subscriptions = new ConcurrentHashMap<>();

    @Override
    public void connect() {
        connected = true;
    }

    @Override
    public void disconnect() {
        subscriptions.clear();
        connected = false;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void sendCommand(DeviceCommand command) throws IoTException {
        if (!connected) {
            throw new IoTException("Mock adapter not connected");
        }
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("[Mock IoT] Command sent: " + command.action() + " for device " + command.deviceId());
        }
        sentCommands.add(command);
    }

    @Override
    public void subscribe(String topic, Consumer<DeviceEvent> listener) throws IoTException {
        if (!connected) {
            throw new IoTException("Mock adapter not connected");
        }
        subscriptions.put(topic, listener);
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("[Mock IoT] Subscribed to: " + topic);
        }
    }

    @Override
    public void unsubscribe(String topic) throws IoTException {
        if (!connected) {
            throw new IoTException("Mock adapter not connected");
        }
        subscriptions.remove(topic);
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("[Mock IoT] Unsubscribed from: " + topic);
        }
    }

    @Override
    public String getProtocolName() {
        return "MOCK";
    }

    /**
     * Returns an immutable snapshot of all commands sent through this adapter.
     *
     * @return the recorded commands
     */
    public List<DeviceCommand> getSentCommands() {
        return Collections.unmodifiableList(sentCommands);
    }

    /** Clears the recorded command history. */
    public void clearCommands() {
        sentCommands.clear();
    }

    /**
     * Simulates an incoming event on the given topic for testing.
     *
     * @param topic the topic whose subscriber should receive the event
     * @param event the event to deliver
     */
    public void simulateEvent(String topic, DeviceEvent event) {
        Consumer<DeviceEvent> listener = subscriptions.get(topic);
        if (listener != null) {
            listener.accept(event);
        }
    }
}
