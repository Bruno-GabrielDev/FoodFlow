package br.edu.ifsp.foodflow.app.util;

import io.restassured.http.ContentType;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Classe utilitária para autenticação nos testes de API.
 * Permite registrar usuários e obter tokens JWT de forma reutilizável.
 */
public class AuthHelper {

    /**
     * Registra um novo usuário com role WAITER e dados aleatórios.
     * @return o username criado
     */
    public static RegisteredUser registerWaiter() {
        return registerUser("WAITER");
    }

    /**
     * Registra um novo usuário com a role informada e dados aleatórios.
     */
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

    /**
     * Faz login e retorna o token JWT.
     */
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

    /**
     * Registra um waiter e já retorna o token pronto para uso.
     */
    public static String registerAndLogin() {
        RegisteredUser user = registerWaiter();
        return login(user.username(), user.password());
    }

    /**
     * Record que representa um usuário registrado nos testes.
     */
    public record RegisteredUser(
            String username,
            String password,
            String email,
            String name,
            String role
    ) {}
}