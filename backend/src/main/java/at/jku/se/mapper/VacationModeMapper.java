package at.jku.se.mapper;

import at.jku.se.dto.response.VacationModeResponse;
import at.jku.se.entity.VacationMode;

/** Maps {@link VacationMode} entities to response DTOs. */
public class VacationModeMapper {

    private VacationModeMapper() {
    }

    /**
     * Converts a VacationMode entity to a VacationModeResponse DTO.
     *
     * @param vm the entity to convert
     * @return the response DTO
     */
    public static VacationModeResponse toResponse(VacationMode vm) {
        VacationModeResponse r = new VacationModeResponse();
        r.id = vm.id;
        r.userId = vm.user.id;
        r.startDate = vm.startDate;
        r.endDate = vm.endDate;
        r.scheduleId = vm.schedule.id;
        r.scheduleName = vm.schedule.name;
        r.active = vm.active;
        return r;
    }
}
