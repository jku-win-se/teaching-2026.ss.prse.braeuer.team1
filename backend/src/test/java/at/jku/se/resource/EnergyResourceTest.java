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
 * Integration tests for {@link at.jku.se.resource.EnergyResource}.
 * Seed data includes energy logs for alice's devices.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnergyResourceTest {

    // ── Dashboard ──────────────────────────────────────────────────────────
    @Test
    @Order(1)
    void dashboard_withUserId_returns200() {
        given()
            .queryParam("userId", 1)
            .when().get("/api/energy/dashboard")
            .then()
                .statusCode(200)
                .body("totalTodayWh", notNullValue())
                .body("totalWeekWh", notNullValue())
                .body("byDevice.size()", greaterThanOrEqualTo(1))
                .body("byRoom.size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(2)
    void dashboard_withoutUserId_returns400() {
        given()
            .when().get("/api/energy/dashboard")
            .then()
                .statusCode(400)
                .body("error", is("userId query parameter is required"));
    }

    @Test
    @Order(3)
    void dashboard_userNotFound_returns404() {
        given()
            .queryParam("userId", 9999)
            .when().get("/api/energy/dashboard")
            .then()
                .statusCode(404)
                .body("error", is("User not found"));
    }

    // ── CSV Export ──────────────────────────────────────────────────────────
    @Test
    @Order(10)
    void exportCsv_withUserId_returns200() {
        given()
            .queryParam("userId", 1)
            .when().get("/api/energy/export")
            .then()
                .statusCode(200)
                .contentType("text/csv")
                .header("Content-Disposition", containsString("energy-summary.csv"))
                .body(containsString("id,timestamp,deviceId,device,room,consumptionWh"));
    }

    @Test
    @Order(11)
    void exportCsv_withoutUserId_returns400() {
        given()
            .when().get("/api/energy/export")
            .then()
                .statusCode(400);
    }

    @Test
    @Order(12)
    void exportCsv_userNotFound_returns404() {
        given()
            .queryParam("userId", 9999)
            .when().get("/api/energy/export")
            .then()
                .statusCode(404);
    }

    // ── Add Log ────────────────────────────────────────────────────────────
    @Test
    @Order(20)
    void addLog_validData_returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"deviceId":1,"timestamp":"2026-03-22T10:00:00","consumptionWh":15.5}
                """)
            .when().post("/api/energy/logs")
            .then()
                .statusCode(201)
                .body("deviceId", is(1))
                .body("consumptionWh", is(15.5F));
    }

    @Test
    @Order(21)
    void addLog_deviceNotFound_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"deviceId":9999,"timestamp":"2026-03-22T10:00:00","consumptionWh":10.0}
                """)
            .when().post("/api/energy/logs")
            .then()
                .statusCode(404)
                .body("error", is("Device not found"));
    }
}
