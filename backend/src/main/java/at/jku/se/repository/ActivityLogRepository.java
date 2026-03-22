package at.jku.se.repository;

import at.jku.se.entity.ActivityLog;
import at.jku.se.entity.Device;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Repository for {@link ActivityLog} entities.
 */
@ApplicationScoped
public class ActivityLogRepository implements PanacheRepository<ActivityLog> {

    /**
     * Returns all activity log entries for the given device, most recent first.
     *
     * @param device the device to query
     * @return list of log entries
     */
    public List<ActivityLog> findByDevice(Device device) {
        return list("device", Sort.by("timestamp").descending(), device);
    }

    /**
     * Returns all activity log entries for all devices belonging to the given user.
     * Used for user-level activity feed and CSV export (FR-16).
     *
     * @param userId the user ID
     * @return list of log entries, most recent first
     */
    public List<ActivityLog> findByUserId(Long userId) {
        return list("device.room.user.id = ?1", Sort.by("timestamp").descending(), userId);
    }
}
