package at.jku.se.repository;

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

    /**
     * Returns all rules owned by the given user.
     *
     * @param user the owner
     * @return list of rules
     */
    public List<Rule> findByUser(User user) {
        return list("user", user);
    }
}
