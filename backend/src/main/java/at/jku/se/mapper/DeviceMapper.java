package at.jku.se.mapper;

import at.jku.se.dto.response.DeviceResponse;
import at.jku.se.entity.Device;

/** Maps {@link Device} entities to response DTOs. */
public class DeviceMapper {

    private DeviceMapper() {
    }

    /**
     * Converts a Device entity to a DeviceResponse DTO including current state.
     *
     * @param device the entity to convert
     * @return the response DTO
     */
    public static DeviceResponse toResponse(Device device) {
        DeviceResponse r = new DeviceResponse();
        r.id = device.id;
        r.name = device.name;
        r.type = device.type;
        r.roomId = device.room.id;
        r.roomName = device.room.name;
        r.switchedOn = device.switchedOn;
        r.level = device.level;
        r.powerConsumptionWatt = device.powerConsumptionWatt;
        r.updatedAt = device.updatedAt;
        return r;
    }
}
