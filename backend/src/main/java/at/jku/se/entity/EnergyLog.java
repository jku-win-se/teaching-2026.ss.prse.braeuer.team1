package at.jku.se.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

/**
 * A periodic energy consumption record for a device (FR-14).
 * Records are created at regular intervals (e.g., hourly) by the energy tracking service,
 * based on the device's nominal {@link Device#powerConsumptionWatt} and its on-time.
 */
@Entity
@Table(name = "energy_logs")
public class EnergyLog extends PanacheEntity {

    /** The device this consumption record belongs to. Deleted automatically when the device is removed. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Device device;

    /** When this record was captured. */
    @Column(nullable = false)
    public LocalDateTime timestamp;

    /** Energy consumed in watt-hours (Wh) during the logging period. */
    @Column(nullable = false)
    public Double consumptionWh;
}
