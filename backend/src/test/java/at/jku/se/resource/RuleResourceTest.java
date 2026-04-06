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
 * Integration tests for {@link at.jku.se.resource.RuleResource}.
 * Seed data includes automation rules for alice.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RuleResourceTest {

    // ── List ───────────────────────────────────────────────────────────────
    @Test
    @Order(1)
    void list_all_returns200() {
        given()
            .when().get("/api/rules")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(2)
    void list_byUserId_returns200() {
        given()
            .queryParam("userId", 1)
            .when().get("/api/rules")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(3)
    void list_userNotFound_returns404() {
        given()
            .queryParam("userId", 9999)
            .when().get("/api/rules")
            .then()
                .statusCode(404);
    }

    // ── Get by ID ──────────────────────────────────────────────────────────
    @Test
    @Order(4)
    void getById_existing_returns200() {
        given()
            .when().get("/api/rules/1")
            .then()
                .statusCode(200)
                .body("name", notNullValue());
    }

    @Test
    @Order(5)
    void getById_nonExisting_returns404() {
        given()
            .when().get("/api/rules/9999")
            .then()
                .statusCode(404);
    }

    // ── Create ─────────────────────────────────────────────────────────────
    @Test
    @Order(10)
    void create_validThresholdRule_returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name":"Test Rule",
                    "triggerType":"THRESHOLD",
                    "triggerCondition":"temp > 25",
                    "triggerDeviceId":3,
                    "triggerThresholdValue":25.0,
                    "actionDeviceId":1,
                    "actionValue":"true",
                    "active":true,
                    "userId":1
                }
                """)
            .when().post("/api/rules")
            .then()
                .statusCode(201)
                .body("name", is("Test Rule"))
                .body("triggerType", is("THRESHOLD"))
                .body("active", is(true));
    }

    @Test
    @Order(11)
    void create_timeBasedRule_noTriggerDevice_returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name":"Time Rule",
                    "triggerType":"TIME_BASED",
                    "triggerCondition":"0 8 * * *",
                    "actionDeviceId":2,
                    "actionValue":"true",
                    "active":true,
                    "userId":1
                }
                """)
            .when().post("/api/rules")
            .then()
                .statusCode(201)
                .body("triggerDeviceId", nullValue());
    }

    @Test
    @Order(12)
    void create_userNotFound_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name":"X","triggerType":"THRESHOLD","actionDeviceId":1,
                    "actionValue":"true","active":true,"userId":9999
                }
                """)
            .when().post("/api/rules")
            .then()
                .statusCode(404);
    }

    @Test
    @Order(13)
    void create_actionDeviceNotFound_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name":"X","triggerType":"THRESHOLD","actionDeviceId":9999,
                    "actionValue":"true","active":true,"userId":1
                }
                """)
            .when().post("/api/rules")
            .then()
                .statusCode(404);
    }

    @Test
    @Order(14)
    void create_triggerDeviceNotFound_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name":"X","triggerType":"THRESHOLD","triggerDeviceId":9999,
                    "actionDeviceId":1,"actionValue":"true","active":true,"userId":1
                }
                """)
            .when().post("/api/rules")
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
                    "name":"Updated Rule",
                    "triggerType":"THRESHOLD",
                    "triggerCondition":"temp > 30",
                    "triggerDeviceId":3,
                    "triggerThresholdValue":30.0,
                    "actionDeviceId":1,
                    "actionValue":"false",
                    "active":false,
                    "userId":1
                }
                """)
            .when().put("/api/rules/1")
            .then()
                .statusCode(200)
                .body("name", is("Updated Rule"));
    }

    @Test
    @Order(21)
    void update_nonExisting_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name":"X","triggerType":"THRESHOLD","actionDeviceId":1,
                    "actionValue":"true","active":true,"userId":1
                }
                """)
            .when().put("/api/rules/9999")
            .then()
                .statusCode(404);
    }

    // ── Delete ─────────────────────────────────────────────────────────────
    @Test
    @Order(99)
    void delete_nonExisting_returns404() {
        given()
            .when().delete("/api/rules/9999")
            .then()
                .statusCode(404);
    }
}
