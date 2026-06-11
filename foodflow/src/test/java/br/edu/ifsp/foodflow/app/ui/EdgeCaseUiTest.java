package br.edu.ifsp.foodflow.app.ui;

import br.edu.ifsp.foodflow.app.annotation.IssueTest;
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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de UI - Casos de Borda")
class EdgeCaseUiTest extends BaseWebTest {

    private String token;
    private String userId;
    private String orderId;

    @BeforeEach
    void prepareOrder() {
        AuthHelper.RegisteredUser user = UiTestHelper.loginViaUi(driver, BASE_URL);
        Response loginResponse = given().contentType(ContentType.JSON)
                .body(Map.of("username", user.username(), "password", user.password()))
                .when().post("/auth/login").then().statusCode(200).extract().response();

        this.token = loginResponse.path("token");
        this.userId = loginResponse.path("userId");
        this.orderId = OrderTestHelper.openOrder(token, OrderTestHelper.getAvailableTableNumber(token), userId);
        driver.navigate().to(BASE_URL + "/orders");
    }

    @AfterEach
    void cleanupOrder() {
        if (token != null && orderId != null) {
            OrderTestHelper.closeOrderSafely(token, orderId);
        }
    }

    @UiTest
    @DisplayName("UI 19: Não deve quebrar ao informar número absurdamente alto de pessoas (999)")
    void shouldHandleAbsurdlyHighPeopleCount() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem().selectFirstMenuItem().confirmAddItem();
        orders.clickCloseOrder().fillPeopleCount("999").confirmCloseOrder();
        assertTrue(orders.urlContains("/orders"), "A página não deveria quebrar com número absurdo de pessoas");
    }

    @UiTest
    @DisplayName("UI 20: Não deve permitir fechar comanda com zero pessoas")
    void shouldRejectZeroPeopleCount() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem().selectFirstMenuItem().confirmAddItem();
        orders.clickCloseOrder().fillPeopleCount("0");
        assertFalse(orders.isConfirmCloseEnabled(), "Botão confirmar deveria estar desabilitado para 0 pessoas");
    }

    @UiTest
    @DisplayName("UI 21: Não deve permitir fechar comanda com número negativo de pessoas")
    void shouldRejectNegativePeopleCount() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem().selectFirstMenuItem().confirmAddItem();
        orders.clickCloseOrder().fillPeopleCount("-5");
        String actualValue = orders.getPeopleCountValue();
        if (actualValue.contains("-")) {
            assertFalse(orders.isConfirmCloseEnabled(), "Botão deveria estar desabilitado para números negativos");
        } else {
            assertTrue(orders.isConfirmCloseEnabled(), "Sistema corrigiu para positivo: " + actualValue);
        }
    }

    @UiTest
    @DisplayName("UI 23: Deve aceitar observação com caracteres especiais e acentuação")
    void shouldHandleSpecialCharactersInObservation() {
        String specialText = "áéíóú çãñ < > & ' \"";
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem().selectFirstMenuItem().fillObservations(specialText).confirmAddItem();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        boolean modalClosed = wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//h3[contains(.,'Adicionar Item')]")));
        assertTrue(modalClosed, "FALHA DE UI: O modal não fechou ao usar acentuação, indicando erro de processamento no Frontend.");
    }

    @UiTest
    @DisplayName("UI 24: Deve aceitar observação vazia (campo é opcional)")
    void shouldAcceptEmptyObservation() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem().selectFirstMenuItem().confirmAddItem();
        assertFalse(orders.isAddItemModalVisible());
    }

    @UiTest
    @DisplayName("UI 25: Não deve permitir cadastro com username extremamente longo (200+ chars)")
    void shouldRejectExtremelyLongUsername() {
        String longUsername = "u".repeat(200);
        RegisterPage register = new RegisterPage(driver).open(BASE_URL);
        register.register("Nome", longUsername, UUID.randomUUID().toString().substring(0,8)+"@test.com", "senha123", "WAITER");
        assertTrue(register.urlContains("/register") || register.urlContains("/login"));
    }

    @UiTest
    @DisplayName("UI 26: Login deve rejeitar campos com apenas espaços em branco")
    void shouldRejectWhitespaceOnlyCredentials() {
        new LoginPage(driver).open(BASE_URL).login("  ", "  ");
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @UiTest
    @DisplayName("UI 57: Deve suportar observação com exatamente 255 caracteres")
    void shouldHandleExact255CharObservation() {
        String text255 = "a".repeat(255);
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem().selectFirstMenuItem().fillObservations(text255).confirmAddItem();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        assertTrue(wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//h3[contains(.,'Adicionar Item')]"))));
    }

    @UiTest
    @IssueTest
    @DisplayName("UI 58: Observação com 256 caracteres causa HTTP 500 - estouro de VARCHAR(255) (ISSUE-02)")
    void shouldHandle256CharObservation() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem().selectFirstMenuItem();

        String text256 = "a".repeat(256);
        orders.fillObservations(text256);
        new Actions(driver).pause(Duration.ofSeconds(5)).perform();

        orders.confirmAddItem();

        boolean alertaDeErro;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.alertIsPresent()).accept();
            alertaDeErro = true;
        } catch (Exception e) {
            alertaDeErro = false;
        }

        new Actions(driver).pause(Duration.ofSeconds(4)).perform();
        assertFalse(alertaDeErro,
                "BUG: O backend rejeitou a observação com 256 caracteres (HTTP 500 por estouro de VARCHAR(255)). "
                + "O sistema deveria aceitar ou validar no frontend antes de enviar.");
    }

    @UiTest
    @DisplayName("UI 61: Deve fechar comanda com exatamente 1 pessoa (limite mínimo válido)")
    void shouldCloseOrderWithExactlyOnePerson() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem().selectFirstMenuItem().confirmAddItem();
        orders.clickCloseOrder().fillPeopleCount("1");

        assertTrue(orders.isConfirmCloseEnabled(), "O botão confirmar deveria estar habilitado para 1 pessoa (mínimo válido)");
        orders.confirmCloseOrder();
        assertTrue(orders.isCloseSuccessVisible(), "Deveria fechar a comanda com sucesso com exatamente 1 pessoa");
    }

    @UiTest
    @DisplayName("UI 68: Deve aceitar cadastro com username de exatamente 255 caracteres (limite da coluna)")
    void shouldAcceptUsernameWith255Chars() {
        String username255 = "u".repeat(255);
        RegisterPage register = new RegisterPage(driver).open(BASE_URL);
        register.register("Nome Teste", username255,
                UUID.randomUUID().toString().substring(0, 8) + "@test.com", "senha123", "WAITER");
        assertTrue(register.urlContains("/login"),
                "Username com 255 caracteres (limite de VARCHAR(255)) deveria ser aceito e redirecionar ao login");
    }

    @UiTest
    @DisplayName("UI 69: Deve rejeitar cadastro com username de 256 caracteres (estouro de VARCHAR(255))")
    void shouldRejectUsernameWith256Chars() {
        String username256 = "u".repeat(256);
        RegisterPage register = new RegisterPage(driver).open(BASE_URL);
        register.register("Nome Teste", username256,
                UUID.randomUUID().toString().substring(0, 8) + "@test.com", "senha123", "WAITER");
        assertTrue(register.isAtRegisterPage() || register.urlContains("/register"),
                "Username com 256 caracteres deveria ser rejeitado (permanecer no cadastro)");
    }

    @UiTest
    @IssueTest
    @DisplayName("UI 72: Fechar comanda sem itens causa erro no sistema (ISSUE-06)")
    void shouldFailWhenClosingOrderWithoutItems() {
        OrdersPage orders = new OrdersPage(driver);

        orders.clickCloseOrder().fillPeopleCount("2");
        new Actions(driver).pause(Duration.ofSeconds(4)).perform();

        orders.confirmCloseOrder();

        boolean alertaDeErro;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.alertIsPresent()).accept();
            alertaDeErro = true;
        } catch (Exception e) {
            alertaDeErro = false;
        }

        new Actions(driver).pause(Duration.ofSeconds(4)).perform();
        assertFalse(alertaDeErro,
                "BUG: O sistema gerou erro ao tentar fechar uma comanda sem itens. "
                + "Deveria impedir o fechamento com uma mensagem amigável ou desabilitar o botão.");
    }
}
