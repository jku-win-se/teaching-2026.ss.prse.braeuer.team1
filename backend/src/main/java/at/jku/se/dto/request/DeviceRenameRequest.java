package at.jku.se.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Request body for renaming an existing device (FR-05). */
public class DeviceRenameRequest {

    /** Creates an empty request; fields are populated during deserialization. */
    public DeviceRenameRequest() {}

    /** New display name for the device. */
    @NotBlank
    public String name;
}
