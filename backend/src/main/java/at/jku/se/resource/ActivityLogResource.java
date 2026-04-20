package at.jku.se.resource;

import at.jku.se.dto.response.ActivityLogResponse;
import at.jku.se.entity.ActivityLog;
import at.jku.se.entity.Device;
import at.jku.se.mapper.ActivityLogMapper;
import at.jku.se.repository.ActivityLogRepository;
import at.jku.se.repository.DeviceRepository;
import at.jku.se.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * REST resource for reading activity logs and CSV export (FR-08, FR-16).
 * Activity log entries are created automatically by the DeviceResource on state changes.
 */
@ApplicationScoped
@Path("/api/activity-logs")
@Produces(MediaType.APPLICATION_JSON)
public class ActivityLogResource {

    /** Creates the resource; intended for CDI instantiation. */
    public ActivityLogResource() {}

    @Inject
    ActivityLogRepository activityLogRepo;

    @Inject
    DeviceRepository deviceRepo;

    @Inject
    UserRepository userRepo;

    /**
     * Returns activity log entries.
     * Provide {@code deviceId} to filter by a specific device,
     * or {@code userId} to retrieve all logs for a user's devices.
     *
     * @param deviceId optional filter by device ID
     * @param userId   optional filter by user ID (retrieves logs for all their devices)
     * @return list of log entries
     */
    @GET
    public Response getLogs(@QueryParam("deviceId") Long deviceId,
                             @QueryParam("userId") Long userId) {
        List<ActivityLog> logs;
        if (deviceId != null) {
            Device device = deviceRepo.findById(deviceId);
            if (device == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Device not found")).build();
            }
            logs = activityLogRepo.findByDevice(device);
        } else if (userId != null) {
            if (userRepo.findById(userId) == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found")).build();
            }
            logs = activityLogRepo.findByUserId(userId);
        } else {
            logs = activityLogRepo.listAll();
        }
        List<ActivityLogResponse> result = logs.stream()
                .map(ActivityLogMapper::toResponse)
                .toList();
        return Response.ok(result).build();
    }

    /**
     * Exports the activity log as a CSV file (FR-16).
     * Use {@code userId} to export all logs for a user's devices.
     *
     * @param userId the user whose logs to export
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
        if (userRepo.findById(userId) == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User not found").build();
        }
        List<ActivityLog> logs = activityLogRepo.findByUserId(userId);
        StringBuilder csv = new StringBuilder();
        csv.append("id,timestamp,device,room,actor,description\n");
        for (ActivityLog log : logs) {
            csv.append(log.id).append(',')
                    .append(log.timestamp).append(',')
                    .append(escapeCsv(log.device.name)).append(',')
                    .append(escapeCsv(log.device.room.name)).append(',')
                    .append(escapeCsv(log.actor)).append(',')
                    .append(escapeCsv(log.description)).append('\n');
        }
        return Response.ok(csv.toString())
                .header("Content-Disposition", "attachment; filename=\"activity-log.csv\"")
                .build();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
