package at.jku.se.mapper;

import at.jku.se.dto.response.ScheduleResponse;
import at.jku.se.entity.Schedule;

/** Maps {@link Schedule} entities to response DTOs. */
public class ScheduleMapper {

    private ScheduleMapper() {
    }

    /**
     * Converts a Schedule entity to a ScheduleResponse DTO.
     *
     * @param schedule the entity to convert
     * @return the response DTO
     */
    public static ScheduleResponse toResponse(Schedule schedule) {
        ScheduleResponse r = new ScheduleResponse();
        r.id = schedule.id;
        r.name = schedule.name;
        r.cronExpression = schedule.cronExpression;
        r.deviceId = schedule.device.id;
        r.deviceName = schedule.device.name;
        r.actionValue = schedule.actionValue;
        r.active = schedule.active;
        r.userId = schedule.user.id;
        return r;
    }
}
