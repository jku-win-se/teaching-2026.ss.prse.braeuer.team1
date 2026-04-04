package at.jku.se.iot;

import java.util.function.Consumer;

/**
 * Abstraction for IoT communication protocols (MQTT, CoAP, Zigbee, etc.).
 * Implementations bridge the SmartHome system to physical hardware.
 */
public interface IoTProtocol {

    void connect() throws IoTException;

    void disconnect();

    boolean isConnected();

    void sendCommand(DeviceCommand command) throws IoTException;

    void subscribe(String topic, Consumer<DeviceEvent> listener) throws IoTException;

    void unsubscribe(String topic) throws IoTException;

    String getProtocolName();
}
