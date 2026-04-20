package at.jku.se.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * A named room (e.g., "Living Room", "Kitchen") that groups devices together.
 * Rooms belong to a single owner user (FR-03).
 */
@Entity
@Table(name = "rooms")
public class Room extends PanacheEntity {

    /** Creates an empty entity; fields are populated before persisting. */
    public Room() {}

    /** Display name of the room. */
    @Column(nullable = false)
    public String name;

    /** The user who owns and manages this room. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    /** Devices installed in this room. Removing a room also removes its devices. */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Device> devices = new ArrayList<>();
}
