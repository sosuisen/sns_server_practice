package com.example.auth;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class AuthResourceTest {

    public static String getToken(String email, String password) {
        return given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"%s","password":"%s"}
                """.formatted(email, password))
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .extract().path("jwt");
    }

    @Test
    void login_validCredentials_returnsToken() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"alice@example.com","password":"pass"}
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("jwt", notNullValue());
    }

    @Test
    void login_wrongPassword_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"alice@example.com","password":"wrongpass"}
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    void login_unknownEmail_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"nobody@example.com","password":"pass123"}
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    void login_invalidEmail_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"not-an-email","password":"pass"}
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    void adminLogin_adminUser_returnsToken() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"admin@example.com","password":"pass"}
                """)
        .when()
            .post("/auth/admin-login")
        .then()
            .statusCode(200)
            .body("jwt", notNullValue());
    }

    @Test
    void adminLogin_regularUser_returns403() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"alice@example.com","password":"pass"}
                """)
        .when()
            .post("/auth/admin-login")
        .then()
            .statusCode(403);
    }
}
