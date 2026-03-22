package at.jku.se.dto.response;

import at.jku.se.entity.enums.UserRole;
import java.time.LocalDateTime;

/** Response DTO for a user — never exposes the password hash. */
public class UserResponse {
    public Long id;
    public String email;
    public UserRole role;
    public LocalDateTime createdAt;
}
