package br.edu.ifsp.foodflow.app.util;

import io.restassured.http.ContentType;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class OrderTestHelper {

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

    public record AuthenticatedUser(String userId, String token, String username) {}
}
