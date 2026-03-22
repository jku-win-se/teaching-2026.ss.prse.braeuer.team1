package at.jku.se.repository;

import at.jku.se.entity.Scene;
import at.jku.se.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Repository for {@link Scene} entities.
 */
@ApplicationScoped
public class SceneRepository implements PanacheRepository<Scene> {

    /**
     * Returns all scenes owned by the given user.
     *
     * @param user the owner
     * @return list of scenes
     */
    public List<Scene> findByUser(User user) {
        return list("user", user);
    }
}
