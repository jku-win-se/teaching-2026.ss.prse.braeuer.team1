package at.jku.se.resource;

import at.jku.se.dto.request.ScheduleRequest;
import at.jku.se.dto.response.ConflictResponse;
import at.jku.se.dto.response.ScheduleResponse;
import at.jku.se.entity.Device;
import at.jku.se.entity.Schedule;
import at.jku.se.entity.User;
import at.jku.se.mapper.ScheduleMapper;
import at.jku.se.repository.DeviceRepository;
import at.jku.se.repository.ScheduleRepository;
import at.jku.se.repository.UserRepository;
import at.jku.se.service.ConflictDetectionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST resource for time-based schedules (FR-09).
 * Includes conflict detection when two schedules target the same device at the same time (FR-15).
 */
@ApplicationScoped
@Path("/api/schedules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScheduleResource {

    @Inject
    ScheduleRepository scheduleRepo;

    @Inject
    DeviceRepository deviceRepo;

    @Inject
    UserRepository userRepo;

    @Inject
    ConflictDetectionService conflictService;

    /**
     * Lists schedules. Optionally filtered by userId.
     *
     * @param userId optional filter by owner user ID
     * @return list of schedules
     */
    @GET
    public Response list(@QueryParam("userId") Long userId) {
        List<Schedule> schedules;
        if (userId != null) {
            User user = userRepo.findById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found")).build();
            }
            schedules = scheduleRepo.findByUser(user);
        } else {
            schedules = scheduleRepo.listAll();
        }
        List<ScheduleResponse> result = schedules.stream()
                .map(ScheduleMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(result).build();
    }

    /**
     * Returns a single schedule by ID.
     *
     * @param id the schedule ID
     * @return the schedule or 404
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Schedule schedule = scheduleRepo.findById(id);
        if (schedule == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Schedule not found")).build();
        }
        return Response.ok(ScheduleMapper.toResponse(schedule)).build();
    }

    /**
     * Creates a new schedule with conflict detection (FR-09, FR-15).
     * Returns 409 if an active schedule already targets the same device with the same cron expression.
     *
     * @param request the schedule details
     * @return 201 with the created schedule, or 409 on conflict
     */
    @POST
    @Transactional
    public Response create(@Valid ScheduleRequest request) {
        User user = userRepo.findById(request.userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "User not found")).build();
        }
        Device device = deviceRepo.findById(request.deviceId);
        if (device == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Device not found")).build();
        }
        Schedule schedule = new Schedule();
        schedule.name = request.name;
        schedule.cronExpression = request.cronExpression;
        schedule.device = device;
        schedule.actionValue = request.actionValue;
        schedule.active = request.active;
        schedule.user = user;
        // FR-15: Conflict detection via ConflictDetectionService
        if (Boolean.TRUE.equals(request.active)) {
            List<ConflictResponse> conflicts = conflictService.checkScheduleConflicts(schedule, null);
            if (!conflicts.isEmpty()) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of("error", conflicts.get(0).message)).build();
            }
        }
        scheduleRepo.persist(schedule);
        return Response.created(URI.create("/api/schedules/" + schedule.id))
                .entity(ScheduleMapper.toResponse(schedule)).build();
    }

    /**
     * Updates an existing schedule (FR-09, FR-15).
     * Applies the same conflict check as on creation.
     *
     * @param id      the schedule ID
     * @param request the updated schedule details
     * @return 200 with the updated schedule
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, @Valid ScheduleRequest request) {
        Schedule schedule = scheduleRepo.findById(id);
        if (schedule == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Schedule not found")).build();
        }
        Device device = deviceRepo.findById(request.deviceId);
        if (device == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Device not found")).build();
        }
        schedule.name = request.name;
        schedule.cronExpression = request.cronExpression;
        schedule.device = device;
        schedule.actionValue = request.actionValue;
        schedule.active = request.active;
        // FR-15: Conflict detection — exclude current schedule
        if (Boolean.TRUE.equals(request.active)) {
            List<ConflictResponse> conflicts = conflictService.checkScheduleConflicts(schedule, id);
            if (!conflicts.isEmpty()) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of("error", conflicts.get(0).message)).build();
            }
        }
        return Response.ok(ScheduleMapper.toResponse(schedule)).build();
    }

    /**
     * Deletes a schedule.
     *
     * @param id the schedule ID
     * @return 204 on success
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Schedule schedule = scheduleRepo.findById(id);
        if (schedule == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Schedule not found")).build();
        }
        scheduleRepo.delete(schedule);
        return Response.noContent().build();
    }
}
