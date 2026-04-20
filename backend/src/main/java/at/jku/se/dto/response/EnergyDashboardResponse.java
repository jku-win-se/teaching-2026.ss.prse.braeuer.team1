package at.jku.se.dto.response;

import java.util.List;

/**
 * Aggregated energy consumption dashboard for a user (FR-14).
 * Shows consumption per device, per room, and as household total — for today and this week.
 */
public class EnergyDashboardResponse {

    /** Creates an empty dashboard response; fields are populated by the service layer. */
    public EnergyDashboardResponse() {}

    /** Energy consumption grouped by individual device. */
    public List<DeviceEnergySummary> byDevice;
    /** Energy consumption grouped by room. */
    public List<RoomEnergySummary> byRoom;
    /** Household total consumption today in watt-hours. */
    public double totalTodayWh;
    /** Household total consumption for the current week in watt-hours. */
    public double totalWeekWh;

    /** Per-device energy summary. */
    public static class DeviceEnergySummary {

        /** Creates an empty per-device summary; fields are populated by the service layer. */
        public DeviceEnergySummary() {}

        /** Identifier of the device. */
        public Long deviceId;
        /** Display name of the device. */
        public String deviceName;
        /** Room the device is assigned to. */
        public String roomName;
        /** Today's consumption in watt-hours. */
        public double todayWh;
        /** This week's consumption in watt-hours. */
        public double weekWh;
    }

    /** Per-room aggregated energy summary. */
    public static class RoomEnergySummary {

        /** Creates an empty per-room summary; fields are populated by the service layer. */
        public RoomEnergySummary() {}

        /** Identifier of the room. */
        public Long roomId;
        /** Display name of the room. */
        public String roomName;
        /** Today's consumption in watt-hours. */
        public double todayWh;
        /** This week's consumption in watt-hours. */
        public double weekWh;
    }
}
