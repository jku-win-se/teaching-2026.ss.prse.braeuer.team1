package at.jku.se.iot;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory that creates the appropriate IoT protocol adapter based on configuration.
 *
 * Configuration in application.properties:
 *   iot.protocol=mock|mqtt
 *   iot.mqtt.broker-url=tcp://localhost:1883
 */
@ApplicationScoped
public class IoTProtocolFactory {

    private static final Logger LOG = Logger.getLogger(IoTProtocolFactory.class.getName());

    @ConfigProperty(name = "iot.protocol", defaultValue = "mock")
    String protocol;

    @ConfigProperty(name = "iot.mqtt.broker-url", defaultValue = "tcp://localhost:1883")
    String mqttBrokerUrl;

    public IoTProtocol create() {
        return switch (protocol.toLowerCase()) {
            case "mqtt" -> {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Creating MQTT protocol adapter for broker: " + mqttBrokerUrl);
                }
                yield new MqttProtocolAdapter(mqttBrokerUrl);
            }
            default -> {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Creating Mock protocol adapter (development mode)");
                }
                yield new MockProtocolAdapter();
            }
        };
    }
}
