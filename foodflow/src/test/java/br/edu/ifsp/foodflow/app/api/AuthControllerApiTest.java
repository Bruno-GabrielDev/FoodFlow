package br.edu.ifsp.foodflow.app.api;

import br.edu.ifsp.foodflow.app.annotation.ApiTest;
import br.edu.ifsp.foodflow.app.util.AuthHelper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Testes de API - AuthController")
class AuthControllerApiTest extends BaseApiTest {

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Nested
    @DisplayName("POST /auth/register")
    class RegisterTests {

        @ApiTest
        @DisplayName("Deve registrar usuário com dados válidos e retornar 200")
        void shouldRegisterUserSuccessfully() {
            String suffix = uniqueSuffix();
            Map<String, Object> body = Map.of(
                    "name", "Fulano Teste",
                    "username", "user_" + suffix,
                    "email", suffix + "@test.com",
                    "password", "senha123",
                    "role", "WAITER"
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(body)
            .when()
                    .post("/auth/register")
            .then()
                    .statusCode(200);
        }

        @ApiTest
        @DisplayName("Deve retornar 400 quando o email for inválido")
        void shouldReturn400WhenEmailIsInvalid() {
            String suffix = uniqueSuffix();
            Map<String, Object> body = Map.of(
                    "name", "Fulano Teste",
                    "username", "user_" + suffix,
                    "email", "email-invalido",
                    "password", "senha123",
                    "role", "WAITER"
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(body)
            .when()
                    .post("/auth/register")
            .then()
                    .statusCode(400);
        }

        @ApiTest
        @DisplayName("Deve retornar 400 quando campos obrigatórios estiverem em branco")
        void shouldReturn400WhenFieldsAreBlank() {
            Map<String, Object> body = Map.of(
                    "name", "",
                    "username", "",
                    "email", "",
                    "password", "",
                    "role", "WAITER"
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(body)
            .when()
                    .post("/auth/register")
            .then()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {

        @ApiTest
        @DisplayName("Deve fazer login com credenciais válidas e retornar token")
        void shouldLoginSuccessfully() {
            AuthHelper.RegisteredUser user = AuthHelper.registerWaiter();

            Map<String, Object> body = Map.of(
                    "username", user.username(),
                    "password", user.password()
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(body)
            .when()
                    .post("/auth/login")
            .then()
                    .statusCode(200)
                    .body("token", notNullValue())
                    .body("username", equalTo(user.username()))
                    .body("userId", notNullValue());
        }

        @ApiTest
        @DisplayName("Deve retornar 401 quando a senha estiver incorreta")
        void shouldReturn401WhenPasswordIsWrong() {
            AuthHelper.RegisteredUser user = AuthHelper.registerWaiter();

            Map<String, Object> body = Map.of(
                    "username", user.username(),
                    "password", "senha-errada"
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(body)
            .when()
                    .post("/auth/login")
            .then()
                    .statusCode(401);
        }

        @ApiTest
        @DisplayName("Deve retornar 401 quando o usuário não existir")
        void shouldReturn401WhenUserDoesNotExist() {
            Map<String, Object> body = Map.of(
                    "username", "usuario_inexistente_" + uniqueSuffix(),
                    "password", "qualquer"
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(body)
            .when()
                    .post("/auth/login")
            .then()
                    .statusCode(401);
        }

        @ApiTest
        @DisplayName("Deve retornar 400 quando username estiver em branco")
        void shouldReturn400WhenUsernameIsBlank() {
            Map<String, Object> body = new HashMap<>();
            body.put("username", "");
            body.put("password", "senha123");

            given()
                    .contentType(ContentType.JSON)
                    .body(body)
            .when()
                    .post("/auth/login")
            .then()
                    .statusCode(400);
        }
    }
}
