package at.jku.se.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * Integration tests for {@link at.jku.se.resource.IoTResource}.
 */
@QuarkusTest
class IoTResourceTest {

    @Test
    void getStatus_returns200_withProtocolInfo() {
        given()
            .when().get("/api/iot/status")
            .then()
                .statusCode(200)
                .body("connected", is(true))
                .body("protocol", is("MOCK"));
    }
}
