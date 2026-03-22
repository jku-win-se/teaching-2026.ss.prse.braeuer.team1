package at.jku.se.resource;

import at.jku.se.dto.request.EnergyLogRequest;
import at.jku.se.dto.response.EnergyDashboardResponse;
import at.jku.se.dto.response.EnergyLogResponse;
import at.jku.se.entity.Device;
import at.jku.se.entity.EnergyLog;
import at.jku.se.entity.Room;
import at.jku.se.entity.User;
import at.jku.se.mapper.EnergyLogMapper;
import at.jku.se.repository.DeviceRepository;
import at.jku.se.repository.EnergyLogRepository;
import at.jku.se.repository.RoomRepository;
import at.jku.se.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST resource for energy consumption tracking and the dashboard (FR-14, FR-16).
 * Energy logs can be recorded manually via POST for testing.
 * The dashboard aggregates consumption per device, per room, and as household total.
 */
@ApplicationScoped
@Path("/api/energy")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnergyResource {

    @Inject
    EnergyLogRepository energyLogRepo;

    @Inject
    UserRepository userRepo;

    @Inject
    RoomRepository roomRepo;

    @Inject
    DeviceRepository deviceRepo;

    /**
     * Returns the energy consumption dashboard for a user (FR-14).
     * Aggregates today's and this week's (Mon–Sun) Wh per device, per room, and total.
     *
     * @param userId the user ID
     * @return the dashboard data
     */
    @GET
    @Path("/dashboard")
    public Response getDashboard(@QueryParam("userId") Long userId) {
        if (userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "userId query parameter is required")).build();
        }
        User user = userRepo.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "User not found")).build();
        }

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now()
                .with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        List<EnergyLog> todayLogs = energyLogRepo.findByUserIdAndPeriod(userId, todayStart, now);
        List<EnergyLog> weekLogs = energyLogRepo.findByUserIdAndPeriod(userId, weekStart, now);

        // Index by device ID
        Map<Long, Double> deviceTodayWh = sumByDevice(todayLogs);
        Map<Long, Double> deviceWeekWh = sumByDevice(weekLogs);

        List<Room> rooms = roomRepo.findByUser(user);
        List<EnergyDashboardResponse.DeviceEnergySummary> byDevice = new ArrayList<>();
        List<EnergyDashboardResponse.RoomEnergySummary> byRoom = new ArrayList<>();

        for (Room room : rooms) {
            double roomTodayWh = 0;
            double roomWeekWh = 0;
            List<Device> devices = deviceRepo.findByRoom(room);
            for (Device device : devices) {
                EnergyDashboardResponse.DeviceEnergySummary ds =
                        new EnergyDashboardResponse.DeviceEnergySummary();
                ds.deviceId = device.id;
                ds.deviceName = device.name;
                ds.roomName = room.name;
                ds.todayWh = deviceTodayWh.getOrDefault(device.id, 0.0);
                ds.weekWh = deviceWeekWh.getOrDefault(device.id, 0.0);
                byDevice.add(ds);
                roomTodayWh += ds.todayWh;
                roomWeekWh += ds.weekWh;
            }
            EnergyDashboardResponse.RoomEnergySummary rs =
                    new EnergyDashboardResponse.RoomEnergySummary();
            rs.roomId = room.id;
            rs.roomName = room.name;
            rs.todayWh = roomTodayWh;
            rs.weekWh = roomWeekWh;
            byRoom.add(rs);
        }

        EnergyDashboardResponse dashboard = new EnergyDashboardResponse();
        dashboard.byDevice = byDevice;
        dashboard.byRoom = byRoom;
        dashboard.totalTodayWh = byRoom.stream().mapToDouble(r -> r.todayWh).sum();
        dashboard.totalWeekWh = byRoom.stream().mapToDouble(r -> r.weekWh).sum();

        return Response.ok(dashboard).build();
    }

    /**
     * Exports the energy consumption summary as a CSV file (FR-16).
     *
     * @param userId the user ID
     * @return CSV file download
     */
    @GET
    @Path("/export")
    @Produces("text/csv")
    public Response exportCsv(@QueryParam("userId") Long userId) {
        if (userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("userId query parameter is required").build();
        }
        User user = userRepo.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User not found").build();
        }
        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        List<EnergyLog> logs = energyLogRepo.findByUserIdAndPeriod(userId, weekStart, now);
        StringBuilder csv = new StringBuilder();
        csv.append("id,timestamp,deviceId,device,room,consumptionWh\n");
        for (EnergyLog log : logs) {
            csv.append(log.id).append(',')
                    .append(log.timestamp).append(',')
                    .append(log.device.id).append(',')
                    .append(log.device.name).append(',')
                    .append(log.device.room.name).append(',')
                    .append(log.consumptionWh).append('\n');
        }
        return Response.ok(csv.toString())
                .header("Content-Disposition", "attachment; filename=\"energy-summary.csv\"")
                .build();
    }

    /**
     * Manually records an energy log entry for a device (for testing / FR-14).
     *
     * @param request the energy log data
     * @return 201 with the created entry
     */
    @POST
    @Path("/logs")
    @Transactional
    public Response addLog(@Valid EnergyLogRequest request) {
        Device device = deviceRepo.findById(request.deviceId);
        if (device == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Device not found")).build();
        }
        EnergyLog log = new EnergyLog();
        log.device = device;
        log.timestamp = request.timestamp;
        log.consumptionWh = request.consumptionWh;
        energyLogRepo.persist(log);
        EnergyLogResponse response = EnergyLogMapper.toResponse(log);
        return Response.created(URI.create("/api/energy/logs/" + log.id))
                .entity(response).build();
    }

    private Map<Long, Double> sumByDevice(List<EnergyLog> logs) {
        Map<Long, Double> result = new HashMap<>();
        for (EnergyLog log : logs) {
            result.merge(log.device.id, log.consumptionWh, Double::sum);
        }
        return result;
    }
}
