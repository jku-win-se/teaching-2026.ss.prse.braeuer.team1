package at.jku.se.mapper;

import at.jku.se.dto.response.UserResponse;
import at.jku.se.entity.User;
import at.jku.se.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        User u = new User();
        u.id = 42L;
        u.email = "test@example.com";
        u.role = UserRole.OWNER;
        u.createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        UserResponse r = UserMapper.toResponse(u);
        assertEquals(42L, r.id);
        assertEquals("test@example.com", r.email);
        assertEquals(UserRole.OWNER, r.role);
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), r.createdAt);
    }

    @Test
    void toResponse_doesNotExposePasswordHash() {
        User u = new User();
        u.id = 1L;
        u.email = "a@b.com";
        u.passwordHash = "secret_hash";
        u.role = UserRole.MEMBER;
        u.createdAt = LocalDateTime.now();

        UserResponse r = UserMapper.toResponse(u);
        // UserResponse has no passwordHash field — just verify no exception
        assertNotNull(r);
    }
}
