package at.jku.se.dto.response;

import java.util.List;

/**
 * Aggregated energy consumption dashboard for a user (FR-14).
 * Shows consumption per device, per room, and as household total — for today and this week.
 */
public class EnergyDashboardResponse {

    public List<DeviceEnergySummary> byDevice;
    public List<RoomEnergySummary> byRoom;
    public double totalTodayWh;
    public double totalWeekWh;

    /** Per-device energy summary. */
    public static class DeviceEnergySummary {
        public Long deviceId;
        public String deviceName;
        public String roomName;
        public double todayWh;
        public double weekWh;
    }

    /** Per-room aggregated energy summary. */
    public static class RoomEnergySummary {
        public Long roomId;
        public String roomName;
        public double todayWh;
        public double weekWh;
    }
}
