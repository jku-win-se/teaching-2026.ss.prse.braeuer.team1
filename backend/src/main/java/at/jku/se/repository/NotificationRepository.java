package at.jku.se.repository;

import at.jku.se.entity.Notification;
import at.jku.se.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Repository for {@link Notification} entities.
 */
@ApplicationScoped
public class NotificationRepository implements PanacheRepository<Notification> {

    /** Creates the repository; intended for CDI instantiation. */
    public NotificationRepository() {}

    /**
     * Returns all unread notifications for the given user, most recent first.
     *
     * @param user the recipient
     * @return list of unread notifications
     */
    public List<Notification> findUnreadByUser(User user) {
        return list("user = ?1 AND read = false", Sort.by("createdAt").descending(), user);
    }

    /**
     * Returns all notifications for the given user, most recent first.
     *
     * @param user the recipient
     * @return list of all notifications
     */
    public List<Notification> findAllByUser(User user) {
        return list("user", Sort.by("createdAt").descending(), user);
    }
}
