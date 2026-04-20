package at.jku.se.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDate;

/**
 * Represents an active vacation-mode configuration for a user (FR-21).
 * While active, the referenced schedule overrides all normal daily schedules
 * for the duration of the vacation period.
 */
@Entity
@Table(name = "vacation_modes")
public class VacationMode extends PanacheEntity {

    /** Default constructor required by JPA. */
    public VacationMode() {
    }

    /** The user who activated vacation mode. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    /** First day of the vacation period (inclusive). */
    @Column(nullable = false)
    public LocalDate startDate;

    /** Last day of the vacation period (inclusive). */
    @Column(nullable = false)
    public LocalDate endDate;

    /**
     * The schedule applied during the vacation period.
     * This overrides all normal daily schedules while vacation mode is active.
     * Deleted automatically when the schedule is removed.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Schedule schedule;

    /** Whether vacation mode is currently enabled. */
    @Column(nullable = false)
    public Boolean active;
}
