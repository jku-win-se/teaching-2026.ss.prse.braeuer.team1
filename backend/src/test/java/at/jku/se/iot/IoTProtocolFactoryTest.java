package at.jku.se.iot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/** Unit tests for {@link IoTProtocolFactory}. */
class IoTProtocolFactoryTest {

    private static final String CUSTOM_BROKER_URL = "tcp://broker.example.com:1883";

    private IoTProtocolFactory factory;

    @BeforeEach
    void setUp() {
        factory = new IoTProtocolFactory();
        factory.mqttBrokerUrl = CUSTOM_BROKER_URL;
    }

    @Test
    void create_defaultsToMockAdapter() {
        factory.protocol = "mock";

        IoTProtocol protocol = factory.create();

        assertInstanceOf(MockProtocolAdapter.class, protocol);
        assertEquals("MOCK", protocol.getProtocolName());
    }

    @Test
    void create_unknownProtocol_fallsBackToMock() {
        factory.protocol = "unknown-protocol";

        IoTProtocol protocol = factory.create();

        assertInstanceOf(MockProtocolAdapter.class, protocol);
    }

    @Test
    void create_mqttProtocol_returnsMqttAdapter() {
        factory.protocol = "mqtt";

        IoTProtocol protocol = factory.create();

        assertInstanceOf(MqttProtocolAdapter.class, protocol);
        assertEquals("MQTT", protocol.getProtocolName());
        assertEquals(CUSTOM_BROKER_URL, ((MqttProtocolAdapter) protocol).getBrokerUrl());
    }

    @Test
    void create_caseInsensitiveProtocolName() {
        factory.protocol = "MQTT";

        IoTProtocol protocol = factory.create();

        assertInstanceOf(MqttProtocolAdapter.class, protocol);
    }

    @Test
    void create_returnsNewInstanceEachCall() {
        factory.protocol = "mock";

        IoTProtocol p1 = factory.create();
        IoTProtocol p2 = factory.create();

        // Distinct instances are required so each consumer gets its own state.
        org.junit.jupiter.api.Assertions.assertNotSame(p1, p2);
    }
}
