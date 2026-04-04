import { useEffect, useRef } from "react";

interface DeviceStateMessage {
  type: string;
  device: {
    id: number;
    name: string;
    type: string;
    roomId: number;
    roomName: string;
    switchedOn?: boolean;
    level?: number;
    powerConsumptionWatt?: number;
    updatedAt: string;
  };
}

/**
 * Hook that connects to the backend WebSocket and calls onDeviceUpdate
 * whenever a device state changes in real-time (FR-07).
 */
export function useDeviceWebSocket(onDeviceUpdate: (device: DeviceStateMessage["device"]) => void) {
  const callbackRef = useRef(onDeviceUpdate);

  useEffect(() => {
    callbackRef.current = onDeviceUpdate;
  }, [onDeviceUpdate]);

  useEffect(() => {
    let ws: WebSocket | null = null;
    let closed = false;

    function connect() {
      if (closed) return;
      const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
      const wsUrl = `${protocol}//${window.location.host}/ws/devices`;

      ws = new WebSocket(wsUrl);

      ws.onmessage = (event) => {
        try {
          const message: DeviceStateMessage = JSON.parse(event.data);
          if (message.type === "DEVICE_STATE_CHANGED" && message.device) {
            callbackRef.current(message.device);
          }
        } catch {
          // ignore malformed messages
        }
      };

      ws.onclose = () => {
        if (!closed) {
          setTimeout(connect, 3000);
        }
      };
    }

    connect();

    return () => {
      closed = true;
      ws?.close();
    };
  }, []);
}
