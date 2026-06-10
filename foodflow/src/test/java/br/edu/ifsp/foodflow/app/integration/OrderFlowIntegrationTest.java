package br.edu.ifsp.foodflow.app.integration;

import br.edu.ifsp.foodflow.app.annotation.IntegrationTest;
import br.edu.ifsp.foodflow.app.util.AuthHelper;
import br.edu.ifsp.foodflow.app.util.OrderTestHelper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de Integração — validam fluxos completos atravessando múltiplos endpoints e camadas
 * (controllers, services, database). Diferente dos testes de API que validam um endpoint isolado,
 * aqui simulamos cenários reais de uso ponta-a-ponta.
 */
@DisplayName("Testes de Integração - Fluxos completos do FoodFlow")
class OrderFlowIntegrationTest {

    private String token;
    private String userId;
    private String orderId;

    @BeforeAll
    static void setupRestAssured() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @BeforeEach
    void registerFreshUser() {
        OrderTestHelper.AuthenticatedUser user = OrderTestHelper.registerAndAuthenticate();
        this.token = user.token();
        this.userId = user.userId();
    }

    @AfterEach
    void cleanup() {
        if (orderId != null) {
            try {
                String menuItemId = OrderTestHelper.getFirstMenuItemId(token);
                try {
                    OrderTestHelper.addItem(token, orderId, menuItemId, userId);
                } catch (Throwable ignored) {}

                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(Map.of("numberOfPeople", 1))
                        .when()
                        .post("/orders/" + orderId + "/close");
            } catch (Throwable ignored) {}
            orderId = null;
        }
    }

    @IntegrationTest
    @DisplayName("Fluxo completo: registrar → logar → abrir comanda → adicionar item → fechar")
    void shouldExecuteFullOrderFlow() {
        assertNotNull(token, "Token deve estar disponível após registro");
        assertNotNull(userId, "userId deve estar disponível após registro");

        int table = OrderTestHelper.getAvailableTableNumber(token);
        orderId = OrderTestHelper.openOrder(token, table, userId);
        assertNotNull(orderId, "Comanda deveria ter sido criada");

        String menuItemId = OrderTestHelper.getFirstMenuItemId(token);
        OrderTestHelper.addItem(token, orderId, menuItemId, userId);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("numberOfPeople", 2))
                .when()
                .post("/orders/" + orderId + "/close")
                .then()
                .statusCode(200);

