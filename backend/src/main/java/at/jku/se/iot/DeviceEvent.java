package at.jku.se.iot;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Event emitted by an IoT protocol adapter reporting a device-side change.
 *
 * @param source    identifier of the originating device or topic
 * @param eventType symbolic event type (e.g., {@code STATE_CHANGE})
 * @param payload   event-specific data
 * @param timestamp when the event was produced
 */
public record DeviceEvent(
        String source,
        String eventType,
        Map<String, Object> payload,
        LocalDateTime timestamp
) {
    /**
     * Factory for a {@code STATE_CHANGE} event stamped with the current time.
     *
     * @param source  origin identifier
     * @param payload event data
     * @return a state-change device event
     */
    public static DeviceEvent stateChange(String source, Map<String, Object> payload) {
        return new DeviceEvent(source, "STATE_CHANGE", payload, LocalDateTime.now());
    }
}
