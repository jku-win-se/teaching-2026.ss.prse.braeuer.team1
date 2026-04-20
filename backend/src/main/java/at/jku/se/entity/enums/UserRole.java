package at.jku.se.entity.enums;

/**
 * Defines the access roles a user can have in the system.
 * OWNER has full access; MEMBER can only control devices.
 */
public enum UserRole {
    /** Full-access account that can manage users and system configuration. */
    OWNER,

    /** Standard account with limited control capabilities. */
    MEMBER
}
