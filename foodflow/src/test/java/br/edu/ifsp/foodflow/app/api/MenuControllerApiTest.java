package br.edu.ifsp.foodflow.app.api;

import br.edu.ifsp.foodflow.app.annotation.ApiTest;
import br.edu.ifsp.foodflow.app.util.AuthHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Testes de API - MenuController")
class MenuControllerApiTest extends BaseApiTest {

    @Nested
    @DisplayName("GET /menu/items")
    class MenuItemsTests {

        @ApiTest
        @DisplayName("Deve listar itens do cardápio com token válido e retornar 200")
        void shouldListMenuItemsWithValidToken() {
            String token = AuthHelper.registerAndLogin();

            given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/menu/items")
                    .then()
                    .statusCode(200)
                    .body("$", not(empty()))
                    .body("[0].id", notNullValue())
                    .body("[0].name", notNullValue())
                    .body("[0].price", notNullValue());
        }

        @ApiTest
        @DisplayName("Deve retornar 403 ao listar itens sem token")
        void shouldReturn403WhenNoToken() {
            given()
                    .when()
                    .get("/menu/items")
                    .then()
                    .statusCode(403);
        }
    }

    @Nested
    @DisplayName("GET /menu/addons")
    class AddOnsTests {

        @ApiTest
        @DisplayName("Deve listar adicionais com token válido e retornar 200")
        void shouldListAddOnsWithValidToken() {
            String token = AuthHelper.registerAndLogin();

            given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/menu/addons")
                    .then()
                    .statusCode(200)
                    .body("$", not(empty()))
                    .body("[0].id", notNullValue())
                    .body("[0].name", notNullValue())
                    .body("[0].price", notNullValue());
        }

        @ApiTest
        @DisplayName("Deve retornar 403 ao listar adicionais sem token")
        void shouldReturn403WhenNoToken() {
            given()
                    .when()
                    .get("/menu/addons")
                    .then()
                    .statusCode(403);
        }
    }
}
