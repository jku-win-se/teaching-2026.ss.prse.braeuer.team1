package at.jku.se.iot;

import at.jku.se.entity.Device;
import at.jku.se.entity.enums.DeviceType;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bridges the SmartHome device layer with physical IoT hardware.
 * Translates device state changes into IoT protocol commands and
 * listens for incoming hardware events.
 */
@ApplicationScoped
public class IoTIntegrationService {

    /** Creates the service; intended for CDI instantiation. */
    public IoTIntegrationService() {}

    private static final Logger LOG = Logger.getLogger(IoTIntegrationService.class.getName());

    @Inject
    IoTProtocolFactory protocolFactory;

    private IoTProtocol protocol;

    @PostConstruct
    void init() {
        protocol = protocolFactory.create();
        try {
            protocol.connect();
        } catch (IoTException e) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "Failed to connect IoT protocol: " + protocol.getProtocolName(), e);
            }
        }
    }

    @PreDestroy
    void shutdown() {
        if (protocol != null) {
            protocol.disconnect();
        }
    }

    /**
     * Sends the current device state to the physical hardware via the configured IoT protocol.
     *
     * @param device the device whose state should be pushed
     */
    public void pushStateToHardware(Device device) {
        if (protocol == null || !protocol.isConnected()) {
            return;
        }
        try {
            DeviceCommand command = buildCommand(device);
            if (command != null) {
                protocol.sendCommand(command);
            }
        } catch (IoTException e) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "Failed to push state for device " + device.id, e);
            }
        }
    }

    /**
     * Subscribes to hardware events for a specific device.
     *
     * @param deviceId the identifier of the device to subscribe to
     */
    public void subscribeToDevice(Long deviceId) {
        if (protocol == null || !protocol.isConnected()) {
            return;
        }
        try {
            protocol.subscribe(String.valueOf(deviceId), event -> {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Received hardware event for device " + deviceId + ": " + event.eventType());
                }
            });
        } catch (IoTException e) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "Failed to subscribe to device " + deviceId, e);
            }
        }
    }

    /**
     * Unsubscribes from hardware events for a specific device.
     *
     * @param deviceId the identifier of the device to unsubscribe from
     */
    public void unsubscribeFromDevice(Long deviceId) {
        if (protocol == null || !protocol.isConnected()) {
            return;
        }
        try {
            protocol.unsubscribe(String.valueOf(deviceId));
        } catch (IoTException e) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "Failed to unsubscribe from device " + deviceId, e);
            }
        }
    }

    /**
     * Indicates whether the underlying protocol adapter is connected.
     *
     * @return {@code true} if connected, {@code false} otherwise
     */
    public boolean isConnected() {
        return protocol != null && protocol.isConnected();
    }

    /**
     * Returns the identifier of the active protocol (e.g., {@code "MOCK"}, {@code "MQTT"}).
     *
     * @return the protocol name, or {@code "NONE"} if no protocol is active
     */
    public String getProtocolName() {
        return protocol != null ? protocol.getProtocolName() : "NONE";
    }

    void setProtocol(IoTProtocol protocol) {
        this.protocol = protocol;
    }

    private DeviceCommand buildCommand(Device device) {
        if (device.type == DeviceType.SWITCH) {
            return Boolean.TRUE.equals(device.switchedOn)
                    ? DeviceCommand.switchOn(device.id)
                    : DeviceCommand.switchOff(device.id);
        }
        if (device.level != null) {
            return DeviceCommand.setLevel(device.id, device.level);
        }
        return null;
    }
}
