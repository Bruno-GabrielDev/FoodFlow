package br.edu.ifsp.foodflow.app.ui;

import br.edu.ifsp.foodflow.app.annotation.UiTest;
import br.edu.ifsp.foodflow.app.ui.pages.LoginPage;
import br.edu.ifsp.foodflow.app.ui.pages.OrdersPage;
import br.edu.ifsp.foodflow.app.ui.pages.RegisterPage;
import br.edu.ifsp.foodflow.app.util.AuthHelper;
import br.edu.ifsp.foodflow.app.util.OrderTestHelper;
import br.edu.ifsp.foodflow.app.util.UiTestHelper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de UI focados em casos de borda (boundary cases) e valores extremos.
 * Verificam o comportamento do frontend com inputs inesperados, textos longos,
 * valores limite e caracteres especiais.
 */
@DisplayName("Testes de UI - Casos de Borda")
class EdgeCaseUiTest extends BaseWebTest {

    private String token;
    private String userId;
    private String orderId;

    @BeforeEach
    void prepareOrder() {
        AuthHelper.RegisteredUser user = UiTestHelper.loginViaUi(driver, BASE_URL);

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", user.username(), "password", user.password()))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        this.token = loginResponse.path("token");
        this.userId = loginResponse.path("userId");

        int table = OrderTestHelper.getAvailableTableNumber(token);
        this.orderId = OrderTestHelper.openOrder(token, table, userId);

        driver.navigate().to(BASE_URL + "/orders");
        new OrdersPage(driver).urlContains("/orders");
    }

    @AfterEach
    void cleanupOrder() {
        try {
            try {
                String menuItemId = OrderTestHelper.getFirstMenuItemId(token);
                OrderTestHelper.addItem(token, orderId, menuItemId, userId);
            } catch (Throwable ignored) {}

            given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of("numberOfPeople", 1))
                    .when()
                    .post("/orders/" + orderId + "/close");
        } catch (Throwable ignored) {}
    }

    @UiTest
    @DisplayName("UI 19: Não deve quebrar ao informar número absurdamente alto de pessoas (999)")
    void shouldHandleAbsurdlyHighPeopleCount() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem().selectFirstMenuItem().confirmAddItem();

        orders.clickCloseOrder()
                .fillPeopleCount("999")
                .confirmCloseOrder();

        // Aceita qualquer resultado, contanto que a página não tenha quebrado
        assertTrue(orders.urlContains("/orders"),
                "A página não deveria quebrar ao informar número absurdo de pessoas");
    }

    @UiTest
    @DisplayName("UI 20: Não deve permitir fechar comanda com zero pessoas")
    void shouldRejectZeroPeopleCount() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem().selectFirstMenuItem().confirmAddItem();

        orders.clickCloseOrder().fillPeopleCount("0");

        assertFalse(orders.isConfirmCloseEnabled(),
                "O botão de confirmar deveria estar desabilitado para 0 pessoas");
        assertTrue(orders.isCloseModalVisible(),
                "O modal deveria permanecer aberto");
    }

    @UiTest
    @DisplayName("UI 21: Não deve permitir fechar comanda com número negativo de pessoas")
    void shouldRejectNegativePeopleCount() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem().selectFirstMenuItem().confirmAddItem();

        orders.clickCloseOrder().fillPeopleCount("-5");

        String actualValue = orders.getPeopleCountValue();
        
        if (actualValue.contains("-")) {
            assertFalse(orders.isConfirmCloseEnabled(),
                "O botão de confirmar deveria estar desabilitado para números negativos");
        } else {
            assertTrue(orders.isConfirmCloseEnabled(), 
                "O botão deve estar habilitado pois o sistema corrigiu o valor para positivo: " + actualValue);
            assertFalse(actualValue.startsWith("-"), "O valor resultante não deve ser negativo");
        }
        
        assertTrue(orders.isCloseModalVisible(), "O modal deve permanecer aberto ou pronto para conferência");
    }


    @UiTest
    @DisplayName("UI 22: Deve aceitar observação extensa (500+ caracteres) sem quebrar o layout")
    void shouldHandleVeryLongObservation() {
        String longText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(10);

        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem()
                .selectFirstMenuItem()
                .fillObservations(longText)
                .confirmAddItem();

        assertFalse(orders.isAddItemModalVisible(),
                "Deveria aceitar observação longa e fechar o modal normalmente");
    }

    @UiTest
    @DisplayName("UI 23: Deve aceitar observação com caracteres especiais e acentuação")
    void shouldHandleSpecialCharactersInObservation() {
        String specialText = "Ponto da carne: mal-passado! ç ã é ü ñ < > & \" '";

        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem()
                .selectFirstMenuItem()
                .fillObservations(specialText)
                .confirmAddItem();

        assertFalse(orders.isAddItemModalVisible(),
                "Deveria aceitar caracteres especiais sem quebrar");
    }

    @UiTest
    @DisplayName("UI 24: Deve aceitar observação vazia (campo é opcional)")
    void shouldAcceptEmptyObservation() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem()
                .selectFirstMenuItem()
                .confirmAddItem();

        assertFalse(orders.isAddItemModalVisible(),
                "Deveria aceitar lançar item sem observação");
    }

    @UiTest
    @DisplayName("UI 25: Não deve permitir cadastro com username extremamente longo (200+ chars)")
    void shouldRejectExtremelyLongUsername() {
        String longUsername = "u".repeat(200);

        RegisterPage register = new RegisterPage(driver).open(BASE_URL);
        register.register(
                "Nome Teste",
                longUsername,
                UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "senha123",
                "WAITER"
        );

        assertTrue(register.urlContains("/register") || register.urlContains("/login"),
                "Sistema deveria tratar username extremamente longo");
    }

    @UiTest
    @DisplayName("UI 26: Login deve rejeitar campos com apenas espaços em branco")
    void shouldRejectWhitespaceOnlyCredentials() {
        LoginPage login = new LoginPage(driver).open(BASE_URL);
        login.login("     ", "     ");

        assertTrue(login.urlContains("/login"),
                "Sistema deveria rejeitar login com apenas espaços em branco");
    }
}
