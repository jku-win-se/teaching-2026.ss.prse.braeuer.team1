package at.jku.se.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Integration tests for {@link at.jku.se.resource.RoomResource}.
 * Seed data: "Living Room" (id=1) and "Kitchen" (id=2) owned by alice (id=1).
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoomResourceTest {

    // ── List ───────────────────────────────────────────────────────────────
    @Test
    @Order(1)
    void listAll_returns200() {
        given()
            .when().get("/api/rooms")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @Order(2)
    void listByUserId_returns200() {
        given()
            .queryParam("userId", 1)
            .when().get("/api/rooms")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2))
                .body("[0].userId", is(1));
    }

    @Test
    @Order(3)
    void listByUserId_nonExisting_returns404() {
        given()
            .queryParam("userId", 9999)
            .when().get("/api/rooms")
            .then()
                .statusCode(404)
                .body("error", is("User not found"));
    }

    // ── Get by ID ──────────────────────────────────────────────────────────
    @Test
    @Order(4)
    void getById_existing_returns200() {
        given()
            .when().get("/api/rooms/1")
            .then()
                .statusCode(200)
                .body("name", is("Living Room"));
    }

    @Test
    @Order(5)
    void getById_nonExisting_returns404() {
        given()
            .when().get("/api/rooms/9999")
            .then()
                .statusCode(404)
                .body("error", is("Room not found"));
    }

    // ── Create ─────────────────────────────────────────────────────────────
    @Test
    @Order(10)
    void create_validData_returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name":"Bedroom","userId":1}
                """)
            .when().post("/api/rooms")
            .then()
                .statusCode(201)
                .body("name", is("Bedroom"))
                .body("userId", is(1));
    }

    @Test
    @Order(11)
    void create_userNotFound_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name":"X","userId":9999}
                """)
            .when().post("/api/rooms")
            .then()
                .statusCode(404)
                .body("error", is("User not found"));
    }

    // ── Rename ─────────────────────────────────────────────────────────────
    @Test
    @Order(20)
    void rename_existing_returns200() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name":"Living Room Renamed","userId":1}
                """)
            .when().put("/api/rooms/1")
            .then()
                .statusCode(200)
                .body("name", is("Living Room Renamed"));
    }

    @Test
    @Order(21)
    void rename_nonExisting_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name":"X","userId":1}
                """)
            .when().put("/api/rooms/9999")
            .then()
                .statusCode(404);
    }

    // ── List Devices ───────────────────────────────────────────────────────
    @Test
    @Order(30)
    void listDevices_existingRoom_returns200() {
        given()
            .when().get("/api/rooms/1/devices")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(31)
    void listDevices_nonExistingRoom_returns404() {
        given()
            .when().get("/api/rooms/9999/devices")
            .then()
                .statusCode(404);
    }

    // ── Create Device ──────────────────────────────────────────────────────
    @Test
    @Order(40)
    void createDevice_validData_returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name":"Test Lamp","type":"SWITCH","powerConsumptionWatt":40.0}
                """)
            .when().post("/api/rooms/1/devices")
            .then()
                .statusCode(201)
                .body("name", is("Test Lamp"))
                .body("type", is("SWITCH"));
    }

    @Test
    @Order(41)
    void createDevice_roomNotFound_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name":"X","type":"SWITCH","powerConsumptionWatt":10}
                """)
            .when().post("/api/rooms/9999/devices")
            .then()
                .statusCode(404);
    }

    // ── Delete ─────────────────────────────────────────────────────────────
    @Test
    @Order(99)
    void delete_nonExistingRoom_returns404() {
        given()
            .when().delete("/api/rooms/9999")
            .then()
                .statusCode(404);
    }
}
