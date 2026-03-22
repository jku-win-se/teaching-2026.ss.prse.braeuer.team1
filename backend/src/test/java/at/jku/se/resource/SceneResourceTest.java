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
 * Integration tests for {@link at.jku.se.resource.SceneResource}.
 * Seed data includes "Movie Night" (id=1) and "Good Morning" (id=2) for alice.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SceneResourceTest {

    // ── List ───────────────────────────────────────────────────────────────
    @Test
    @Order(1)
    void list_all_returns200() {
        given()
            .when().get("/api/scenes")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @Order(2)
    void list_byUserId_returns200() {
        given()
            .queryParam("userId", 1)
            .when().get("/api/scenes")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(3)
    void list_userNotFound_returns404() {
        given()
            .queryParam("userId", 9999)
            .when().get("/api/scenes")
            .then()
                .statusCode(404);
    }

    // ── Get by ID ──────────────────────────────────────────────────────────
    @Test
    @Order(4)
    void getById_existing_returns200() {
        given()
            .when().get("/api/scenes/1")
            .then()
                .statusCode(200)
                .body("name", is("Movie Night"))
                .body("deviceStates.size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(5)
    void getById_nonExisting_returns404() {
        given()
            .when().get("/api/scenes/9999")
            .then()
                .statusCode(404);
    }

    // ── Create ─────────────────────────────────────────────────────────────
    @Test
    @Order(10)
    void create_withDeviceStates_returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name":"Test Scene",
                    "userId":1,
                    "deviceStates":[
                        {"deviceId":1,"targetSwitchedOn":true},
                        {"deviceId":2,"targetLevel":50.0}
                    ]
                }
                """)
            .when().post("/api/scenes")
            .then()
                .statusCode(201)
                .body("name", is("Test Scene"))
                .body("deviceStates.size()", is(2));
    }

    @Test
    @Order(11)
    void create_withoutDeviceStates_returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name":"Empty Scene","userId":1}
                """)
            .when().post("/api/scenes")
            .then()
                .statusCode(201)
                .body("name", is("Empty Scene"));
    }

    @Test
    @Order(12)
    void create_userNotFound_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name":"X","userId":9999}
                """)
            .when().post("/api/scenes")
            .then()
                .statusCode(404);
    }

    @Test
    @Order(13)
    void create_deviceNotFound_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name":"X","userId":1,
                    "deviceStates":[{"deviceId":9999,"targetSwitchedOn":true}]
                }
                """)
            .when().post("/api/scenes")
            .then()
                .statusCode(404);
    }

    // ── Update ─────────────────────────────────────────────────────────────
    @Test
    @Order(20)
    void update_existing_returns200() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name":"Movie Night Updated",
                    "userId":1,
                    "deviceStates":[{"deviceId":1,"targetSwitchedOn":false}]
                }
                """)
            .when().put("/api/scenes/1")
            .then()
                .statusCode(200)
                .body("name", is("Movie Night Updated"));
    }

    @Test
    @Order(21)
    void update_nonExisting_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name":"X","userId":1}
                """)
            .when().put("/api/scenes/9999")
            .then()
                .statusCode(404);
    }

    // ── Activate ───────────────────────────────────────────────────────────
    @Test
    @Order(30)
    void activate_existing_returns200_andCreatesNotification() {
        given()
            .contentType(ContentType.JSON)
            .when().post("/api/scenes/2/activate")
            .then()
                .statusCode(200)
                .body("message", containsString("activated successfully"))
                .body("devicesChanged", greaterThanOrEqualTo(1));

        // Verify a notification was created for alice
        given()
            .queryParam("userId", 1)
            .when().get("/api/notifications")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(31)
    void activate_nonExisting_returns404() {
        given()
            .contentType(ContentType.JSON)
            .when().post("/api/scenes/9999/activate")
            .then()
                .statusCode(404);
    }

    // ── Delete ─────────────────────────────────────────────────────────────
    @Test
    @Order(99)
    void delete_nonExisting_returns404() {
        given()
            .when().delete("/api/scenes/9999")
            .then()
                .statusCode(404);
    }
}
