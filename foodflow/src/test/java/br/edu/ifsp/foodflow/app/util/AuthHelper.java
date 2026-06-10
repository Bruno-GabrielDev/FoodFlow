package br.edu.ifsp.foodflow.app.util;

import io.restassured.http.ContentType;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class AuthHelper {

    public static RegisteredUser registerWaiter() {
        return registerUser("WAITER");
    }

    public static RegisteredUser registerUser(String role) {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String username = "user_" + unique;
        String email = unique + "@test.com";
        String password = "senha123";
        String name = "Test User " + unique;

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", name,
                        "username", username,
                        "email", email,
                        "password", password,
                        "role", role
                ))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(200);

        return new RegisteredUser(username, password, email, name, role);
    }

    public static String login(String username, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", username,
                        "password", password
                ))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    public static String registerAndLogin() {
        RegisteredUser user = registerWaiter();
        return login(user.username(), user.password());
    }

    public record RegisteredUser(
            String username,
            String password,
            String email,
            String name,
            String role
    ) {}
}
