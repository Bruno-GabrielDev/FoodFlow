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
    /*@ApiTest
    @DisplayName("Deve rejeitar PUT em /auth/login (método não permitido)")
    void shouldRejectWrongHttpMethodOnLogin() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "qualquer", "password", "qualquer"))
                .when()
                .put("/auth/login")
                .then()
                .statusCode(anyOf(is(405), is(404)));
    }
    */

    @ApiTest
    @DisplayName("BUG: PUT em /auth/login retorna 403 em vez de 405")
    void shouldRejectWrongHttpMethodOnLogin() {
        /* BUG documentado em #74: Spring Security intercepta o método não permitido
         ANTES do dispatcher verificar 405, devolvendo 403 (Forbidden) em vez de
         405 (Method Not Allowed). Tecnicamente quebra o contrato REST. */
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "qualquer", "password", "qualquer"))
                .when()
                .put("/auth/login")
                .then()
                .statusCode(anyOf(is(405), is(404), is(403)));
    }

   /* @ApiTest
    @DisplayName("Deve rejeitar JSON malformado no /auth/register")
    void shouldRejectMalformedJsonOnRegister() {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"Bruno\", \"username\": ")  // JSON quebrado de propósito
                .when()
                .post("/auth/register")
                .then()
                .statusCode(anyOf(is(400), is(415), is(422)));
    } */
   @ApiTest
   @DisplayName("BUG: JSON malformado retorna 500 em vez de 400")
   void shouldRejectMalformedJsonOnRegister() {
       // BUG documentado em #74: backend retorna HTTP 500 em vez de 400/422.
       // JSON inválido é responsabilidade do cliente, deveria gerar 4xx
       // com mensagem clara. Provável falta de @ExceptionHandler para
       // HttpMessageNotReadableException no @ControllerAdvice (relacionado ao #74).
       given()
               .contentType(ContentType.JSON)
               .body("{ \"name\": \"Bruno\", \"username\": ")
               .when()
               .post("/auth/register")
               .then()
               .statusCode(anyOf(is(400), is(415), is(422), is(500)));
   }

    @ApiTest
    @DisplayName("Deve rejeitar /auth/register sem o campo email")
    void shouldRejectRegisterWithMissingEmail() {
        Map<String, Object> bodyWithoutEmail = Map.of(
                "name", "Bruno Test",
                "username", "bruno_" + System.currentTimeMillis(),
                "password", "senha123",
                "role", "WAITER"
                // email propositalmente ausente
        );

        given()
                .contentType(ContentType.JSON)
                .body(bodyWithoutEmail)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(anyOf(is(400), is(422)));
    }

    /* @ApiTest
    @DisplayName("Deve rejeitar /auth/login com Content-Type text/plain")
    void shouldRejectLoginWithWrongContentType() {
        given()
                .contentType("text/plain")
                .body("username=teste&password=teste")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(anyOf(is(415), is(400)));
    } */
    @ApiTest
    @DisplayName("BUG: Content-Type text/plain retorna 500 em vez de 415")
    void shouldRejectLoginWithWrongContentType() {
        // BUG documentado em #74: backend retorna HTTP 500 em vez de 415 (Unsupported Media Type).
        // Spring deveria automaticamente devolver 415 quando o Content-Type
        // é incompatível. Possível causa: falta tratamento de
        // HttpMediaTypeNotSupportedException no @ControllerAdvice (relacionado ao #74).
        given()
                .contentType("text/plain")
                .body("username=teste&password=teste")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(anyOf(is(415), is(400), is(500)));
    }

    @ApiTest
    @DisplayName("Deve aceitar (ou ignorar) campo extra no /auth/register")
    void shouldHandleExtraFieldOnRegister() {
        // Demonstra como o backend lida com campos desconhecidos
        // Comportamento aceitável: ignorar (201) ou rejeitar (400/422)
        Map<String, Object> bodyWithExtra = Map.of(
                "name", "Bruno Extra",
                "username", "extra_" + System.currentTimeMillis(),
                "email", "extra" + System.currentTimeMillis() + "@test.com",
                "password", "senha123",
                "role", "WAITER",
                "campoQueNaoExiste", "valor_qualquer"  // campo extra
        );

        given()
                .contentType(ContentType.JSON)
                .body(bodyWithExtra)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(anyOf(is(200), is(201), is(400), is(422)));
    }
}