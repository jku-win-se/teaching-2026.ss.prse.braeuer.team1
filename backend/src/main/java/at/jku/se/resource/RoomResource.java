package at.jku.se.resource;

import at.jku.se.dto.request.DeviceCreateRequest;
import at.jku.se.dto.request.RoomRequest;
import at.jku.se.dto.response.DeviceResponse;
import at.jku.se.dto.response.RoomResponse;
import at.jku.se.entity.Device;
import at.jku.se.entity.Room;
import at.jku.se.entity.User;
import at.jku.se.mapper.DeviceMapper;
import at.jku.se.mapper.RoomMapper;
import at.jku.se.repository.DeviceRepository;
import at.jku.se.repository.RoomRepository;
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
 * REST resource for room management (FR-03).
 * Rooms group devices and belong to a single user.
 */
@ApplicationScoped
@Path("/api/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @Inject
    RoomRepository roomRepo;

    @Inject
    UserRepository userRepo;

    @Inject
    DeviceRepository deviceRepo;

    /**
     * Lists all rooms. Optionally filtered by owner userId.
     *
     * @param userId optional filter by owner user ID
     * @return list of rooms
     */
    @GET
    public Response listRooms(@QueryParam("userId") Long userId) {
        List<Room> rooms;
        if (userId != null) {
            User user = userRepo.findById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found")).build();
            }
            rooms = roomRepo.findByUser(user);
        } else {
            rooms = roomRepo.listAll();
        }
        List<RoomResponse> result = rooms.stream()
                .map(RoomMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(result).build();
    }

    /**
     * Returns a single room by ID.
     *
     * @param id the room ID
     * @return the room or 404
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Room room = roomRepo.findById(id);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Room not found")).build();
        }
        return Response.ok(RoomMapper.toResponse(room)).build();
    }

    /**
     * Creates a new room for the specified user (FR-03).
     *
     * @param request the room details
     * @return 201 with the created room
     */
    @POST
    @Transactional
    public Response create(@Valid RoomRequest request) {
        User user = userRepo.findById(request.userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "User not found")).build();
        }
        Room room = new Room();
        room.name = request.name;
        room.user = user;
        roomRepo.persist(room);
        return Response.created(URI.create("/api/rooms/" + room.id))
                .entity(RoomMapper.toResponse(room)).build();
    }

    /**
     * Renames an existing room (FR-03).
     *
     * @param id      the room ID
     * @param request the new name
     * @return 200 with the updated room
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public Response rename(@PathParam("id") Long id, @Valid RoomRequest request) {
        Room room = roomRepo.findById(id);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Room not found")).build();
        }
        room.name = request.name;
        return Response.ok(RoomMapper.toResponse(room)).build();
    }

    /**
     * Lists all devices in a room (FR-04).
     *
     * @param id the room ID
     * @return list of devices in the room
     */
    @GET
    @Path("/{id}/devices")
    public Response listDevices(@PathParam("id") Long id) {
        Room room = roomRepo.findById(id);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Room not found")).build();
        }
        List<DeviceResponse> result = deviceRepo.findByRoom(room).stream()
                .map(DeviceMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(result).build();
    }

    /**
     * Adds a new virtual device to a room (FR-04).
     *
     * @param id      the room ID
     * @param request the device details
     * @return 201 with the created device
     */
    @POST
    @Path("/{id}/devices")
    @Transactional
    public Response createDevice(@PathParam("id") Long id, @Valid DeviceCreateRequest request) {
        Room room = roomRepo.findById(id);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Room not found")).build();
        }
        Device device = new Device();
        device.name = request.name;
        device.type = request.type;
        device.room = room;
        device.powerConsumptionWatt = request.powerConsumptionWatt;
        device.updatedAt = LocalDateTime.now();
        deviceRepo.persist(device);
        return Response.created(URI.create("/api/rooms/" + id + "/devices/" + device.id))
                .entity(DeviceMapper.toResponse(device)).build();
    }

    /**
     * Deletes a room and all its devices (FR-03).
     * Device-related logs and energy records are removed via DB cascade.
     *
     * @param id the room ID
     * @return 204 on success
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Room room = roomRepo.findById(id);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Room not found")).build();
        }
        roomRepo.delete(room);
        return Response.noContent().build();
    }
}
