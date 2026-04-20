package at.jku.se.repository;

import at.jku.se.entity.Schedule;
import at.jku.se.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Repository for {@link Schedule} entities.
 */
@ApplicationScoped
public class ScheduleRepository implements PanacheRepository<Schedule> {

    /** Creates the repository; intended for CDI instantiation. */
    public ScheduleRepository() {}

    /**
     * Returns all schedules owned by the given user.
     *
     * @param user the owner
     * @return list of schedules
     */
    public List<Schedule> findByUser(User user) {
        return list("user", user);
    }
}
