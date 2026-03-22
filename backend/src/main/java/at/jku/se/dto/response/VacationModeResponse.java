package at.jku.se.dto.response;

import java.time.LocalDate;

/** Response DTO for a vacation mode configuration. */
public class VacationModeResponse {
    public Long id;
    public Long userId;
    public LocalDate startDate;
    public LocalDate endDate;
    public Long scheduleId;
    public String scheduleName;
    public Boolean active;
}
