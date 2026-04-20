package at.jku.se.repository;

import at.jku.se.entity.EnergyLog;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for {@link EnergyLog} entities.
 */
@ApplicationScoped
public class EnergyLogRepository implements PanacheRepository<EnergyLog> {

    /** Creates the repository; intended for CDI instantiation. */
    public EnergyLogRepository() {}

    /**
     * Returns all energy log entries for all devices belonging to the given user within a time range.
     * Used to compute household-level energy aggregations for the dashboard (FR-14).
     *
     * @param userId the user whose devices to include
     * @param from   start of the time range (inclusive)
     * @param to     end of the time range (inclusive)
     * @return list of energy log entries
     */
    public List<EnergyLog> findByUserIdAndPeriod(Long userId, LocalDateTime from, LocalDateTime to) {
        return list("device.room.user.id = ?1 AND timestamp >= ?2 AND timestamp <= ?3",
                Sort.by("timestamp"), userId, from, to);
    }
}
