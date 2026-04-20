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
 * A named collection of device target states that can be activated in one action (FR-17).
 * Example: "Movie Night" dims lights to 20 % and closes all blinds.
 */
@Entity
@Table(name = "scenes")
public class Scene extends PanacheEntity {

    /** Creates an empty entity; fields are populated before persisting. */
    public Scene() {}

    /** Display name of the scene (e.g., "Movie Night"). */
    @Column(nullable = false)
    public String name;

    /** The user who owns this scene. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    /** The individual device target states that make up this scene. */
    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<SceneDeviceState> deviceStates = new ArrayList<>();
}
