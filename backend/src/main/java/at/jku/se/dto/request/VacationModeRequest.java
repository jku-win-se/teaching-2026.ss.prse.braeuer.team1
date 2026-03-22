package at.jku.se.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Request body for activating or updating vacation mode (FR-21). */
public class VacationModeRequest {

    @NotNull
    public Long userId;

    @NotNull
    public LocalDate startDate;

    @NotNull
    public LocalDate endDate;

    /** The schedule to apply during the vacation period (overrides normal schedules). */
    @NotNull
    public Long scheduleId;

    @NotNull
    public Boolean active;
}
