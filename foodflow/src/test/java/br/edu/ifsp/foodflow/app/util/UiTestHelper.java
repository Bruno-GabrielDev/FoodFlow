package br.edu.ifsp.foodflow.app.util;

import io.restassured.RestAssured;
import br.edu.ifsp.foodflow.app.ui.pages.LoginPage;
import org.openqa.selenium.WebDriver;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.given;

/**
 * Utilitário para os testes de UI.
 * Permite preparar dados (como registrar usuários) via API antes
 * de exercitar a interface gráfica.
 */
public class UiTestHelper {

    private static final String API_BASE_URI = "http://localhost:8080";

    /**
     * Registra um novo garçom via API e retorna suas credenciais,
     * para que possam ser usadas no login pela interface.
     */
    public static AuthHelper.RegisteredUser createUserViaApi() {
        RestAssured.baseURI = API_BASE_URI;
        return AuthHelper.registerWaiter();
    }

    /**
     * Cria um usuário via API, faz login pela UI e espera o redirecionamento ao dashboard.
     * Retorna as credenciais usadas, para o caso de o teste precisar delas.
     */
    public static AuthHelper.RegisteredUser loginViaUi(WebDriver driver, String baseUrl) {
        AuthHelper.RegisteredUser user = createUserViaApi();
        new LoginPage(driver)
                .open(baseUrl)
                .login(user.username(), user.password());
        // Aguarda redirecionamento ao dashboard antes de devolver o controle ao teste
        new LoginPage(driver).urlContains("/dashboard");
        return user;
    }

    /**
     * Fecha todas as comandas ativas via API, liberando mesas para o próximo teste.
     * Útil em @AfterEach de testes de UI que podem deixar comandas abertas.
     */
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

                // Tenta adicionar item (caso a comanda esteja vazia, close exigirá item)
                try {
                    OrderTestHelper.addItem(admin.token(), orderId, menuItemId, admin.userId());
                } catch (Throwable ignored) {}

                // Tenta fechar
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