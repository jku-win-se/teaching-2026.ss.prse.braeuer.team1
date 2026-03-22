package at.jku.se.mapper;

import at.jku.se.dto.response.NotificationResponse;
import at.jku.se.entity.Notification;
import at.jku.se.entity.User;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class NotificationMapperTest {

    @Test
    void toResponse_mapsAllFields() {
        User u = new User();
        u.id = 1L;
        Notification n = new Notification();
        n.id = 10L;
        n.user = u;
        n.message = "Rule fired";
        n.createdAt = LocalDateTime.of(2026, 3, 22, 8, 0);
        n.read = false;

        NotificationResponse r = NotificationMapper.toResponse(n);
        assertEquals(10L, r.id);
        assertEquals(1L, r.userId);
        assertEquals("Rule fired", r.message);
        assertEquals(LocalDateTime.of(2026, 3, 22, 8, 0), r.createdAt);
        assertFalse(r.read);
    }
}
