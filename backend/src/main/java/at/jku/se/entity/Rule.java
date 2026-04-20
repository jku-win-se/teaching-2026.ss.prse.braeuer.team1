package at.jku.se.entity;

import at.jku.se.entity.enums.TriggerType;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * An IF-THEN automation rule evaluated by the rule engine (FR-10, FR-11).
 * Supports three trigger types: time-based, threshold (sensor value), and event (state change).
 */
@Entity
@Table(name = "rules")
public class Rule extends PanacheEntity {

    /** Creates an empty entity; fields are populated before persisting. */
    public Rule() {}

    /** Human-readable name for the rule (e.g., "Motion activates hallway light"). */
    @Column(nullable = false)
    public String name;

    /** How the rule is triggered (FR-11). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TriggerType triggerType;

    /**
     * Textual description or cron expression of the trigger condition.
     * For TIME_BASED: a cron string. For others: a human-readable description.
     */
    public String triggerCondition;

    /**
     * The device whose state or value is monitored for THRESHOLD and EVENT triggers.
     * Null for TIME_BASED triggers.
     */
    @ManyToOne
    @JoinColumn(name = "trigger_device_id")
    public Device triggerDevice;

    /**
     * The numeric value that triggers the rule for THRESHOLD type
     * (e.g., temperature below 18.0 °C). Null for other trigger types.
     */
    public Double triggerThresholdValue;

    /** The device on which the rule's action is performed. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "action_device_id", nullable = false)
    public Device actionDevice;

    /**
     * The value to apply to {@code actionDevice} when the rule fires.
     * Same string encoding as {@link Schedule#actionValue}.
     */
    @Column(nullable = false)
    public String actionValue;

    /** Whether this rule is currently active. */
    @Column(nullable = false)
    public Boolean active;

    /** The user who owns this rule. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;
}
