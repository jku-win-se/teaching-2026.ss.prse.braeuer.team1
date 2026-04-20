package at.jku.se.repository;

import at.jku.se.entity.Device;
import at.jku.se.entity.Rule;
import at.jku.se.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Repository for {@link Rule} entities.
 */
@ApplicationScoped
public class RuleRepository implements PanacheRepository<Rule> {

    /** Creates the repository; intended for CDI instantiation. */
    public RuleRepository() {}

    /**
     * Returns all rules owned by the given user.
     *
     * @param user the owner
     * @return list of rules
     */
    public List<Rule> findByUser(User user) {
        return list("user", user);
    }

    /**
     * Returns all active rules whose trigger device matches the given device.
     * Used by the rule engine to evaluate EVENT and THRESHOLD rules on state change.
     *
     * @param device the trigger device
     * @return list of active matching rules
     */
    public List<Rule> findActiveByTriggerDevice(Device device) {
        return list("triggerDevice = ?1 and active = true", device);
    }
}
