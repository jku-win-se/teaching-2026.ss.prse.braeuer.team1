package at.jku.se.repository;

import at.jku.se.entity.Device;
import at.jku.se.entity.Room;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Repository for {@link Device} entities.
 */
@ApplicationScoped
public class DeviceRepository implements PanacheRepository<Device> {

    /**
     * Returns all devices in the given room.
     *
     * @param room the room to query
     * @return list of devices
     */
    public List<Device> findByRoom(Room room) {
        return list("room", room);
    }
}
