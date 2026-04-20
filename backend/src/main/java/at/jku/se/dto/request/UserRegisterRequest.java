package at.jku.se.dto.request;

import at.jku.se.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request body for registering a new user (FR-01). */
public class UserRegisterRequest {

    /** Email address of the new account. */
    @NotBlank
    @Email(message = "Must be a valid email address")
    public String email;

    /** Initial plain-text password for the new account. */
    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    public String password;

    /** Role assigned to the new account, defaults to MEMBER. */
    @NotNull
    public UserRole role = UserRole.MEMBER;

    /** Default constructor for deserialization frameworks. */
    public UserRegisterRequest() {
    }
}
