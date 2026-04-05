package at.jku.se.websocket;

import at.jku.se.dto.response.DeviceResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.websockets.next.OpenConnections;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Broadcasts device state changes to all connected WebSocket clients (FR-07).
 */
@ApplicationScoped
public class DeviceEventBroadcaster {

    private static final Logger LOG = Logger.getLogger(DeviceEventBroadcaster.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Inject
    OpenConnections connections;

    /**
     * Sends a device state update to all connected WebSocket clients.
     */
    public void broadcastDeviceUpdate(DeviceResponse device) {
        try {
            String json = MAPPER.writeValueAsString(new DeviceStateMessage("DEVICE_STATE_CHANGED", device));
            connections.forEach(c -> c.sendText(json).subscribe().asCompletionStage());
        } catch (JsonProcessingException e) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "Failed to serialize device update", e);
            }
        }
    }

    /**
     * Message wrapper sent to WebSocket clients.
     */
    public record DeviceStateMessage(String type, DeviceResponse device) {
    }
}
