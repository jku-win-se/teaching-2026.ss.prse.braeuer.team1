package at.jku.se.resource;

import at.jku.se.dto.request.InviteRequest;
import at.jku.se.dto.request.UserLoginRequest;
import at.jku.se.dto.request.UserRegisterRequest;
import at.jku.se.dto.response.UserResponse;
import at.jku.se.entity.User;
import at.jku.se.entity.enums.UserRole;
import at.jku.se.mapper.UserMapper;
import at.jku.se.repository.NotificationRepository;
import at.jku.se.repository.UserRepository;
import at.jku.se.repository.VacationModeRepository;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST resource for user management.
 * Handles registration (FR-01), login (FR-02), and member invitation (FR-20).
 */
@ApplicationScoped
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserRepository userRepo;

    @Inject
    NotificationRepository notifRepo;

    @Inject
    VacationModeRepository vacationRepo;

    /** Default constructor required by CDI. */
    public UserResource() {
    }

    /**
     * Returns all registered users.
     *
     * @return list of all users
     */
    @GET
    public List<UserResponse> listAll() {
        return userRepo.listAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    /**
     * Returns a single user by ID.
     *
     * @param id the user ID
     * @return the user or 404
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        User user = userRepo.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "User not found")).build();
        }
        return Response.ok(UserMapper.toResponse(user)).build();
    }

    /**
     * Registers a new user with a hashed password (FR-01, NFR-02).
     *
     * @param request registration data
     * @return 201 with the created user, or 409 if email already exists
     */
    @POST
    @Path("/register")
    @Transactional
    public Response register(@Valid UserRegisterRequest request) {
        if (userRepo.findByEmail(request.email) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Email already registered")).build();
        }
        User user = new User();
        user.email = request.email;
        user.passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt());
        user.role = request.role;
        user.createdAt = LocalDateTime.now();
        userRepo.persist(user);
        return Response.created(URI.create("/api/users/" + user.id))
                .entity(UserMapper.toResponse(user)).build();
    }

    /**
     * Authenticates a user with email and password (FR-02).
     *
     * @param request login credentials
     * @return 200 with user info, or 401 on invalid credentials
     */
    @POST
    @Path("/login")
    public Response login(@Valid UserLoginRequest request) {
        User user = userRepo.findByEmail(request.email);
        if (user == null || !BCrypt.checkpw(request.password, user.passwordHash)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid email or password")).build();
        }
        return Response.ok(UserMapper.toResponse(user)).build();
    }

    /**
     * Invites a new member by creating an account with MEMBER role (FR-20).
     * Must be called by an OWNER (enforcement left to future auth layer).
     *
     * @param ownerId the inviting owner's ID (for context)
     * @param request the invite details
     * @return 201 with the new member's info
     */
    @POST
    @Path("/{ownerId}/invite")
    @Transactional
    public Response inviteMember(@PathParam("ownerId") Long ownerId,
                                  @Valid InviteRequest request) {
        User owner = userRepo.findById(ownerId);
        if (owner == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Owner not found")).build();
        }
        if (owner.role != UserRole.OWNER) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Only owners can invite members")).build();
        }
        if (userRepo.findByEmail(request.email) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Email already registered")).build();
        }
        User member = new User();
        member.email = request.email;
        member.passwordHash = BCrypt.hashpw(request.temporaryPassword, BCrypt.gensalt());
        member.role = UserRole.MEMBER;
        member.createdAt = LocalDateTime.now();
        userRepo.persist(member);
        return Response.created(URI.create("/api/users/" + member.id))
                .entity(UserMapper.toResponse(member)).build();
    }

    /**
     * Revokes a member's access by deleting their account (FR-20).
     *
     * @param memberId the member's user ID
     * @return 204 on success, 404 if not found, 400 if trying to delete an owner
     */
    @DELETE
    @Path("/{memberId}/revoke")
    @Transactional
    public Response revokeMember(@PathParam("memberId") Long memberId) {
        User member = userRepo.findById(memberId);
        if (member == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "User not found")).build();
        }
        if (member.role == UserRole.OWNER) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Cannot revoke an owner's access")).build();
        }
        // Clean up associated records before deleting the user
        notifRepo.delete("user", member);
        vacationRepo.delete("user", member);
        userRepo.delete(member);
        return Response.noContent().build();
    }

    /**
     * Deletes a user account and all associated data.
     *
     * @param id the user ID
     * @return 204 on success, 404 if not found
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        User user = userRepo.findById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "User not found")).build();
        }
        notifRepo.delete("user", user);
        vacationRepo.delete("user", user);
        userRepo.delete(user);
        return Response.noContent().build();
    }
}
