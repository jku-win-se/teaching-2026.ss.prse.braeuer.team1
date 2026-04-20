package at.jku.se.dto.response;

import java.time.LocalDateTime;

/** Response DTO for an in-app notification (FR-12). */
public class NotificationResponse {

    /** Creates an empty response; fields are populated by the mapping layer. */
    public NotificationResponse() {}

    /** Database identifier of the notification. */
    public Long id;
    /** Identifier of the recipient user. */
    public Long userId;
    /** Notification message text. */
    public String message;
    /** Timestamp when the notification was created. */
    public LocalDateTime createdAt;
    /** Whether the user has acknowledged the notification. */
    public Boolean read;
}
