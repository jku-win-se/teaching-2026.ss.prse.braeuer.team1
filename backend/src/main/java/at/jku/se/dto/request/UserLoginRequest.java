package at.jku.se.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request body for user login (FR-02). */
public class UserLoginRequest {

    @NotBlank
    @Email
    public String email;

    @NotBlank
    public String password;
}
