package br.edu.ifsp.foodflow.app.api;

import br.edu.ifsp.foodflow.app.annotation.ApiTest;
import br.edu.ifsp.foodflow.app.util.OrderTestHelper;
import br.edu.ifsp.foodflow.app.util.OrderTestHelper.AuthenticatedUser;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Testes de API - OrderController")
class OrderControllerApiTest extends BaseApiTest {

    /**
     * Registra as comandas abertas durante os testes para limpeza posterior.
     * Cada entrada guarda o token e o orderId.
     */
    private final List<String[]> openedOrders = new ArrayList<>();

    /**
     * Abre uma comanda e registra para limpeza automática no final do teste.
     */
    private String openOrderTracked(AuthenticatedUser user, int table) {
        String orderId = OrderTestHelper.openOrder(user.token(), table, user.userId());
        openedOrders.add(new String[]{user.token(), orderId});
        return orderId;
    }

    /**
     * Após cada teste, fecha todas as comandas abertas para liberar as mesas.
     */
    @AfterEach
    void cleanupOpenedOrders() {
        for (String[] order : openedOrders) {
            String token = order[0];
            String orderId = order[1];
            OrderTestHelper.closeOrderSafely(token, orderId);
        }
        openedOrders.clear();
    }

    @ApiTest
    @DisplayName("Deve rejeitar GET /orders com token JWT inválido")
    void shouldRejectInvalidJwtToken() {
        given()
                .header("Authorization", "Bearer token_falso_que_nao_existe.parte2.parte3")
                .when()
                .get("/orders")
                .then()
                .statusCode(anyOf(is(401), is(403)));
    }

    @ApiTest
    @DisplayName("Valores limites: tableId inválido (0, negativo, inexistente) deve falhar")
    void shouldRejectInvalidTableIds() {
        AuthenticatedUser user = OrderTestHelper.registerAndAuthenticate();

        // tableId = 0 (limite inferior)
        given()
                .header("Authorization", "Bearer " + user.token())
                .contentType(ContentType.JSON)
                .body(Map.of("waiterId", user.userId()))
                .when()
                .post("/orders/0/open")
                .then()
                .statusCode(anyOf(is(400), is(404), is(422), is(500)));

        // tableId = -1 (negativo)
        given()
                .header("Authorization", "Bearer " + user.token())
                .contentType(ContentType.JSON)
                .body(Map.of("waiterId", user.userId()))
                .when()
                .post("/orders/-1/open")
                .then()
                .statusCode(anyOf(is(400), is(404), is(422), is(500)));

        // tableId = 99999 (inexistente, extremo)
        given()
                .header("Authorization", "Bearer " + user.token())
                .contentType(ContentType.JSON)
                .body(Map.of("waiterId", user.userId()))
                .when()
                .post("/orders/99999/open")
                .then()
                .statusCode(anyOf(is(400), is(404), is(422), is(500)));
    }

    @ApiTest
    @DisplayName("Idempotência: fechar a mesma comanda 2x deve falhar na segunda chamada")
    void shouldHandleClosingSameOrderTwice() {
        AuthenticatedUser user = OrderTestHelper.registerAndAuthenticate();
        int tableNumber = OrderTestHelper.getAvailableTableNumber(user.token());
        String orderId = OrderTestHelper.openOrder(user.token(), tableNumber, user.userId());
        // Não usa openOrderTracked porque vamos fechar manualmente

        // Adiciona pelo menos 1 item para poder fechar
        String menuItemId = OrderTestHelper.getFirstMenuItemId(user.token());
        OrderTestHelper.addItem(user.token(), orderId, menuItemId, user.userId());

        // Primeiro fechamento — deve dar certo (200)
        given()
                .header("Authorization", "Bearer " + user.token())
                .contentType(ContentType.JSON)
                .body(Map.of("numberOfPeople", 1))
                .when()
                .post("/orders/" + orderId + "/close")
                .then()
                .statusCode(200);

        // Segundo fechamento — deve falhar (comanda já fechada)
        given()
                .header("Authorization", "Bearer " + user.token())
                .contentType(ContentType.JSON)
                .body(Map.of("numberOfPeople", 1))
                .when()
                .post("/orders/" + orderId + "/close")
                .then()
                .statusCode(anyOf(is(400), is(404), is(409), is(422), is(500)));
    }

