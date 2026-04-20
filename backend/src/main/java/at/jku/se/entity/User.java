package at.jku.se.entity;

import at.jku.se.entity.enums.UserRole;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Represents a registered user of the SmartHome Orchestrator.
 * Supports two roles: OWNER (full access) and MEMBER (control only).
 */
@Entity
@Table(name = "users")
public class User extends PanacheEntity {

    /** Default constructor required by JPA. */
    public User() {
    }

    /** Unique email address used for login and invitations. */
    @Column(nullable = false, unique = true)
    public String email;

    /** Bcrypt-hashed password — never stored in plain text (NFR-02). */
    @Column(nullable = false)
    public String passwordHash;

    /** Access role determining what the user may do (FR-13). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public UserRole role;

    /** Timestamp when the account was created. */
    @Column(nullable = false)
    public LocalDateTime createdAt;
}
