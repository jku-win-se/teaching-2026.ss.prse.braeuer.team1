package at.jku.se.mapper;

import at.jku.se.dto.response.ActivityLogResponse;
import at.jku.se.entity.ActivityLog;

/** Maps {@link ActivityLog} entities to response DTOs. */
public class ActivityLogMapper {

    private ActivityLogMapper() {
    }

    /**
     * Converts an ActivityLog entity to an ActivityLogResponse DTO.
     *
     * @param log the entity to convert
     * @return the response DTO
     */
    public static ActivityLogResponse toResponse(ActivityLog log) {
        ActivityLogResponse r = new ActivityLogResponse();
        r.id = log.id;
        r.deviceId = log.device.id;
        r.deviceName = log.device.name;
        r.roomName = log.device.room.name;
        r.actor = log.actor;
        r.description = log.description;
        r.timestamp = log.timestamp;
        return r;
    }
}
