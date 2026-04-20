package at.jku.se.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request body for user login (FR-02). */
public class UserLoginRequest {

    /** Email address used to identify the user account. */
    @NotBlank
    @Email
    public String email;

    /** Plain-text password that will be verified against the stored hash. */
    @NotBlank
    public String password;

    /** Default constructor for deserialization frameworks. */
    public UserLoginRequest() {
    }
}
