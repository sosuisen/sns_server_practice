package com.example.message;

import com.example.auth.AuthResourceTest;
import com.example.constraint.MessageConstraints;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class MessageResourceTest {

    private String token;

    @BeforeEach
    void setUp() {
        token = AuthResourceTest.getToken("alice@example.com", "pass");
    }

    @Test
    void createMessage_authenticated_returns201() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"body\":\"Hello World\"}")
        .when()
            .post("/messages")
        .then()
            .statusCode(201)
            .header("Location", nullValue())
            .body("body", is("Hello World"))
            .body("authorName", is("alice"));
    }

    @Test
    void createMessage_unauthenticated_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"body\":\"Hello\"}")
        .when()
            .post("/messages")
        .then()
            .statusCode(401);
    }

    @Test
    void createMessage_bodyTooLong_returns400() {
        String longBody = "a".repeat(141);
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"body\":\"" + longBody + "\"}")
        .when()
            .post("/messages")
        .then()
            .statusCode(400);
    }

    @Test
    void listMessages_authenticated_returns200() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/messages")
        .then()
            .statusCode(200);
    }

    @Test
    void listMessages_unauthenticated_returns401() {
        given()
        .when()
            .get("/messages")
        .then()
            .statusCode(401);
    }

    @Test
    void listMessages_invalidSince_returns400() {
        given()
            .header("Authorization", "Bearer " + token)
            .queryParam("since", "not-a-date")
        .when()
            .get("/messages")
        .then()
            .statusCode(400);
    }

    @Test
    void listMessages_limitZero_returns400() {
        given()
            .header("Authorization", "Bearer " + token)
            .queryParam("limit", 0)
        .when()
            .get("/messages")
        .then()
            .statusCode(400);
    }

    @Test
    void listMessages_limitTooLarge_returns400() {
        given()
            .header("Authorization", "Bearer " + token)
            .queryParam("limit", MessageConstraints.MAX_LIST_LIMIT + 1)
        .when()
            .get("/messages")
        .then()
            .statusCode(400);
    }

    @Test
    void deleteMessage_byAdmin_returns204() {
        long messageId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"body\":\"Alice message for admin delete\"}")
        .when()
            .post("/messages")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        String adminToken = AuthResourceTest.getToken("admin@example.com", "pass");
        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .delete("/messages/" + messageId)
        .then()
            .statusCode(204);
    }

    @Test
    void deleteMessage_byOtherUser_returns403() {
        long messageId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"body\":\"Alice message\"}")
        .when()
            .post("/messages")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username":"testbobmessage","email":"testbobmessage@example.com","password":"pass1234!"}
                """)
        .when().post("/users");

        String bobToken = AuthResourceTest.getToken("testbobmessage@example.com", "pass1234!");

        given()
            .header("Authorization", "Bearer " + bobToken)
        .when()
            .delete("/messages/" + messageId)
        .then()
            .statusCode(403);
    }

}
