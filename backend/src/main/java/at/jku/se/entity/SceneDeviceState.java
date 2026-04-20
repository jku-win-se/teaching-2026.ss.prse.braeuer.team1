package at.jku.se.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * The target state of one device within a scene (FR-17).
 * Uses the same two-field state model as {@link Device}: {@code targetSwitchedOn}
 * and {@code targetLevel}, with null meaning "do not change this aspect".
 */
@Entity
@Table(name = "scene_device_states")
public class SceneDeviceState extends PanacheEntity {

    /** Creates an empty entity; fields are populated before persisting. */
    public SceneDeviceState() {}

    /** The scene this entry belongs to. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "scene_id", nullable = false)
    public Scene scene;

    /** The device whose state should be set when the scene is activated. Deleted automatically when the device is removed. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Device device;

    /**
     * Target on/off state for SWITCH devices.
     * {@code null} means this scene does not control the switch aspect.
     */
    public Boolean targetSwitchedOn;

    /**
     * Target numeric level (brightness %, temperature, sensor value, blind %).
     * {@code null} means this scene does not control the level aspect.
     */
    public Double targetLevel;
}
