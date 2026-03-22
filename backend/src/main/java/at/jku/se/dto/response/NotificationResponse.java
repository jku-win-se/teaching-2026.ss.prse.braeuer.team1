package at.jku.se.dto.response;

import java.time.LocalDateTime;

/** Response DTO for an in-app notification (FR-12). */
public class NotificationResponse {
    public Long id;
    public Long userId;
    public String message;
    public LocalDateTime createdAt;
    public Boolean read;
}
