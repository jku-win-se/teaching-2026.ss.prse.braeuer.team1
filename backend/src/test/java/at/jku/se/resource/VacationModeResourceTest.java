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

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VacationModeResourceTest {

    @Test
    @Order(1)
    void list_all_returns200() {
        given().when().get("/api/vacation-modes").then().statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(2)
    void list_byUserId_returns200() {
        given().queryParam("userId", 1).when().get("/api/vacation-modes").then()
            .statusCode(200).body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(3)
    void list_userNotFound_returns404() {
        given().queryParam("userId", 9999).when().get("/api/vacation-modes").then().statusCode(404);
    }

    @Test
    @Order(4)
    void getById_existing_returns200() {
        given().when().get("/api/vacation-modes/1").then().statusCode(200)
            .body("userId", is(1)).body("active", notNullValue());
    }

    @Test
    @Order(5)
    void getById_nonExisting_returns404() {
        given().when().get("/api/vacation-modes/9999").then().statusCode(404);
    }

    @Test
    @Order(10)
    void create_validData_returns201() {
        given().contentType(ContentType.JSON)
            .body("{\"userId\":1,\"startDate\":\"2026-06-01\",\"endDate\":\"2026-06-10\","
                + "\"scheduleId\":1,\"active\":true}")
            .when().post("/api/vacation-modes").then().statusCode(201)
            .body("active", is(true));
    }

    @Test
    @Order(11)
    void create_endBeforeStart_returns400() {
        given().contentType(ContentType.JSON)
            .body("{\"userId\":1,\"startDate\":\"2026-06-10\",\"endDate\":\"2026-06-01\","
                + "\"scheduleId\":1,\"active\":true}")
            .when().post("/api/vacation-modes").then().statusCode(400)
            .body("error", is("endDate must be on or after startDate"));
    }

    @Test
    @Order(12)
    void create_userNotFound_returns404() {
        given().contentType(ContentType.JSON)
            .body("{\"userId\":9999,\"startDate\":\"2026-06-01\",\"endDate\":\"2026-06-10\","
                + "\"scheduleId\":1,\"active\":true}")
            .when().post("/api/vacation-modes").then().statusCode(404);
    }

    @Test
    @Order(13)
    void create_scheduleNotFound_returns404() {
        given().contentType(ContentType.JSON)
            .body("{\"userId\":1,\"startDate\":\"2026-06-01\",\"endDate\":\"2026-06-10\","
                + "\"scheduleId\":9999,\"active\":true}")
            .when().post("/api/vacation-modes").then().statusCode(404);
    }

    @Test
    @Order(20)
    void update_existing_returns200() {
        given().contentType(ContentType.JSON)
            .body("{\"userId\":1,\"startDate\":\"2026-07-01\",\"endDate\":\"2026-07-15\","
                + "\"scheduleId\":1,\"active\":false}")
            .when().put("/api/vacation-modes/1").then().statusCode(200)
            .body("active", is(false));
    }

    @Test
    @Order(21)
    void update_nonExisting_returns404() {
        given().contentType(ContentType.JSON)
            .body("{\"userId\":1,\"startDate\":\"2026-07-01\",\"endDate\":\"2026-07-15\","
                + "\"scheduleId\":1,\"active\":true}")
            .when().put("/api/vacation-modes/9999").then().statusCode(404);
    }

    @Test
    @Order(22)
    void update_endBeforeStart_returns400() {
        given().contentType(ContentType.JSON)
            .body("{\"userId\":1,\"startDate\":\"2026-07-15\",\"endDate\":\"2026-07-01\","
                + "\"scheduleId\":1,\"active\":true}")
            .when().put("/api/vacation-modes/1").then().statusCode(400);
    }

    @Test
    @Order(99)
    void delete_nonExisting_returns404() {
        given().when().delete("/api/vacation-modes/9999").then().statusCode(404);
    }
}
