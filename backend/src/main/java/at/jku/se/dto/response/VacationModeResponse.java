package at.jku.se.dto.response;

import java.time.LocalDate;

/** Response DTO for a vacation mode configuration. */
public class VacationModeResponse {
    /** Unique identifier of the vacation mode entry. */
    public Long id;

    /** Identifier of the owning user. */
    public Long userId;

    /** First day of the configured vacation period. */
    public LocalDate startDate;

    /** Last day of the configured vacation period. */
    public LocalDate endDate;

    /** Identifier of the schedule applied during vacation mode. */
    public Long scheduleId;

    /** Name of the schedule applied during vacation mode. */
    public String scheduleName;

    /** Whether vacation mode is currently enabled. */
    public Boolean active;

    /** Default constructor for serialization frameworks. */
    public VacationModeResponse() {
    }
}
