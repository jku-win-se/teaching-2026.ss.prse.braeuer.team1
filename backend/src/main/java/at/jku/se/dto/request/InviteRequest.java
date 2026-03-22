package at.jku.se.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for inviting a member by email (FR-20). */
public class InviteRequest {

    @NotBlank
    @Email
    public String email;

    @NotBlank
    @Size(min = 6, message = "Temporary password must be at least 6 characters")
    public String temporaryPassword;
}
