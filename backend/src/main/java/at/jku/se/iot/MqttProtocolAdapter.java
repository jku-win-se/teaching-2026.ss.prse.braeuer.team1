package at.jku.se.iot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MQTT protocol adapter for communicating with physical IoT devices.
 * Requires an external MQTT broker (e.g. Mosquitto) to be running.
 *
 * This adapter defines the MQTT topic structure:
 *   - Commands: smarthome/devices/{deviceId}/command
 *   - State:    smarthome/devices/{deviceId}/state
 */
public class MqttProtocolAdapter implements IoTProtocol {

    private static final Logger LOG = Logger.getLogger(MqttProtocolAdapter.class.getName());
    private static final String TOPIC_PREFIX = "smarthome/devices/";

    private final String brokerUrl;
    private final Map<String, Consumer<DeviceEvent>> subscriptions = new ConcurrentHashMap<>();
    private boolean connected = false;

    /**
     * Creates an MQTT adapter configured for the given broker.
     *
     * @param brokerUrl the MQTT broker URL (e.g., {@code tcp://localhost:1883})
     */
    public MqttProtocolAdapter(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    @Override
    public void connect() throws IoTException {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Connecting to MQTT broker at " + brokerUrl);
        }
        // Actual MQTT client connection would go here (e.g. Eclipse Paho)
        // For now this is the integration point — real implementation requires
        // adding an MQTT client library dependency
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
            throw new IoTException("Not connected to MQTT broker");
        }
        String topic = TOPIC_PREFIX + command.deviceId() + "/command";
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Publishing command to " + topic + ": " + command.action());
        }
        // Actual MQTT publish would go here:
        // mqttClient.publish(topic, serializeCommand(command))
    }

    @Override
    public void subscribe(String topic, Consumer<DeviceEvent> listener) throws IoTException {
        if (!connected) {
            throw new IoTException("Not connected to MQTT broker");
        }
        String fullTopic = TOPIC_PREFIX + topic + "/state";
        subscriptions.put(fullTopic, listener);
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Subscribed to " + fullTopic);
        }
        // Actual MQTT subscribe would go here:
        // mqttClient.subscribe(fullTopic, (t, msg) -> listener.accept(deserializeEvent(msg)))
    }

    @Override
    public void unsubscribe(String topic) throws IoTException {
        if (!connected) {
            throw new IoTException("Not connected to MQTT broker");
        }
        String fullTopic = TOPIC_PREFIX + topic + "/state";
        subscriptions.remove(fullTopic);
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Unsubscribed from " + fullTopic);
        }
    }

    @Override
    public String getProtocolName() {
        return "MQTT";
    }

    /**
     * Returns the broker URL this adapter is configured for.
     *
     * @return the MQTT broker URL
     */
    public String getBrokerUrl() {
        return brokerUrl;
    }

    /**
     * Simulates receiving a message on a topic (useful for testing).
     */
    void simulateMessage(String topic, DeviceEvent event) {
        Consumer<DeviceEvent> listener = subscriptions.get(topic);
        if (listener != null) {
            listener.accept(event);
        }
    }
}
