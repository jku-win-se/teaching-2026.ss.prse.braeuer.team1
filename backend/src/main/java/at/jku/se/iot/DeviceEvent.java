package at.jku.se.iot;

import java.time.LocalDateTime;
import java.util.Map;

public record DeviceEvent(
        String source,
        String eventType,
        Map<String, Object> payload,
        LocalDateTime timestamp
) {
    public static DeviceEvent stateChange(String source, Map<String, Object> payload) {
        return new DeviceEvent(source, "STATE_CHANGE", payload, LocalDateTime.now());
    }
}
