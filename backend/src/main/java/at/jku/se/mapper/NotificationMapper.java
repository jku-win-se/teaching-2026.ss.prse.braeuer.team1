package at.jku.se.mapper;

import at.jku.se.dto.response.NotificationResponse;
import at.jku.se.entity.Notification;

/** Maps {@link Notification} entities to response DTOs. */
public class NotificationMapper {

    private NotificationMapper() {
    }

    /**
     * Converts a Notification entity to a NotificationResponse DTO.
     *
     * @param notification the entity to convert
     * @return the response DTO
     */
    public static NotificationResponse toResponse(Notification notification) {
        NotificationResponse r = new NotificationResponse();
        r.id = notification.id;
        r.userId = notification.user.id;
        r.message = notification.message;
        r.createdAt = notification.createdAt;
        r.read = notification.read;
        return r;
    }
}
