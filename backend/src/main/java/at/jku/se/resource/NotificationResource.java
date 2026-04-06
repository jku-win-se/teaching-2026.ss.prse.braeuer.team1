package at.jku.se.resource;

import at.jku.se.dto.response.NotificationResponse;
import at.jku.se.entity.Notification;
import at.jku.se.entity.User;
import at.jku.se.mapper.NotificationMapper;
import at.jku.se.repository.NotificationRepository;
import at.jku.se.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * REST resource for in-app notifications (FR-12).
 * Notifications are created automatically by the system (e.g., on scene activation or rule execution).
 */
@ApplicationScoped
@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @Inject
    NotificationRepository notifRepo;

    @Inject
    UserRepository userRepo;

    /**
     * Returns notifications for a user.
     * Use {@code unreadOnly=true} to fetch only unread notifications.
     *
     * @param userId     the user ID (required)
     * @param unreadOnly if true, return only unread notifications (default: false)
     * @return list of notifications
     */
    @GET
    public Response list(@QueryParam("userId") Long userId,
                          @QueryParam("unreadOnly") boolean unreadOnly) {
        if (userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "userId query parameter is required")).build();
        }
        User user = userRepo.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "User not found")).build();
        }
        List<Notification> notifications = unreadOnly
                ? notifRepo.findUnreadByUser(user)
                : notifRepo.findAllByUser(user);
        List<NotificationResponse> result = notifications.stream()
                .map(NotificationMapper::toResponse)
                .toList();
        return Response.ok(result).build();
    }

    /**
     * Marks a single notification as read (FR-12).
     *
     * @param id the notification ID
     * @return 200 with the updated notification
     */
    @PUT
    @Path("/{id}/read")
    @Transactional
    public Response markAsRead(@PathParam("id") Long id) {
        Notification notification = notifRepo.findById(id);
        if (notification == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Notification not found")).build();
        }
        notification.read = true;
        return Response.ok(NotificationMapper.toResponse(notification)).build();
    }

    /**
     * Marks all notifications for a user as read (FR-12).
     *
     * @param userId the user ID
     * @return 204 on success
     */
    @PUT
    @Path("/read-all")
    @Transactional
    public Response markAllAsRead(@QueryParam("userId") Long userId) {
        if (userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "userId query parameter is required")).build();
        }
        User user = userRepo.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "User not found")).build();
        }
        List<Notification> unread = notifRepo.findUnreadByUser(user);
        for (Notification n : unread) {
            n.read = true;
        }
        return Response.noContent().build();
    }
}
