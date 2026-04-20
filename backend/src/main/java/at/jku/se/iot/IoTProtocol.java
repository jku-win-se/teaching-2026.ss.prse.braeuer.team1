package at.jku.se.iot;

import java.util.function.Consumer;

/**
 * Abstraction for IoT communication protocols (MQTT, CoAP, Zigbee, etc.).
 * Implementations bridge the SmartHome system to physical hardware.
 */
public interface IoTProtocol {

    /**
     * Opens the connection to the underlying hardware or broker.
     *
     * @throws IoTException if the connection cannot be established
     */
    void connect() throws IoTException;

    /** Closes the connection and releases any associated resources. */
    void disconnect();

    /**
     * Indicates whether the protocol adapter currently has an open connection.
     *
     * @return {@code true} when connected, {@code false} otherwise
     */
    boolean isConnected();

    /**
     * Sends a command to the target hardware.
     *
     * @param command the command to dispatch
     * @throws IoTException if the command cannot be delivered
     */
    void sendCommand(DeviceCommand command) throws IoTException;

    /**
     * Subscribes to events published on the given topic.
     *
     * @param topic    the topic to subscribe to
     * @param listener callback invoked for each received event
     * @throws IoTException if the subscription cannot be registered
     */
    void subscribe(String topic, Consumer<DeviceEvent> listener) throws IoTException;

    /**
     * Removes a previous subscription.
     *
     * @param topic the topic to unsubscribe from
     * @throws IoTException if unsubscription fails
     */
    void unsubscribe(String topic) throws IoTException;

    /**
     * Returns a short identifier of the protocol implementation (e.g., {@code "MOCK"}, {@code "MQTT"}).
     *
     * @return the protocol name
     */
    String getProtocolName();
}
