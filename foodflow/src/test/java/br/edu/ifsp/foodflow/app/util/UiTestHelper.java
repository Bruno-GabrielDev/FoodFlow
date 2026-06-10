package br.edu.ifsp.foodflow.app.util;

import io.restassured.RestAssured;
import br.edu.ifsp.foodflow.app.ui.pages.LoginPage;
import org.openqa.selenium.WebDriver;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.given;

public class UiTestHelper {

    private static final String API_BASE_URI = "http://localhost:8080";

    public static AuthHelper.RegisteredUser createUserViaApi() {
        RestAssured.baseURI = API_BASE_URI;
        return AuthHelper.registerWaiter();
    }

    public static AuthHelper.RegisteredUser loginViaUi(WebDriver driver, String baseUrl) {
        AuthHelper.RegisteredUser user = createUserViaApi();
        new LoginPage(driver)
                .open(baseUrl)
                .login(user.username(), user.password());

        new LoginPage(driver).urlContains("/dashboard");
        return user;
    }

    public static void closeAllActiveOrders() {
        try {
            OrderTestHelper.AuthenticatedUser admin = OrderTestHelper.registerAndAuthenticate();

            List<Map<String, Object>> orders = given()
                    .header("Authorization", "Bearer " + admin.token())
                    .when()
                    .get("/orders")
                    .then()
                    .extract()
                    .path("$");

            if (orders == null || orders.isEmpty()) return;

            String menuItemId = OrderTestHelper.getFirstMenuItemId(admin.token());

            for (Map<String, Object> order : orders) {
                Object orderIdObj = order.get("orderId");
                if (orderIdObj == null) continue;
                String orderId = orderIdObj.toString();

                try {
                    OrderTestHelper.addItem(admin.token(), orderId, menuItemId, admin.userId());
                } catch (Throwable ignored) {}

                try {
                    given()
                            .header("Authorization", "Bearer " + admin.token())
                            .contentType(ContentType.JSON)
                            .body(Map.of("numberOfPeople", 1))
                            .when()
                            .post("/orders/" + orderId + "/close");
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }
}
