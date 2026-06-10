package br.edu.ifsp.foodflow.app.util;

import io.restassured.http.ContentType;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Classe utilitária para os testes de pedidos (OrderController).
 * Centraliza operações comuns como buscar IDs reais do cardápio,
 * abrir comandas e adicionar itens.
 */
public class OrderTestHelper {

    /**
     * Faz login e retorna o objeto completo do usuário (com id e token).
     */
    public static AuthenticatedUser registerAndAuthenticate() {
        AuthHelper.RegisteredUser user = AuthHelper.registerWaiter();

        var response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", user.username(),
                        "password", user.password()
                ))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract();

        String token = response.path("token");
        String userId = response.path("userId");

        return new AuthenticatedUser(userId, token, user.username());
    }

    /**
     * Busca o ID do primeiro item do cardápio.
     */
    public static String getFirstMenuItemId(String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/menu/items")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].id");
    }

    /**
     * Busca os IDs de todos os adicionais.
     */
    public static List<String> getAddOnIds(String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/menu/addons")
                .then()
                .statusCode(200)
                .extract()
                .path("id");
    }

    /**
     * Abre uma comanda em uma mesa e retorna o orderId.
     */
    public static String openOrder(String token, int tableNumber, String userId) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("userId", userId))
                .when()
                .post("/orders/" + tableNumber + "/open")
                .then()
                .statusCode(201)
                .extract()
                .path("orderId");
    }

    /**
     * Adiciona um item a uma comanda e retorna o orderItemId (buscando via detalhes).
     */
    public static void addItem(String token, String orderId, String menuItemId, String waiterId) {
        addItem(token, orderId, menuItemId, waiterId, null);
    }

    public static void addItem(String token, String orderId, String menuItemId, String waiterId, String observations) {
        var body = new java.util.HashMap<String, Object>();
        body.put("menuItemId", menuItemId);
        body.put("waiterId", waiterId);
        if (observations != null) {
            body.put("observations", observations);
        }

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/orders/" + orderId + "/items")
                .then()
                .statusCode(201);
    }

    /**
     * Adiciona um item e retorna o ID atribuido a ele na comanda.
     */
    public static String addItemAndReturnId(
            String token,
            String orderId,
            int tableNumber,
            String menuItemId,
            String waiterId
    ) {
        addItem(token, orderId, menuItemId, waiterId);

        return given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/orders/tables/" + tableNumber + "/order")
                .then()
                .statusCode(200)
                .extract()
                .path("items[-1].id");
    }

    /**
     * Busca o número de uma mesa disponível (status AVAILABLE).
     */
    public static int getAvailableTableNumber(String token) {
        List<Integer> availableTables = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tables")
                .then()
                .statusCode(200)
                .extract()
                .path("findAll { it.status == 'AVAILABLE' }.tableNumber");

        if (availableTables.isEmpty()) {
            throw new IllegalStateException("Nenhuma mesa disponível para o teste.");
        }
        return availableTables.get(0);
    }

    /**
     * Fecha uma comanda de forma segura para liberar a mesa.
     * Usado na limpeza dos testes (cleanup).
     */
    public static void closeOrderSafely(String token, String orderId) {
        try {
            given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of("numberOfPeople", 1))
                    .when()
                    .post("/orders/" + orderId + "/close");
        } catch (Exception ignored) {

        }
    }

    /**
     * Record que representa um usuário autenticado nos testes.
     */
    public record AuthenticatedUser(String userId, String token, String username) {}
}