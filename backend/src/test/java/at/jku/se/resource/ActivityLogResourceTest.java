package at.jku.se.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Integration tests for {@link at.jku.se.resource.ActivityLogResource}.
 * Seed data includes activity logs for alice's devices.
 */
@QuarkusTest
class ActivityLogResourceTest {

    @Test
    void getLogs_allLogs_returns200() {
        given()
            .when().get("/api/activity-logs")
            .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    void getLogs_byDeviceId_returns200() {
        given()
            .queryParam("deviceId", 1)
            .when().get("/api/activity-logs")
            .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].deviceId", is(1));
    }

    @Test
    void getLogs_byDeviceId_notFound_returns404() {
        given()
            .queryParam("deviceId", 9999)
            .when().get("/api/activity-logs")
            .then()
                .statusCode(404)
                .body("error", is("Device not found"));
    }

    @Test
    void getLogs_byUserId_returns200() {
        given()
            .queryParam("userId", 1)
            .when().get("/api/activity-logs")
            .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    void getLogs_byUserId_notFound_returns404() {
        given()
            .queryParam("userId", 9999)
            .when().get("/api/activity-logs")
            .then()
                .statusCode(404)
                .body("error", is("User not found"));
    }

    // ── CSV Export ──────────────────────────────────────────────────────────
    @Test
    void exportCsv_withUserId_returns200_csv() {
        given()
            .when().get("/api/activity-logs/export?userId=1")
            .then()
                .statusCode(200)
                .contentType("text/csv")
                .header("Content-Disposition", containsString("activity-log.csv"))
                .body(containsString("id,timestamp,device,room,actor,description"));
    }

    @Test
    void exportCsv_withoutUserId_returns400() {
        given()
            .when().get("/api/activity-logs/export")
            .then()
                .statusCode(400);
    }

    @Test
    void exportCsv_userNotFound_returns404() {
        given()
            .when().get("/api/activity-logs/export?userId=9999")
            .then()
                .statusCode(404);
    }
}
