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

/**
 * A time-based schedule that executes a fixed device action on a recurring pattern (FR-09).
 * Unlike rules, schedules have no additional condition — they fire unconditionally at the
 * specified time. Also used as the applied plan for vacation mode (FR-21).
 */
@Entity
@Table(name = "schedules", indexes = {
        @Index(name = "idx_schedule_device_active", columnList = "device_id, active")
})
public class Schedule extends PanacheEntity {

    /** Descriptive name for the schedule (e.g., "Morning Lights"). */
    @Column(nullable = false)
    public String name;

    /**
     * Cron expression defining the recurrence pattern (e.g., {@code "0 7 * * MON-FRI"}).
     * Format: minute hour day-of-month month day-of-week.
     */
    @Column(nullable = false)
    public String cronExpression;

    /** The device this schedule acts on. Schedule is deleted automatically when the device is removed. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Device device;

    /**
     * The value to apply when the schedule fires.
     * Encoded as a string to support all device types
     * (e.g., {@code "true"} for a switch, {@code "75.0"} for a dimmer level).
     */
    @Column(nullable = false)
    public String actionValue;

    /** Whether this schedule is currently active. */
    @Column(nullable = false)
    public Boolean active;

    /** The user who owns this schedule. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;
}
