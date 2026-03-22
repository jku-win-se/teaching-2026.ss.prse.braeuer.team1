package at.jku.se.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * Integration tests for {@link at.jku.se.resource.DeviceResource}.
 * Seed data includes devices in "Living Room" (id=1) — e.g. "Main Light" (id=1).
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeviceResourceTest {

    // ── Get by ID ──────────────────────────────────────────────────────────
    @Test
    @Order(1)
    void getById_existing_returns200() {
        given()
            .when().get("/api/devices/1")
            .then()
                .statusCode(200)
                .body("name", is("Main Light"))
                .body("type", is("SWITCH"));
    }

    @Test
    @Order(2)
    void getById_nonExisting_returns404() {
        given()
            .when().get("/api/devices/9999")
            .then()
                .statusCode(404)
                .body("error", is("Device not found"));
    }

    // ── Rename ─────────────────────────────────────────────────────────────
    @Test
    @Order(10)
    void rename_existing_returns200() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name":"Main Light Renamed"}
                """)
            .when().put("/api/devices/1/rename")
            .then()
                .statusCode(200)
                .body("name", is("Main Light Renamed"));
    }

    @Test
    @Order(11)
    void rename_nonExisting_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name":"X"}
                """)
            .when().put("/api/devices/9999/rename")
            .then()
                .statusCode(404);
    }

    // ── Update State ───────────────────────────────────────────────────────
    @Test
    @Order(20)
    void updateState_switchOn_returns200_andCreatesActivityLog() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"switchedOn":true,"actor":"test-user"}
                """)
            .when().put("/api/devices/1/state")
            .then()
                .statusCode(200)
                .body("switchedOn", is(true));

        // verify an activity log was created
        given()
            .queryParam("deviceId", 1)
            .when().get("/api/activity-logs")
            .then()
                .statusCode(200)
                .body("size()", org.hamcrest.Matchers.greaterThan(0));
    }

    @Test
    @Order(21)
    void updateState_level_returns200() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"level":50.0,"actor":"test-user"}
                """)
            .when().put("/api/devices/2/state")
            .then()
                .statusCode(200)
                .body("level", is(50.0F));
    }

    @Test
    @Order(22)
    void updateState_nonExistingDevice_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"switchedOn":true,"actor":"x"}
                """)
            .when().put("/api/devices/9999/state")
            .then()
                .statusCode(404);
    }

    // ── Delete ─────────────────────────────────────────────────────────────
    @Test
    @Order(99)
    void delete_nonExisting_returns404() {
        given()
            .when().delete("/api/devices/9999")
            .then()
                .statusCode(404);
    }
}
