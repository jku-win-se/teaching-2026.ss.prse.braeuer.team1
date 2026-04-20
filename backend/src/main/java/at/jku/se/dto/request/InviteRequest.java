package at.jku.se.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for inviting a member by email (FR-20). */
public class InviteRequest {

    /** Creates an empty request; fields are populated during deserialization. */
    public InviteRequest() {}

    /** Email address of the person being invited. */
    @NotBlank
    @Email
    public String email;

    /** Initial temporary password assigned to the invited account. */
    @NotBlank
    @Size(min = 6, message = "Temporary password must be at least 6 characters")
    public String temporaryPassword;
}
