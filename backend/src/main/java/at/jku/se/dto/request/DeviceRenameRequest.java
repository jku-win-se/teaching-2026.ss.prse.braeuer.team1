package at.jku.se.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Request body for renaming an existing device (FR-05). */
public class DeviceRenameRequest {

    @NotBlank
    public String name;
}