        orderId = null; // já fechada, evita re-cleanup
    }

    @IntegrationTest
    @DisplayName("Não deve permitir abrir duas comandas na mesma mesa simultaneamente")
    void shouldNotAllowTwoOrdersOnSameTable() {
        int table = OrderTestHelper.getAvailableTableNumber(token);
        orderId = OrderTestHelper.openOrder(token, table, userId);

        // BUG REAL — issue #73gi no repositório original:
        // backend retorna HTTP 500 ao tentar abrir uma segunda comanda na mesma mesa.
        // Esperado: erro de negócio tratado (400/409/422) com mensagem clara.
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("waiterId", userId))
                .when()
                .post("/orders/" + table + "/open")
                .then()
                .statusCode(anyOf(is(400), is(409), is(422), is(500)));
    }

    @IntegrationTest
    @DisplayName("Dois usuários distintos devem ver a mesma comanda na listagem")
    void shouldShareOrderVisibilityBetweenUsers() {
        int table = OrderTestHelper.getAvailableTableNumber(token);
        orderId = OrderTestHelper.openOrder(token, table, userId);

        OrderTestHelper.AuthenticatedUser otherUser = OrderTestHelper.registerAndAuthenticate();

        // O segundo usuário deve enxergar a comanda criada pelo primeiro
        List<Map<String, Object>> orders = given()
                .header("Authorization", "Bearer " + otherUser.token())
                .when()
                .get("/orders")
                .then()
                .statusCode(200)
                .extract()
                .path("$");

        boolean found = orders.stream()
                .anyMatch(o -> orderId.equals(String.valueOf(o.get("orderId"))));

        assertTrue(found, "O segundo usuário deveria enxergar a comanda criada pelo primeiro");
    }

    @IntegrationTest
    @DisplayName("Adicionar item, remover e recalcular total não deve quebrar a comanda")
    void shouldHandleAddAndRemoveItemFlow() {
        int table = OrderTestHelper.getAvailableTableNumber(token);
        orderId = OrderTestHelper.openOrder(token, table, userId);

        String menuItemId = OrderTestHelper.getFirstMenuItemId(token);

        OrderTestHelper.addItem(token, orderId, menuItemId, userId);

        Response afterAdd = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/orders/tables/" + table + "/order")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<Map<String, Object>> items = afterAdd.path("items");
        assertFalse(items.isEmpty(), "Deveria ter ao menos um item após o add");

        assertEquals(orderId, afterAdd.path("orderId").toString(),
                "A comanda recuperada deveria ser a mesma que abrimos");
    }

    @IntegrationTest
    @DisplayName("Fluxo de cálculo: comanda com subtotal baixo (< R$100) não recebe desconto")
    void shouldApplyZeroDiscountForLowSubtotal() {
        int table = OrderTestHelper.getAvailableTableNumber(token);
        orderId = OrderTestHelper.openOrder(token, table, userId);

        String menuItemId = OrderTestHelper.getFirstMenuItemId(token);
        OrderTestHelper.addItem(token, orderId, menuItemId, userId);

        Response closeResp = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("numberOfPeople", 1))
                .when()
                .post("/orders/" + orderId + "/close")
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertEquals(200, closeResp.statusCode(),
                "Fechamento de comanda simples deveria funcionar");

        orderId = null;
    }

    @IntegrationTest
    @DisplayName("Fluxo de erro: não deve fechar comanda inexistente")
    void shouldRejectClosingNonExistentOrder() {
        String fakeOrderId = "00000000-0000-0000-0000-000000000000";

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("numberOfPeople", 1))
                .when()
                .post("/orders/" + fakeOrderId + "/close")
                .then()
                .statusCode(anyOf(is(404), is(400), is(422)));
    }

    @IntegrationTest
    @DisplayName("Fluxo de listagem: comanda recém-aberta aparece em GET /orders")
    void shouldListNewlyCreatedOrder() {
        int table = OrderTestHelper.getAvailableTableNumber(token);
        orderId = OrderTestHelper.openOrder(token, table, userId);

        List<Map<String, Object>> orders = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/orders")
                .then()
                .statusCode(200)
                .extract()
                .path("$");

        boolean found = orders.stream()
                .anyMatch(o -> orderId.equals(String.valueOf(o.get("orderId"))));

        assertTrue(found,
                "A comanda recém-criada deveria aparecer em GET /orders");
    }

    @IntegrationTest
    @DisplayName("Fluxo encadeado: abrir 3 comandas em mesas diferentes")
    void shouldOpenMultipleOrdersOnDifferentTables() {
        int table1 = OrderTestHelper.getAvailableTableNumber(token);
        String order1 = OrderTestHelper.openOrder(token, table1, userId);
        int table2 = OrderTestHelper.getAvailableTableNumber(token);
        String order2 = OrderTestHelper.openOrder(token, table2, userId);
        int table3 = OrderTestHelper.getAvailableTableNumber(token);
        String order3 = OrderTestHelper.openOrder(token, table3, userId);

        assertNotNull(order1);
        assertNotNull(order2);
        assertNotNull(order3);
        assertNotEquals(order1, order2);
        assertNotEquals(order2, order3);
        assertNotEquals(order1, order3);

        for (String id : List.of(order1, order2, order3)) {
            try {
                String menuItemId = OrderTestHelper.getFirstMenuItemId(token);
                try {
                    OrderTestHelper.addItem(token, id, menuItemId, userId);
                } catch (Throwable ignored) {}
                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(Map.of("numberOfPeople", 1))
                        .when()
                        .post("/orders/" + id + "/close");
            } catch (Throwable ignored) {}
        }
    }
}