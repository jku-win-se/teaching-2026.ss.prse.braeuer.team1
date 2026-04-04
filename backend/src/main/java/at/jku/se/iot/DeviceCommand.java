package at.jku.se.iot;

import java.util.Map;

public record DeviceCommand(
        Long deviceId,
        String action,
        Map<String, Object> parameters
) {
    public static DeviceCommand switchOn(Long deviceId) {
        return new DeviceCommand(deviceId, "SWITCH_ON", Map.of());
    }

    public static DeviceCommand switchOff(Long deviceId) {
        return new DeviceCommand(deviceId, "SWITCH_OFF", Map.of());
    }

    public static DeviceCommand setLevel(Long deviceId, double level) {
        return new DeviceCommand(deviceId, "SET_LEVEL", Map.of("level", level));
    }
}
