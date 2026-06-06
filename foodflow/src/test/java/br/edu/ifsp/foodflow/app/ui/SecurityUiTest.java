package br.edu.ifsp.foodflow.app.ui;

import br.edu.ifsp.foodflow.app.annotation.UiTest;
import br.edu.ifsp.foodflow.app.ui.pages.LoginPage;
import br.edu.ifsp.foodflow.app.ui.pages.OrdersPage;
import br.edu.ifsp.foodflow.app.util.AuthHelper;
import br.edu.ifsp.foodflow.app.util.OrderTestHelper;
import br.edu.ifsp.foodflow.app.util.UiTestHelper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de UI focados em segurança e controle de acesso.
 * Verificam se o frontend protege rotas privadas e se comporta corretamente
 * em cenários de sessão inválida, token manipulado, logout e ataques de injeção.
 */
@DisplayName("Testes de UI - Segurança e Controle de Acesso")
class SecurityUiTest extends BaseWebTest {

    private String token;
    private String orderId;

    @UiTest
    @DisplayName("UI 01: Deve redirecionar para login ao acessar /dashboard sem autenticação")
    void shouldRedirectToLoginWhenAccessingDashboardWithoutAuth() {
        driver.navigate().to(BASE_URL + "/dashboard");
        LoginPage login = new LoginPage(driver);
        assertTrue(login.urlContains("/login"), "Usuário não autenticado deveria ser redirecionado para /login");
    }

    @UiTest
    @DisplayName("UI 02: Deve redirecionar para login ao acessar /orders sem autenticação")
    void shouldRedirectToLoginWhenAccessingOrdersWithoutAuth() {
        driver.navigate().to(BASE_URL + "/orders");
        LoginPage login = new LoginPage(driver);
        assertTrue(login.urlContains("/login"), "Usuário não autenticado deveria ser redirecionado para /login");
    }

    @UiTest
    @DisplayName("UI 03: Deve invalidar sessão quando o token é apagado do localStorage")
    void shouldInvalidateSessionWhenTokenIsRemoved() {
        UiTestHelper.loginViaUi(driver, BASE_URL);
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
        driver.navigate().to(BASE_URL + "/orders");
        LoginPage login = new LoginPage(driver);
        assertTrue(login.urlContains("/login"), "Após limpar o localStorage, o usuário deveria ser redirecionado para le login");
    }

    @UiTest
    @DisplayName("UI 04: Deve invalidar sessão quando o token é manipulado/corrompido")
    void shouldInvalidateSessionWhenTokenIsTampered() {
        UiTestHelper.loginViaUi(driver, BASE_URL);
        ((JavascriptExecutor) driver).executeScript(
                "window.localStorage.setItem('@FoodFlow:token', 'token-falso-malicioso-123');"
        );
        driver.navigate().to(BASE_URL + "/orders");
        ((JavascriptExecutor) driver).executeScript("location.reload();");
        LoginPage login = new LoginPage(driver);
        assertTrue(login.urlContains("/login"), "Token inválido deveria invalidar a sessão e redirecionar ao login");
    }

    @UiTest
    @DisplayName("UI 05: Não deve manter acesso após logout ao tentar voltar pela URL")
    void shouldNotAllowAccessAfterLogoutViaUrl() {
        UiTestHelper.loginViaUi(driver, BASE_URL);
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
        driver.navigate().to(BASE_URL + "/dashboard");
        LoginPage login = new LoginPage(driver);
        assertTrue(login.urlContains("/login"), "Após logout, mesmo tentando acessar pela URL não deveria permitir entrada");
    }

    @UiTest
    @DisplayName("UI 06: Login não deve aceitar credenciais com tentativa de SQL injection")
    void shouldNotAcceptSqlInjectionAttempt() {
        LoginPage loginPage = new LoginPage(driver).open(BASE_URL);
        loginPage.login("' OR '1'='1", "' OR '1'='1");
        assertTrue(loginPage.isAtLoginPage(), "O sistema deve permanecer na tela de login");
        assertFalse(driver.getCurrentUrl().contains("/dashboard"), "O usuário JAMAIS deveria ser redirecionado para o dashboard");
    }

