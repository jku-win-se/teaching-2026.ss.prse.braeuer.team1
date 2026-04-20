package at.jku.se.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

/**
 * An immutable audit record capturing every manual or automated device state change (FR-08).
 * Used as the source of truth for CSV export (FR-16) and the activity feed in the UI.
 */
@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_log_timestamp", columnList = "timestamp"),
        @Index(name = "idx_activity_log_device_id", columnList = "device_id")
})
public class ActivityLog extends PanacheEntity {

    /** Creates an empty entity; fields are populated before persisting. */
    public ActivityLog() {}

    /** The device whose state changed. Deleted automatically when the device is removed. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Device device;

    /** Name of the user or automation rule that caused the change. */
    @Column(nullable = false)
    public String actor;

    /** Human-readable description of the action taken. */
    @Column(nullable = false)
    public String description;

    /** When the state change occurred. */
    @Column(nullable = false)
    public LocalDateTime timestamp;
}
