package at.jku.se.resource;

import at.jku.se.dto.request.VacationModeRequest;
import at.jku.se.dto.response.VacationModeResponse;
import at.jku.se.entity.Schedule;
import at.jku.se.entity.User;
import at.jku.se.entity.VacationMode;
import at.jku.se.mapper.VacationModeMapper;
import at.jku.se.repository.ScheduleRepository;
import at.jku.se.repository.UserRepository;
import at.jku.se.repository.VacationModeRepository;
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

/**
 * REST resource for vacation mode (FR-21).
 * When active, the configured schedule overrides all normal daily schedules for the given date range.
 */
@ApplicationScoped
@Path("/api/vacation-modes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VacationModeResource {

    @Inject
    VacationModeRepository vacationRepo;

    @Inject
    UserRepository userRepo;

    @Inject
    ScheduleRepository scheduleRepo;

    /** Default constructor required by CDI. */
    public VacationModeResource() {
    }

    /**
     * Lists vacation mode configurations. Optionally filtered by userId.
     *
     * @param userId optional filter by owner user ID
     * @return list of vacation mode entries
     */
    @GET
    public Response list(@QueryParam("userId") Long userId) {
        List<VacationMode> modes;
        if (userId != null) {
            User user = userRepo.findById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found")).build();
            }
            modes = vacationRepo.list("user", user);
        } else {
            modes = vacationRepo.listAll();
        }
        List<VacationModeResponse> result = modes.stream()
                .map(VacationModeMapper::toResponse)
                .toList();
        return Response.ok(result).build();
    }

    /**
     * Returns a single vacation mode by ID.
     *
     * @param id the vacation mode ID
     * @return the vacation mode or 404
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        VacationMode vm = vacationRepo.findById(id);
        if (vm == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Vacation mode not found")).build();
        }
        return Response.ok(VacationModeMapper.toResponse(vm)).build();
    }

    /**
     * Creates a new vacation mode configuration (FR-21).
     *
     * @param request the vacation mode details
     * @return 201 with the created vacation mode
     */
    @POST
    @Transactional
    public Response create(@Valid VacationModeRequest request) {
        User user = userRepo.findById(request.userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "User not found")).build();
        }
        if (request.endDate.isBefore(request.startDate)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "endDate must be on or after startDate")).build();
        }
        Schedule schedule = scheduleRepo.findById(request.scheduleId);
        if (schedule == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Schedule not found")).build();
        }
        VacationMode vm = new VacationMode();
        vm.user = user;
        vm.startDate = request.startDate;
        vm.endDate = request.endDate;
        vm.schedule = schedule;
        vm.active = request.active;
        vacationRepo.persist(vm);
        return Response.created(URI.create("/api/vacation-modes/" + vm.id))
                .entity(VacationModeMapper.toResponse(vm)).build();
    }

    /**
     * Updates an existing vacation mode (FR-21).
     *
     * @param id      the vacation mode ID
     * @param request the updated details
     * @return 200 with the updated vacation mode
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, @Valid VacationModeRequest request) {
        VacationMode vm = vacationRepo.findById(id);
        if (vm == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Vacation mode not found")).build();
        }
        if (request.endDate.isBefore(request.startDate)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "endDate must be on or after startDate")).build();
        }
        Schedule schedule = scheduleRepo.findById(request.scheduleId);
        if (schedule == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Schedule not found")).build();
        }
        vm.startDate = request.startDate;
        vm.endDate = request.endDate;
        vm.schedule = schedule;
        vm.active = request.active;
        return Response.ok(VacationModeMapper.toResponse(vm)).build();
    }

    /**
     * Deletes a vacation mode configuration.
     *
     * @param id the vacation mode ID
     * @return 204 on success
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        VacationMode vm = vacationRepo.findById(id);
        if (vm == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Vacation mode not found")).build();
        }
        vacationRepo.delete(vm);
        return Response.noContent().build();
    }
}
