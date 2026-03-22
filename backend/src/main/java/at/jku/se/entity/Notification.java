package at.jku.se.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * An in-app notification sent to a user when a rule fires or fails (FR-12).
 */
@Entity
@Table(name = "notifications")
public class Notification extends PanacheEntity {

    /** The user who receives this notification. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    /** The notification message text. */
    @Column(nullable = false)
    public String message;

    /** When the notification was created. */
    @Column(nullable = false)
    public LocalDateTime createdAt;

    /** Whether the user has acknowledged this notification. */
    @Column(nullable = false)
    public Boolean read;
}
