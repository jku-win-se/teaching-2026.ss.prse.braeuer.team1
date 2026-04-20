package at.jku.se.iot;

import java.util.Map;

/**
 * Command dispatched to an IoT protocol adapter to change a device's state.
 *
 * @param deviceId   identifier of the target device
 * @param action     symbolic action name (e.g., {@code SWITCH_ON})
 * @param parameters optional action parameters (e.g., {@code level})
 */
public record DeviceCommand(
        Long deviceId,
        String action,
        Map<String, Object> parameters
) {
    /**
     * Creates a command that switches the device on.
     *
     * @param deviceId the target device identifier
     * @return the switch-on command
     */
    public static DeviceCommand switchOn(Long deviceId) {
        return new DeviceCommand(deviceId, "SWITCH_ON", Map.of());
    }

    /**
     * Creates a command that switches the device off.
     *
     * @param deviceId the target device identifier
     * @return the switch-off command
     */
    public static DeviceCommand switchOff(Long deviceId) {
        return new DeviceCommand(deviceId, "SWITCH_OFF", Map.of());
    }

    /**
     * Creates a command that sets the device's analog level.
     *
     * @param deviceId the target device identifier
     * @param level    the requested level (e.g., dimmer percentage or temperature)
     * @return the set-level command
     */
    public static DeviceCommand setLevel(Long deviceId, double level) {
        return new DeviceCommand(deviceId, "SET_LEVEL", Map.of("level", level));
    }
}
