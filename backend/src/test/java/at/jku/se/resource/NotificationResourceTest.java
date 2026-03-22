package at.jku.se.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Integration tests for {@link at.jku.se.resource.NotificationResource}.
 * Seed data includes notifications for alice (id=1).
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NotificationResourceTest {

    // ── List ───────────────────────────────────────────────────────────────
    @Test
    @Order(1)
    void list_withUserId_returns200() {
        given()
            .queryParam("userId", 1)
            .when().get("/api/notifications")
            .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(2)
    void list_unreadOnly_returns200() {
        given()
            .queryParam("userId", 1)
            .queryParam("unreadOnly", true)
            .when().get("/api/notifications")
            .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(3)
    void list_withoutUserId_returns400() {
        given()
            .when().get("/api/notifications")
            .then()
                .statusCode(400)
                .body("error", is("userId query parameter is required"));
    }

    @Test
    @Order(4)
    void list_userNotFound_returns404() {
        given()
            .queryParam("userId", 9999)
            .when().get("/api/notifications")
            .then()
                .statusCode(404)
                .body("error", is("User not found"));
    }

    // ── Mark as read ───────────────────────────────────────────────────────
    @Test
    @Order(10)
    void markAsRead_existing_returns200() {
        // notification from seed data (id=1)
        given()
            .contentType(ContentType.JSON)
            .when().put("/api/notifications/1/read")
            .then()
                .statusCode(200)
                .body("read", is(true));
    }

    @Test
    @Order(11)
    void markAsRead_nonExisting_returns404() {
        given()
            .contentType(ContentType.JSON)
            .when().put("/api/notifications/9999/read")
            .then()
                .statusCode(404)
                .body("error", is("Notification not found"));
    }

    // ── Mark all as read ───────────────────────────────────────────────────
    @Test
    @Order(20)
    void markAllAsRead_withUserId_returns204() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("userId", 1)
            .when().put("/api/notifications/read-all")
            .then()
                .statusCode(204);
    }

    @Test
    @Order(21)
    void markAllAsRead_withoutUserId_returns400() {
        given()
            .contentType(ContentType.JSON)
            .when().put("/api/notifications/read-all")
            .then()
                .statusCode(400);
    }

    @Test
    @Order(22)
    void markAllAsRead_userNotFound_returns404() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("userId", 9999)
            .when().put("/api/notifications/read-all")
            .then()
                .statusCode(404);
    }
}
