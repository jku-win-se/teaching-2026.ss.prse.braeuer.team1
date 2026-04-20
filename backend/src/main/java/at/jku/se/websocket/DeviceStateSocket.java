package at.jku.se.websocket;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * WebSocket endpoint for real-time device state updates (FR-07).
 * Clients connect to /ws/devices and receive JSON messages
 * whenever a device state changes.
 */
@WebSocket(path = "/ws/devices")
public class DeviceStateSocket {

    /** Creates the socket endpoint; intended for CDI instantiation. */
    public DeviceStateSocket() {}

    private static final Logger LOG = Logger.getLogger(DeviceStateSocket.class.getName());

    @Inject
    WebSocketConnection connection;

    /** Callback invoked when a new client connects to the socket. */
    @OnOpen
    public void onOpen() {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("WebSocket client connected: " + connection.id());
        }
    }

    /** Callback invoked when a client disconnects from the socket. */
    @OnClose
    public void onClose() {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("WebSocket client disconnected: " + connection.id());
        }
    }
}
