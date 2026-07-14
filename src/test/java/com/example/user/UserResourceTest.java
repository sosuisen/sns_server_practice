package com.example.user;

import com.example.auth.AuthResourceTest;
import com.example.constraint.UserConstraints;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class UserResourceTest {

    @Test
    void createUser_returns201() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"newuser","email":"newuser@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .header("Location", nullValue())
            .body("id", notNullValue())
            .body("username", is("newuser"))
            .body("email", is("newuser@example.com"))
            .body("role", is("user"))
            .body("passwordHash", nullValue());
    }

    @Test
    void listUsers_requiresAdmin_returns200() {
        String adminToken = AuthResourceTest.getToken("admin@example.com", "pass");
        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test
    void listUsers_limitReturnsOneItem_returns200() {
        String adminToken = AuthResourceTest.getToken("admin@example.com", "pass");
        given()
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("limit", 1)
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("size()", is(1));
    }

    @Test
    void listUsers_limitTooLarge_returns400() {
        String adminToken = AuthResourceTest.getToken("admin@example.com", "pass");
        given()
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("limit", UserConstraints.MAX_LIST_LIMIT + 1)
        .when()
            .get("/users")
        .then()
            .statusCode(400);
    }

    @Test
    void listUsers_unauthenticated_returns401() {
        given()
        .when()
            .get("/users")
        .then()
            .statusCode(401);
    }

    @Test
    void patchUser_unauthenticated_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"whatever"}
                """)
        .when()
            .patch("/users/1")
        .then()
            .statusCode(401);
    }

    @Test
    void deleteUser_unauthenticated_returns401() {
        given()
        .when()
            .delete("/users/1")
        .then()
            .statusCode(401);
    }

    @Test
    void updateUser_self_returns200() {
        long id = given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testbob","email":"testbob@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        String token = AuthResourceTest.getToken("testbob@example.com", "pass1234!");
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("""
                {"username":"testbob2","email":"testbob2@example.com","password":"pass1234!"}
                """)
        .when()
            .patch("/users/" + id)
        .then()
            .statusCode(200)
            .body("username", is("testbob2"));
    }

    @Test
    void createUser_invalidEmail_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"tuser","email":"not-an-email","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(400);
    }

    @Test
    void createUser_usernameTooLong_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"thisusernameistoolong","email":"x@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(400);
    }

    @Test
    void createUser_duplicateEmail_returns409() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testdave","email":"testdave@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testdave2","email":"testdave@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(409)
            .body("errors[0].code", is("EMAIL_IN_USE"));
    }

    @Test
    void deleteUser_bySelf_returns204() {
        long id = given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testcarol","email":"testcarol@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        String token = AuthResourceTest.getToken("testcarol@example.com", "pass1234!");
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/users/" + id)
        .then()
            .statusCode(204);
    }

    @Test
    void deleteUser_byAdmin_returns204() {
        long id = given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testadmindel","email":"testadmindel@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        String adminToken = AuthResourceTest.getToken("admin@example.com", "pass");
        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .delete("/users/" + id)
        .then()
            .statusCode(204);
    }

    @Test
    void patchRole_adminChangesUserToAdmin_returns200() {
        long id = given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testeve","email":"testeve@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        String adminToken = AuthResourceTest.getToken("admin@example.com", "pass");
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {"role":"admin"}
                """)
        .when()
            .patch("/users/" + id + "/role")
        .then()
            .statusCode(200)
            .body("role", is("admin"));
    }

    @Test
    void patchUser_byOtherUser_returns403() {
        long id = given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testgraceowner","email":"testgraceowner@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testgraceother","email":"testgraceother@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users");

        String otherToken = AuthResourceTest.getToken("testgraceother@example.com", "pass1234!");
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + otherToken)
            .body("""
                {"username":"hacked"}
                """)
        .when()
            .patch("/users/" + id)
        .then()
            .statusCode(403);
    }

    @Test
    void deleteUser_byOtherUser_returns403() {
        long id = given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testhankowner","email":"testhankowner@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testhankother","email":"testhankother@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users");

        String otherToken = AuthResourceTest.getToken("testhankother@example.com", "pass1234!");
        given()
            .header("Authorization", "Bearer " + otherToken)
        .when()
            .delete("/users/" + id)
        .then()
            .statusCode(403);
    }

    @Test
    void patchUser_allNullBody_returns400() {
        long id = given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testnullpatch","email":"testnullpatch@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        String token = AuthResourceTest.getToken("testnullpatch@example.com", "pass1234!");
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{}")
        .when()
            .patch("/users/" + id)
        .then()
            .statusCode(400);
    }

    @Test
    void patchUser_duplicateEmail_returns409() {
        long id = given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testemailowner","email":"testemailowner@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testemailother","email":"testemailother@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201);

        String token = AuthResourceTest.getToken("testemailowner@example.com", "pass1234!");
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("""
                {"email":"testemailother@example.com"}
                """)
        .when()
            .patch("/users/" + id)
        .then()
            .statusCode(409)
            .body("errors[0].code", is("EMAIL_IN_USE"));
    }

    @Test
    void patchRole_nullRole_returns400() {
        long id = given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testnullrole","email":"testnullrole@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        String adminToken = AuthResourceTest.getToken("admin@example.com", "pass");
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + adminToken)
            .body("{}")
        .when()
            .patch("/users/" + id + "/role")
        .then()
            .statusCode(400);
    }

    @Test
    void patchUser_byAdmin_returns200() {
        long id = given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testadminpatch","email":"testadminpatch@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        String adminToken = AuthResourceTest.getToken("admin@example.com", "pass");
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {"username":"testadminpatch2"}
                """)
        .when()
            .patch("/users/" + id)
        .then()
            .statusCode(200)
            .body("username", is("testadminpatch2"));
    }

    @Test
    void patchRole_nonAdmin_returns403() {
        long id = given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testfrank","email":"testfrank@example.com","password":"pass1234!"}
                """)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        String userToken = AuthResourceTest.getToken("testfrank@example.com", "pass1234!");
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("""
                {"role":"admin"}
                """)
        .when()
            .patch("/users/" + id + "/role")
        .then()
            .statusCode(403);
    }
}
