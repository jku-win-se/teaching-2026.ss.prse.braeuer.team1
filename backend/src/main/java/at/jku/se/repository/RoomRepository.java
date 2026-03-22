package at.jku.se.repository;

import at.jku.se.entity.Room;
import at.jku.se.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Repository for {@link Room} entities.
 */
@ApplicationScoped
public class RoomRepository implements PanacheRepository<Room> {

    /**
     * Returns all rooms owned by the given user.
     *
     * @param user the owner
     * @return list of rooms
     */
    public List<Room> findByUser(User user) {
        return list("user", user);
    }
}
