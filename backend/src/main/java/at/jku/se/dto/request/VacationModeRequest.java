package at.jku.se.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Request body for activating or updating vacation mode (FR-21). */
public class VacationModeRequest {

    /** Identifier of the user for whom vacation mode is configured. */
    @NotNull
    public Long userId;

    /** First day of the vacation period (inclusive). */
    @NotNull
    public LocalDate startDate;

    /** Last day of the vacation period (inclusive). */
    @NotNull
    public LocalDate endDate;

    /** The schedule to apply during the vacation period (overrides normal schedules). */
    @NotNull
    public Long scheduleId;

    /** Whether vacation mode should be active. */
    @NotNull
    public Boolean active;

    /** Default constructor for deserialization frameworks. */
    public VacationModeRequest() {
    }
}
