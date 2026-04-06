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
class ScheduleResourceTest {

    @Test
    @Order(1)
    void list_all_returns200() {
        given().when().get("/api/schedules").then().statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(2)
    void list_byUserId_returns200() {
        given().queryParam("userId", 1).when().get("/api/schedules").then()
            .statusCode(200).body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(3)
    void list_userNotFound_returns404() {
        given().queryParam("userId", 9999).when().get("/api/schedules").then().statusCode(404);
    }

    @Test
    @Order(4)
    void getById_existing_returns200() {
        given().when().get("/api/schedules/1").then().statusCode(200).body("name", notNullValue());
    }

    @Test
    @Order(5)
    void getById_nonExisting_returns404() {
        given().when().get("/api/schedules/9999").then().statusCode(404);
    }

    @Test
    @Order(10)
    void create_validData_returns201() {
        given().contentType(ContentType.JSON)
            .body("{\"name\":\"Test Schedule\",\"cronExpression\":\"0 9 * * SAT\","
                + "\"deviceId\":1,\"actionValue\":\"true\",\"active\":true,\"userId\":1}")
            .when().post("/api/schedules").then().statusCode(201)
            .body("name", is("Test Schedule")).body("active", is(true));
    }

    @Test
    @Order(11)
    void create_conflict_returns409() {
        given().contentType(ContentType.JSON)
            .body("{\"name\":\"Src\",\"cronExpression\":\"0 12 * * SUN\","
                + "\"deviceId\":1,\"actionValue\":\"true\",\"active\":true,\"userId\":1}")
            .when().post("/api/schedules").then().statusCode(201);

        given().contentType(ContentType.JSON)
            .body("{\"name\":\"Dup\",\"cronExpression\":\"0 12 * * SUN\","
                + "\"deviceId\":1,\"actionValue\":\"false\",\"active\":true,\"userId\":1}")
            .when().post("/api/schedules").then().statusCode(409)
            .body("error", containsString("steuern dasselbe Gerät"));
    }

    @Test
    @Order(12)
    void create_inactive_noConflict_returns201() {
        given().contentType(ContentType.JSON)
            .body("{\"name\":\"Inactive\",\"cronExpression\":\"0 12 * * SUN\","
                + "\"deviceId\":1,\"actionValue\":\"false\",\"active\":false,\"userId\":1}")
            .when().post("/api/schedules").then().statusCode(201);
    }

    @Test
    @Order(13)
    void create_userNotFound_returns404() {
        given().contentType(ContentType.JSON)
            .body("{\"name\":\"X\",\"cronExpression\":\"0 0 * * *\","
                + "\"deviceId\":1,\"actionValue\":\"true\",\"active\":true,\"userId\":9999}")
            .when().post("/api/schedules").then().statusCode(404);
    }

    @Test
    @Order(14)
    void create_deviceNotFound_returns404() {
        given().contentType(ContentType.JSON)
            .body("{\"name\":\"X\",\"cronExpression\":\"0 0 * * *\","
                + "\"deviceId\":9999,\"actionValue\":\"true\",\"active\":true,\"userId\":1}")
            .when().post("/api/schedules").then().statusCode(404);
    }

    @Test
    @Order(20)
    void update_existing_returns200() {
        given().contentType(ContentType.JSON)
            .body("{\"name\":\"Updated\",\"cronExpression\":\"0 8 * * MON-FRI\","
                + "\"deviceId\":1,\"actionValue\":\"false\",\"active\":true,\"userId\":1}")
            .when().put("/api/schedules/1").then().statusCode(200)
            .body("name", is("Updated"));
    }

    @Test
    @Order(21)
    void update_nonExisting_returns404() {
        given().contentType(ContentType.JSON)
            .body("{\"name\":\"X\",\"cronExpression\":\"0 0 * * *\","
                + "\"deviceId\":1,\"actionValue\":\"true\",\"active\":true,\"userId\":1}")
            .when().put("/api/schedules/9999").then().statusCode(404);
    }

    @Test
    @Order(99)
    void delete_nonExisting_returns404() {
        given().when().delete("/api/schedules/9999").then().statusCode(404);
    }
}
