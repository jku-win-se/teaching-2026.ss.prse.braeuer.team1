package at.jku.se.resource;

import at.jku.se.dto.response.ConflictResponse;
import at.jku.se.entity.User;
import at.jku.se.repository.UserRepository;
import at.jku.se.service.ConflictDetectionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

/**
 * REST resource for scheduling conflict detection (FR-15).
 * Returns all conflicts between active schedules, rules, and cross-entity conflicts.
 */
@ApplicationScoped
@Path("/api/conflicts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConflictResource {

    @Inject
    ConflictDetectionService conflictService;

    @Inject
    UserRepository userRepo;

    /**
     * Returns all detected automation conflicts.
     * Optionally scoped to a specific user's automations.
     *
     * @param userId optional filter by user ID
     * @return list of conflict descriptions
     */
    @GET
    public Response getConflicts(@QueryParam("userId") Long userId) {
        User user = null;
        if (userId != null) {
            user = userRepo.findById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found")).build();
            }
        }
        List<ConflictResponse> conflicts = conflictService.findAllConflicts(user);
        return Response.ok(conflicts).build();
    }
}
