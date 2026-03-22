package at.jku.se.repository;

import at.jku.se.entity.VacationMode;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for {@link VacationMode} entities.
 */
@ApplicationScoped
public class VacationModeRepository implements PanacheRepository<VacationMode> {
}