    @Nested
    @DisplayName("POST /orders/{tableId}/open")
    class OpenOrderTests {

        @ApiTest
        @DisplayName("Deve abrir uma comanda em mesa disponível e retornar 201")
        void shouldOpenOrderSuccessfully() {
            AuthenticatedUser user = OrderTestHelper.registerAndAuthenticate();
            int table = OrderTestHelper.getAvailableTableNumber(user.token());

            String orderId = openOrderTracked(user, table);

            given()
                    .header("Authorization", "Bearer " + user.token())
                    .when()
                    .get("/orders/tables/" + table + "/order")
                    .then()
                    .statusCode(200)
                    .body("orderId", equalTo(orderId))
                    .body("tableNumber", equalTo(table));
        }

        @ApiTest
        @DisplayName("Deve retornar 404 ao abrir comanda em mesa inexistente")
        void shouldReturn404WhenTableDoesNotExist() {
            AuthenticatedUser user = OrderTestHelper.registerAndAuthenticate();

            given()
                    .header("Authorization", "Bearer " + user.token())
                    .contentType(ContentType.JSON)
                    .body(Map.of("userId", user.userId()))
                    .when()
                    .post("/orders/99999/open")
                    .then()
                    .statusCode(404);
        }

        @ApiTest
        @DisplayName("Deve retornar 403 ao abrir comanda sem token")
        void shouldReturn403WhenNoToken() {
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("userId", UUID.randomUUID().toString()))
                    .when()
                    .post("/orders/6/open")
                    .then()
                    .statusCode(403);
        }
    }

    @Nested
    @DisplayName("GET /orders")
    class ListOrdersTests {

        @ApiTest
        @DisplayName("Deve listar comandas ativas com token válido e retornar 200")
        void shouldListActiveOrders() {
            AuthenticatedUser user = OrderTestHelper.registerAndAuthenticate();

            given()
                    .header("Authorization", "Bearer " + user.token())
                    .when()
                    .get("/orders")
                    .then()
                    .statusCode(200)
                    .body("$", notNullValue());
        }

        @ApiTest
        @DisplayName("Deve retornar 403 ao listar comandas sem token")
        void shouldReturn403WhenNoToken() {
            given()
                    .when()
                    .get("/orders")
                    .then()
                    .statusCode(403);
        }
    }

    @Nested
    @DisplayName("POST /orders/{orderId}/items")
    class AddItemTests {

        @ApiTest
        @DisplayName("Deve adicionar item à comanda e retornar 201")
        void shouldAddItemSuccessfully() {
            AuthenticatedUser user = OrderTestHelper.registerAndAuthenticate();
            int table = OrderTestHelper.getAvailableTableNumber(user.token());
            String orderId = openOrderTracked(user, table);
            String menuItemId = OrderTestHelper.getFirstMenuItemId(user.token());

            given()
                    .header("Authorization", "Bearer " + user.token())
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "menuItemId", menuItemId,
                            "waiterId", user.userId()
                    ))
                    .when()
                    .post("/orders/" + orderId + "/items")
                    .then()
                    .statusCode(201)
                    .body("orderId", equalTo(orderId))
                    .body("total", greaterThan(0f));
        }

        @ApiTest
        @DisplayName("Deve retornar 400 ao adicionar item sem menuItemId")
        void shouldReturn400WhenMenuItemIdIsMissing() {
            AuthenticatedUser user = OrderTestHelper.registerAndAuthenticate();
            int table = OrderTestHelper.getAvailableTableNumber(user.token());
            String orderId = openOrderTracked(user, table);

            given()
                    .header("Authorization", "Bearer " + user.token())
                    .contentType(ContentType.JSON)
                    .body(Map.of("waiterId", user.userId()))
                    .when()
                    .post("/orders/" + orderId + "/items")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("POST /orders/{orderId}/close")
    class CloseOrderTests {

        @ApiTest
        @DisplayName("Deve fechar comanda com itens e retornar 200")
        void shouldCloseOrderSuccessfully() {
            AuthenticatedUser user = OrderTestHelper.registerAndAuthenticate();
            int table = OrderTestHelper.getAvailableTableNumber(user.token());
            String orderId = OrderTestHelper.openOrder(user.token(), table, user.userId());
            String menuItemId = OrderTestHelper.getFirstMenuItemId(user.token());
            OrderTestHelper.addItem(user.token(), orderId, menuItemId, user.userId());

            given()
                    .header("Authorization", "Bearer " + user.token())
                    .contentType(ContentType.JSON)
                    .body(Map.of("numberOfPeople", 2))
                    .when()
                    .post("/orders/" + orderId + "/close")
                    .then()
                    .statusCode(200)
                    .body("orderId", equalTo(orderId))
                    .body("totalPerPerson", notNullValue());
        }

        @ApiTest
        @DisplayName("Deve retornar 422 ao fechar comanda sem itens")
        void shouldReturn422WhenClosingEmptyOrder() {
            AuthenticatedUser user = OrderTestHelper.registerAndAuthenticate();
            int table = OrderTestHelper.getAvailableTableNumber(user.token());
            String orderId = openOrderTracked(user, table);

            given()
                    .header("Authorization", "Bearer " + user.token())
                    .contentType(ContentType.JSON)
                    .body(Map.of("numberOfPeople", 2))
                    .when()
                    .post("/orders/" + orderId + "/close")
                    .then()
                    .statusCode(422);
        }

        @ApiTest
        @DisplayName("Deve retornar 400 ao fechar comanda com número de pessoas inválido")
        void shouldReturn400WhenNumberOfPeopleIsInvalid() {
            AuthenticatedUser user = OrderTestHelper.registerAndAuthenticate();
            int table = OrderTestHelper.getAvailableTableNumber(user.token());
            String orderId = openOrderTracked(user, table);
            String menuItemId = OrderTestHelper.getFirstMenuItemId(user.token());
            OrderTestHelper.addItem(user.token(), orderId, menuItemId, user.userId());

            given()
                    .header("Authorization", "Bearer " + user.token())
                    .contentType(ContentType.JSON)
                    .body(Map.of("numberOfPeople", 0))
                    .when()
                    .post("/orders/" + orderId + "/close")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /orders/tables/{tableId}/order")
    class GetOrderByTableTests {

        @ApiTest
        @DisplayName("Deve buscar comanda ativa de uma mesa e retornar 200")
        void shouldGetOrderByTable() {
            AuthenticatedUser user = OrderTestHelper.registerAndAuthenticate();
            int table = OrderTestHelper.getAvailableTableNumber(user.token());
            String orderId = openOrderTracked(user, table);

            given()
                    .header("Authorization", "Bearer " + user.token())
                    .when()
                    .get("/orders/tables/" + table + "/order")
                    .then()
                    .statusCode(200)
                    .body("orderId", equalTo(orderId))
                    .body("tableNumber", equalTo(table));
        }

        @ApiTest
        @DisplayName("Deve retornar 404 ao buscar comanda de mesa sem comanda ativa")
        void shouldReturn404WhenNoActiveOrder() {
            AuthenticatedUser user = OrderTestHelper.registerAndAuthenticate();

            given()
                    .header("Authorization", "Bearer " + user.token())
                    .when()
                    .get("/orders/tables/99999/order")
                    .then()
                    .statusCode(404);
        }
    }

}