    @UiTest
    @DisplayName("UI 07: Deve sanitizar HTML Injection (Doom) nas observações do pedido")
    void shouldSanitizeDoomHtmlInjection() {
        AuthHelper.RegisteredUser user = UiTestHelper.loginViaUi(driver, BASE_URL);
        Response loginResponse = given().contentType(ContentType.JSON)
                .body(Map.of("username", user.username(), "password", user.password()))
                .when().post("/auth/login").then().statusCode(200).extract().response();

        this.token = loginResponse.path("token");
        String userId = loginResponse.path("userId");
        int table = OrderTestHelper.getAvailableTableNumber(token);
        this.orderId = OrderTestHelper.openOrder(token, table, userId);

        driver.navigate().to(BASE_URL + "/orders");
        OrdersPage ordersPage = new OrdersPage(driver);
        String doomPayload = "<h3>DOOM INJECTED!</h3><iframe src=\"https://dos.zone/player/https%3A%2F%2Fdos.zone%2Fclones%2Fdoom%2F\" width=\"600\" height=\"400\"></iframe>";

        ordersPage.clickAddItem().selectFirstMenuItem().fillObservations(doomPayload).confirmAddItem();
        ordersPage.clickDetails();
        
        int iframesCount = driver.findElements(By.tagName("iframe")).size();
        assertEquals(0, iframesCount, "O sistema renderizou o iframe do Doom! Vulnerabilidade detectada.");
    }

    @UiTest
    @DisplayName("UI 08: Deve redirecionar para login ao perder o token durante uma ação")
    void shouldRedirectToLoginOnTokenLossDuringAction() {
        AuthHelper.RegisteredUser user = UiTestHelper.loginViaUi(driver, BASE_URL);
        Response loginResponse = given().contentType(ContentType.JSON)
                .body(Map.of("username", user.username(), "password", user.password()))
                .when().post("/auth/login").then().statusCode(200).extract().response();

        this.token = loginResponse.path("token");
        String userId = loginResponse.path("userId");
        int table = OrderTestHelper.getAvailableTableNumber(token);
        this.orderId = OrderTestHelper.openOrder(token, table, userId);

        driver.navigate().to(BASE_URL + "/orders");
        OrdersPage ordersPage = new OrdersPage(driver);
        ordersPage.clickAddItem().selectFirstMenuItem();

        ((JavascriptExecutor) driver).executeScript("window.localStorage.setItem('@FoodFlow:token', 'token-expirado');");
        ordersPage.confirmAddItem();

        try {
            org.openqa.selenium.support.ui.WebDriverWait alertWait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5));
            alertWait.until(org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}

        assertTrue(ordersPage.urlContains("/login"), "Deveria ter redirecionado para /login ao detectar token inválido");
    }

    @UiTest
    @DisplayName("UI 09: Deve impedir IDOR (Garçom A tentando manipular comanda do Garçom B)")
    void shouldPreventIdorAttackBetweenWaiters() {
        OrderTestHelper.AuthenticatedUser waiterA = OrderTestHelper.registerAndAuthenticate();
        String orderIdA = OrderTestHelper.openOrder(waiterA.token(), OrderTestHelper.getAvailableTableNumber(waiterA.token()), waiterA.userId());

        AuthHelper.RegisteredUser waiterBData = UiTestHelper.loginViaUi(driver, BASE_URL);
        Response loginB = given().contentType(ContentType.JSON)
                .body(Map.of("username", waiterBData.username(), "password", waiterBData.password()))
                .when().post("/auth/login").then().extract().response();
        
        String tokenB = loginB.path("token");
        String userIdB = loginB.path("userId");

        Response attack = given().header("Authorization", "Bearer " + tokenB).contentType(ContentType.JSON)
                .body(Map.of("menuItemId", OrderTestHelper.getFirstMenuItemId(waiterA.token()), "quantity", 1, "waiterId", userIdB))
                .when().post("/orders/" + orderIdA + "/items");

        assertNotEquals(201, attack.getStatusCode(), "VULNERABILIDADE DETECTADA: Garçom B conseguiu manipular comanda do Garçom A!");
        
        OrderTestHelper.addItem(waiterA.token(), orderIdA, OrderTestHelper.getFirstMenuItemId(waiterA.token()), waiterA.userId());
        given().header("Authorization", "Bearer " + waiterA.token()).contentType(ContentType.JSON).body(Map.of("numberOfPeople", 1)).post("/orders/" + orderIdA + "/close");
    }

    @AfterEach
    void cleanup() {
        if (token != null && orderId != null) {
            try {
                given().header("Authorization", "Bearer " + token).contentType(ContentType.JSON)
                    .body(Map.of("numberOfPeople", 1)).when().post("/orders/" + orderId + "/close");
            } catch (Exception ignored) {}
        }
    }
}
