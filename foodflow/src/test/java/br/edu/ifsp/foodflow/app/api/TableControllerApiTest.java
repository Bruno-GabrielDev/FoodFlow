package br.edu.ifsp.foodflow.app.api;

import br.edu.ifsp.foodflow.app.annotation.ApiTest;
import br.edu.ifsp.foodflow.app.util.AuthHelper;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Testes de API - TableController")
class TableControllerApiTest extends BaseApiTest {

    @ApiTest
    @DisplayName("Deve listar as mesas com token válido e retornar 200")
    void shouldListTablesWithValidToken() {
        String token = AuthHelper.registerAndLogin();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tables")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].tableNumber", notNullValue())
                .body("[0].status", notNullValue());
    }

    @ApiTest
    @DisplayName("Deve retornar status de mesa válido (AVAILABLE, OCCUPIED, etc) no payload")
    void shouldReturnValidTableStatusInPayload() {
        String token = AuthHelper.registerAndLogin();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tables")
                .then()
                .statusCode(200)
                .body("status", everyItem(anyOf(
                        equalTo("AVAILABLE"),
                        equalTo("OCCUPIED"),
                        equalTo("CLOSED"),
                        equalTo("RESERVED")
                )));
    }

    @ApiTest
    @DisplayName("Deve retornar 403 ao listar mesas sem token de autenticação")
    void shouldReturn403WhenNoTokenProvided() {
        given()
                .when()
                .get("/tables")
                .then()
                .statusCode(403);
    }

    @ApiTest
    @DisplayName("Deve retornar 403 ao listar mesas com token inválido")
    void shouldReturn403WhenTokenIsInvalid() {
        given()
                .header("Authorization", "Bearer token-invalido-aqui")
                .when()
                .get("/tables")
                .then()
                .statusCode(403);
    }
}