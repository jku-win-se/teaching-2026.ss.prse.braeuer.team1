package at.jku.se.resource;

import at.jku.se.dto.request.DeviceRenameRequest;
import at.jku.se.dto.request.DeviceStateRequest;
import at.jku.se.entity.ActivityLog;
import at.jku.se.entity.Device;
import at.jku.se.iot.IoTIntegrationService;
import at.jku.se.mapper.DeviceMapper;
import at.jku.se.repository.ActivityLogRepository;
import at.jku.se.repository.DeviceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST resource for device management (FR-04, FR-05, FR-06, FR-07).
 * Devices are scoped under rooms for creation and listing.
 */
@ApplicationScoped
@Path("/api/devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceResource {

    @Inject
    DeviceRepository deviceRepo;

    @Inject
    ActivityLogRepository activityLogRepo;

    @Inject
    IoTIntegrationService iotService;

    /**
     * Returns a single device including its current state (FR-07).
     *
     * @param id the device ID
     * @return the device or 404
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Device device = deviceRepo.findById(id);
        if (device == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Device not found")).build();
        }
        return Response.ok(DeviceMapper.toResponse(device)).build();
    }

    /**
     * Renames an existing device (FR-05).
     *
     * @param id      the device ID
     * @param request the new name
     * @return 200 with the updated device
     */
    @PUT
    @Path("/{id}/rename")
    @Transactional
    public Response rename(@PathParam("id") Long id, @Valid DeviceRenameRequest request) {
        Device device = deviceRepo.findById(id);
        if (device == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Device not found")).build();
        }
        device.name = request.name;
        device.updatedAt = LocalDateTime.now();
        return Response.ok(DeviceMapper.toResponse(device)).build();
    }

    /**
     * Removes a device from a room (FR-05).
     * Related activity logs and energy logs are removed via DB cascade.
     *
     * @param id the device ID
     * @return 204 on success
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Device device = deviceRepo.findById(id);
        if (device == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Device not found")).build();
        }
        deviceRepo.delete(device);
        return Response.noContent().build();
    }

    /**
     * Manually controls a device's state and records an activity log entry (FR-06,
     * FR-08).
     * Supported state changes depend on the device type:
     * <ul>
     * <li>SWITCH: set {@code switchedOn}</li>
     * <li>DIMMER: set {@code level} (0–100 %)</li>
     * <li>THERMOSTAT: set {@code level} (target °C)</li>
     * <li>SENSOR: set {@code level} (sensor reading)</li>
     * <li>BLIND: set {@code level} (0 = closed, 100 = open)</li>
     * </ul>
     *
     * @param id      the device ID
     * @param request the new state and the actor
     * @return 200 with the updated device
     */
    @PUT
    @Path("/{id}/state")
    @Transactional
    public Response updateState(@PathParam("id") Long id, @Valid DeviceStateRequest request) {
        Device device = deviceRepo.findById(id);
        if (device == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Device not found")).build();
        }

        StringBuilder description = new StringBuilder("State updated:");
        if (request.switchedOn != null) {
            device.switchedOn = request.switchedOn;
            description.append(" switchedOn=").append(request.switchedOn);
        }
        if (request.level != null) {
            device.level = request.level;
            description.append(" level=").append(request.level);
        }
        device.updatedAt = LocalDateTime.now();

        ActivityLog log = new ActivityLog();
        log.device = device;
        log.actor = request.actor;
        log.description = description.toString();
        log.timestamp = device.updatedAt;
        activityLogRepo.persist(log);

        iotService.pushStateToHardware(device);

        return Response.ok(DeviceMapper.toResponse(device)).build();
    }
}
