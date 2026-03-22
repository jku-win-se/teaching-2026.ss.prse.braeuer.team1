package at.jku.se.resource;

import at.jku.se.dto.request.RuleRequest;
import at.jku.se.dto.response.RuleResponse;
import at.jku.se.entity.Device;
import at.jku.se.entity.Rule;
import at.jku.se.entity.User;
import at.jku.se.mapper.RuleMapper;
import at.jku.se.repository.DeviceRepository;
import at.jku.se.repository.RuleRepository;
import at.jku.se.repository.UserRepository;
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
 * REST resource for IF-THEN automation rules (FR-10, FR-11).
 * Supports TIME_BASED, THRESHOLD, and EVENT trigger types.
 */
@ApplicationScoped
@Path("/api/rules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RuleResource {

    @Inject
    RuleRepository ruleRepo;

    @Inject
    DeviceRepository deviceRepo;

    @Inject
    UserRepository userRepo;

    /**
     * Lists rules. Optionally filtered by userId.
     *
     * @param userId optional filter by owner user ID
     * @return list of rules
     */
    @GET
    public Response list(@QueryParam("userId") Long userId) {
        List<Rule> rules;
        if (userId != null) {
            User user = userRepo.findById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found")).build();
            }
            rules = ruleRepo.findByUser(user);
        } else {
            rules = ruleRepo.listAll();
        }
        List<RuleResponse> result = rules.stream()
                .map(RuleMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(result).build();
    }

    /**
     * Returns a single rule by ID.
     *
     * @param id the rule ID
     * @return the rule or 404
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Rule rule = ruleRepo.findById(id);
        if (rule == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Rule not found")).build();
        }
        return Response.ok(RuleMapper.toResponse(rule)).build();
    }

    /**
     * Creates a new automation rule (FR-10, FR-11).
     *
     * @param request the rule definition
     * @return 201 with the created rule
     */
    @POST
    @Transactional
    public Response create(@Valid RuleRequest request) {
        User user = userRepo.findById(request.userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "User not found")).build();
        }
        Object resolved = resolveDevices(request);
        if (resolved instanceof Response errorResponse) {
            return errorResponse;
        }
        ResolvedDevices devices = (ResolvedDevices) resolved;
        Device actionDevice = devices.actionDevice;
        Device triggerDevice = devices.triggerDevice;
        Rule rule = new Rule();
        rule.name = request.name;
        rule.triggerType = request.triggerType;
        rule.triggerCondition = request.triggerCondition;
        rule.triggerDevice = triggerDevice;
        rule.triggerThresholdValue = request.triggerThresholdValue;
        rule.actionDevice = actionDevice;
        rule.actionValue = request.actionValue;
        rule.active = request.active;
        rule.user = user;
        ruleRepo.persist(rule);
        return Response.created(URI.create("/api/rules/" + rule.id))
                .entity(RuleMapper.toResponse(rule)).build();
    }

    /**
     * Updates an existing rule (FR-10, FR-11).
     *
     * @param id      the rule ID
     * @param request the updated rule definition
     * @return 200 with the updated rule
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, @Valid RuleRequest request) {
        Rule rule = ruleRepo.findById(id);
        if (rule == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Rule not found")).build();
        }
        Object resolved = resolveDevices(request);
        if (resolved instanceof Response errorResponse) {
            return errorResponse;
        }
        ResolvedDevices devices = (ResolvedDevices) resolved;
        Device actionDevice = devices.actionDevice;
        Device triggerDevice = devices.triggerDevice;
        rule.name = request.name;
        rule.triggerType = request.triggerType;
        rule.triggerCondition = request.triggerCondition;
        rule.triggerDevice = triggerDevice;
        rule.triggerThresholdValue = request.triggerThresholdValue;
        rule.actionDevice = actionDevice;
        rule.actionValue = request.actionValue;
        rule.active = request.active;
        return Response.ok(RuleMapper.toResponse(rule)).build();
    }

    /**
     * Deletes a rule.
     *
     * @param id the rule ID
     * @return 204 on success
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Rule rule = ruleRepo.findById(id);
        if (rule == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Rule not found")).build();
        }
        ruleRepo.delete(rule);
        return Response.noContent().build();
    }

    private record ResolvedDevices(Device actionDevice, Device triggerDevice) { }

    /**
     * Resolves action and trigger devices from the request.
     *
     * @param request the rule request
     * @return a ResolvedDevices on success, or a Response with 404 on failure
     */
    private Object resolveDevices(RuleRequest request) {
        Device actionDevice = deviceRepo.findById(request.actionDeviceId);
        if (actionDevice == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Action device not found")).build();
        }
        Device triggerDevice = null;
        if (request.triggerDeviceId != null) {
            triggerDevice = deviceRepo.findById(request.triggerDeviceId);
            if (triggerDevice == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Trigger device not found")).build();
            }
        }
        return new ResolvedDevices(actionDevice, triggerDevice);
    }
}
