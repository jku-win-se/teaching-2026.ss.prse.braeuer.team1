package at.jku.se.mapper;

import at.jku.se.dto.response.EnergyLogResponse;
import at.jku.se.entity.EnergyLog;

/** Maps {@link EnergyLog} entities to response DTOs. */
public class EnergyLogMapper {

    private EnergyLogMapper() {
    }

    /**
     * Converts an EnergyLog entity to an EnergyLogResponse DTO.
     *
     * @param log the entity to convert
     * @return the response DTO
     */
    public static EnergyLogResponse toResponse(EnergyLog log) {
        EnergyLogResponse r = new EnergyLogResponse();
        r.id = log.id;
        r.deviceId = log.device.id;
        r.deviceName = log.device.name;
        r.timestamp = log.timestamp;
        r.consumptionWh = log.consumptionWh;
        return r;
    }
}
