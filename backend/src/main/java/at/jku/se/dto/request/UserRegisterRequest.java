package at.jku.se.dto.request;

import at.jku.se.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request body for registering a new user (FR-01). */
public class UserRegisterRequest {

    @NotBlank
    @Email(message = "Must be a valid email address")
    public String email;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    public String password;

    @NotNull
    public UserRole role = UserRole.MEMBER;
}
