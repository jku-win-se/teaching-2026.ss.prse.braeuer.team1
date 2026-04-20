package at.jku.se.entity;

import at.jku.se.entity.enums.DeviceType;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * A virtual smart-home device of a specific type located in a room (FR-04).
 *
 * <p>Device state is stored in two generic fields to avoid a complex type hierarchy:
 * <ul>
 *   <li>{@code switchedOn} – used by SWITCH (on/off).</li>
 *   <li>{@code level} – used by DIMMER (0–100 %), THERMOSTAT (°C setpoint),
 *       SENSOR (current value), BLIND (0 = closed, 100 = open).</li>
 * </ul>
 */
@Entity
@Table(name = "devices")
public class Device extends PanacheEntity {

    /** Creates an empty entity; fields are populated before persisting. */
    public Device() {}

    /** Human-readable name of the device. */
    @Column(nullable = false)
    public String name;

    /** Functional type of the device (determines which state fields are used). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public DeviceType type;

    /** The room this device is installed in. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    public Room room;

    /** Boolean state: on/off for SWITCH. Null for other types. */
    public Boolean switchedOn;

    /**
     * Numeric state level. Interpretation depends on {@code type}:
     * DIMMER = brightness %, THERMOSTAT = target °C, SENSOR = sensor reading,
     * BLIND = open percentage.
     */
    public Double level;

    /** Nominal power draw in Watts — used to estimate energy consumption (FR-14). */
    public Double powerConsumptionWatt;

    /** Timestamp of the last state change (FR-07). */
    @Column(nullable = false)
    public LocalDateTime updatedAt;
}
