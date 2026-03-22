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
 * Integration tests for {@link at.jku.se.resource.UserResource}.
 * Seed data: alice@example.com (OWNER, id=1), bob@example.com (MEMBER, id=2).
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

    // ── List / Get ────────────────────────────────────────────────────────
    @Test
    @Order(1)
    void listAllUsers_returnsSeededUsers() {
        given()
            .when().get("/api/users")
            .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].email", notNullValue());
    }

    @Test
    @Order(2)
    void getById_existingUser_returns200() {
        given()
            .when().get("/api/users/1")
            .then()
                .statusCode(200)
                .body("email", is("alice@example.com"))
                .body("role", is("OWNER"));
    }

    @Test
    @Order(3)
    void getById_nonExistingUser_returns404() {
        given()
            .when().get("/api/users/9999")
            .then()
                .statusCode(404)
                .body("error", is("User not found"));
    }

    // ── Register ──────────────────────────────────────────────────────────
    @Test
    @Order(10)
    void register_validData_returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"newuser@test.com","password":"secret123","role":"MEMBER"}
                """)
            .when().post("/api/users/register")
            .then()
                .statusCode(201)
                .body("email", is("newuser@test.com"))
                .body("role", is("MEMBER"))
                .body("id", notNullValue());
    }

    @Test
    @Order(11)
    void register_duplicateEmail_returns409() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"alice@example.com","password":"password123","role":"OWNER"}
                """)
            .when().post("/api/users/register")
            .then()
                .statusCode(409)
                .body("error", is("Email already registered"));
    }

    // ── Login ─────────────────────────────────────────────────────────────
    @Test
    @Order(20)
    void login_validCredentials_returns200() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"alice@example.com","password":"password123"}
                """)
            .when().post("/api/users/login")
            .then()
                .statusCode(200)
                .body("email", is("alice@example.com"));
    }

    @Test
    @Order(21)
    void login_wrongPassword_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"alice@example.com","password":"wrongpw"}
                """)
            .when().post("/api/users/login")
            .then()
                .statusCode(401)
                .body("error", is("Invalid email or password"));
    }

    @Test
    @Order(22)
    void login_nonExistingEmail_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"nobody@test.com","password":"pw"}
                """)
            .when().post("/api/users/login")
            .then()
                .statusCode(401)
                .body("error", is("Invalid email or password"));
    }

    // ── Invite ─────────────────────────────────────────────────────────────
    @Test
    @Order(30)
    void invite_asOwner_returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"invited@test.com","temporaryPassword":"temp123"}
                """)
            .when().post("/api/users/1/invite")
            .then()
                .statusCode(201)
                .body("email", is("invited@test.com"))
                .body("role", is("MEMBER"));
    }

    @Test
    @Order(31)
    void invite_asMember_returns403() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"another@test.com","temporaryPassword":"temp123"}
                """)
            .when().post("/api/users/2/invite")
            .then()
                .statusCode(403)
                .body("error", is("Only owners can invite members"));
    }

    @Test
    @Order(32)
    void invite_duplicateEmail_returns409() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"alice@example.com","temporaryPassword":"temp123"}
                """)
            .when().post("/api/users/1/invite")
            .then()
                .statusCode(409)
                .body("error", is("Email already registered"));
    }

    @Test
    @Order(33)
    void invite_ownerNotFound_returns404() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"x@test.com","temporaryPassword":"temppassword"}
                """)
            .when().post("/api/users/9999/invite")
            .then()
                .statusCode(404);
    }

    // ── Revoke ─────────────────────────────────────────────────────────────
    @Test
    @Order(40)
    void revoke_owner_returns400() {
        given()
            .when().delete("/api/users/1/revoke")
            .then()
                .statusCode(400)
                .body("error", is("Cannot revoke an owner's access"));
    }

    @Test
    @Order(41)
    void revoke_nonExisting_returns404() {
        given()
            .when().delete("/api/users/9999/revoke")
            .then()
                .statusCode(404);
    }

    // ── Delete ─────────────────────────────────────────────────────────────
    @Test
    @Order(99)
    void delete_nonExisting_returns404() {
        given()
            .when().delete("/api/users/9999")
            .then()
                .statusCode(404);
    }
}
