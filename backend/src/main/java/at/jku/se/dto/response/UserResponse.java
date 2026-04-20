package at.jku.se.dto.response;

import at.jku.se.entity.enums.UserRole;
import java.time.LocalDateTime;

/** Response DTO for a user — never exposes the password hash. */
public class UserResponse {
    /** Unique identifier of the user. */
    public Long id;

    /** Login email address of the user. */
    public String email;

    /** Access role granted to the user. */
    public UserRole role;

    /** Timestamp when the account was created. */
    public LocalDateTime createdAt;

    /** Default constructor for serialization frameworks. */
    public UserResponse() {
    }
}
