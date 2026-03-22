package at.jku.se.repository;

import at.jku.se.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for {@link User} entities.
 * Provides CRUD operations and custom queries via Panache.
 */
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    /**
     * Finds a user by their unique email address.
     *
     * @param email the email to search for
     * @return the matching User, or {@code null} if not found
     */
    public User findByEmail(String email) {
        return find("email", email).firstResult();
    }
}
