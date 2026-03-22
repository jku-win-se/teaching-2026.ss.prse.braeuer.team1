package at.jku.se.resource;

import at.jku.se.dto.request.SceneRequest;
import at.jku.se.dto.response.SceneResponse;
import at.jku.se.entity.ActivityLog;
import at.jku.se.entity.Device;
import at.jku.se.entity.Notification;
import at.jku.se.entity.Scene;
import at.jku.se.entity.SceneDeviceState;
import at.jku.se.entity.User;
import at.jku.se.mapper.SceneMapper;
import at.jku.se.repository.ActivityLogRepository;
import at.jku.se.repository.DeviceRepository;
import at.jku.se.repository.NotificationRepository;
import at.jku.se.repository.SceneRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST resource for scene management and activation (FR-17).
 * A scene is a named set of device target states activated with a single action.
 */
@ApplicationScoped
@Path("/api/scenes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SceneResource {

    @Inject
    SceneRepository sceneRepo;

    @Inject
    UserRepository userRepo;

    @Inject
    DeviceRepository deviceRepo;

    @Inject
    ActivityLogRepository activityLogRepo;

    @Inject
    NotificationRepository notifRepo;

    /**
     * Lists scenes. Optionally filtered by userId.
     *
     * @param userId optional filter by owner user ID
     * @return list of scenes
     */
    @GET
    public Response list(@QueryParam("userId") Long userId) {
        List<Scene> scenes;
        if (userId != null) {
            User user = userRepo.findById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found")).build();
            }
            scenes = sceneRepo.findByUser(user);
        } else {
            scenes = sceneRepo.listAll();
        }
        List<SceneResponse> result = scenes.stream()
                .map(SceneMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(result).build();
    }

    /**
     * Returns a single scene including all device states.
     *
     * @param id the scene ID
     * @return the scene or 404
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Scene scene = sceneRepo.findById(id);
        if (scene == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Scene not found")).build();
        }
        return Response.ok(SceneMapper.toResponse(scene)).build();
    }

    /**
     * Creates a new scene with optional device target states (FR-17).
     *
     * @param request the scene definition
     * @return 201 with the created scene
     */
    @POST
    @Transactional
    public Response create(@Valid SceneRequest request) {
        User user = userRepo.findById(request.userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "User not found")).build();
        }
        Scene scene = new Scene();
        scene.name = request.name;
        scene.user = user;
        if (request.deviceStates != null) {
            for (var stateReq : request.deviceStates) {
                Device device = deviceRepo.findById(stateReq.deviceId);
                if (device == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of("error", "Device " + stateReq.deviceId + " not found"))
                            .build();
                }
                SceneDeviceState state = new SceneDeviceState();
                state.scene = scene;
                state.device = device;
                state.targetSwitchedOn = stateReq.targetSwitchedOn;
                state.targetLevel = stateReq.targetLevel;
                scene.deviceStates.add(state);
            }
        }
        sceneRepo.persist(scene);
        return Response.created(URI.create("/api/scenes/" + scene.id))
                .entity(SceneMapper.toResponse(scene)).build();
    }

    /**
     * Updates a scene's name and replaces all its device states (FR-17).
     *
     * @param id      the scene ID
     * @param request the updated scene definition
     * @return 200 with the updated scene
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, @Valid SceneRequest request) {
        Scene scene = sceneRepo.findById(id);
        if (scene == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Scene not found")).build();
        }
        scene.name = request.name;
        scene.deviceStates.clear();
        if (request.deviceStates != null) {
            for (var stateReq : request.deviceStates) {
                Device device = deviceRepo.findById(stateReq.deviceId);
                if (device == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of("error", "Device " + stateReq.deviceId + " not found"))
                            .build();
                }
                SceneDeviceState state = new SceneDeviceState();
                state.scene = scene;
                state.device = device;
                state.targetSwitchedOn = stateReq.targetSwitchedOn;
                state.targetLevel = stateReq.targetLevel;
                scene.deviceStates.add(state);
            }
        }
        return Response.ok(SceneMapper.toResponse(scene)).build();
    }

    /**
     * Deletes a scene.
     *
     * @param id the scene ID
     * @return 204 on success
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Scene scene = sceneRepo.findById(id);
        if (scene == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Scene not found")).build();
        }
        sceneRepo.delete(scene);
        return Response.noContent().build();
    }

    /**
     * Activates a scene: applies all device target states, logs each change,
     * and sends a notification to the scene owner (FR-17).
     *
     * @param id the scene ID
     * @return 200 with a confirmation message
     */
    @POST
    @Path("/{id}/activate")
    @Transactional
    public Response activate(@PathParam("id") Long id) {
        Scene scene = sceneRepo.findById(id);
        if (scene == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Scene not found")).build();
        }
        LocalDateTime now = LocalDateTime.now();
        int devicesChanged = 0;
        for (SceneDeviceState state : scene.deviceStates) {
            Device device = state.device;
            StringBuilder description = new StringBuilder("Scene '")
                    .append(scene.name).append("' activated:");
            if (state.targetSwitchedOn != null) {
                device.switchedOn = state.targetSwitchedOn;
                description.append(" switchedOn=").append(state.targetSwitchedOn);
            }
            if (state.targetLevel != null) {
                device.level = state.targetLevel;
                description.append(" level=").append(state.targetLevel);
            }
            device.updatedAt = now;
            ActivityLog log = new ActivityLog();
            log.device = device;
            log.actor = "Scene:" + scene.name;
            log.description = description.toString();
            log.timestamp = now;
            activityLogRepo.persist(log);
            devicesChanged++;
        }
        // FR-12: Send in-app notification to owner
        Notification notification = new Notification();
        notification.user = scene.user;
        notification.message = "Scene '" + scene.name + "' was activated ("
                + devicesChanged + " device(s) updated).";
        notification.createdAt = now;
        notification.read = false;
        notifRepo.persist(notification);
        return Response.ok(Map.of(
                "message", "Scene '" + scene.name + "' activated successfully",
                "devicesChanged", devicesChanged)).build();
    }
}
