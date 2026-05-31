package br.edu.ifsp.foodflow.app.ui;

import br.edu.ifsp.foodflow.app.annotation.UiTest;
import br.edu.ifsp.foodflow.app.ui.pages.LoginPage;
import br.edu.ifsp.foodflow.app.util.UiTestHelper;
import org.junit.jupiter.api.DisplayName;
import org.openqa.selenium.JavascriptExecutor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de UI focados em segurança e controle de acesso.
 * Verificam se o frontend protege rotas privadas e se comporta corretamente
 * em cenários de sessão inválida, token manipulado e logout.
 */
@DisplayName("Testes de UI - Segurança e Controle de Acesso")
class SecurityUiTest extends BaseWebTest {

    @UiTest
    @DisplayName("Deve redirecionar para login ao acessar /dashboard sem autenticação")
    void shouldRedirectToLoginWhenAccessingDashboardWithoutAuth() {
        driver.navigate().to(BASE_URL + "/dashboard");

        LoginPage login = new LoginPage(driver);
        boolean redirected = login.urlContains("/login");

        assertTrue(redirected,
                "Usuário não autenticado deveria ser redirecionado para /login");
    }

    @UiTest
    @DisplayName("Deve redirecionar para login ao acessar /orders sem autenticação")
    void shouldRedirectToLoginWhenAccessingOrdersWithoutAuth() {
        driver.navigate().to(BASE_URL + "/orders");

        LoginPage login = new LoginPage(driver);
        boolean redirected = login.urlContains("/login");

        assertTrue(redirected,
                "Usuário não autenticado deveria ser redirecionado para /login");
    }

    @UiTest
    @DisplayName("Deve invalidar sessão quando o token é apagado do localStorage")
    void shouldInvalidateSessionWhenTokenIsRemoved() {
        UiTestHelper.loginViaUi(driver, BASE_URL);

        // Apaga o token armazenado no navegador, simulando sessão perdida
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");

        // Tenta navegar para uma rota protegida
        driver.navigate().to(BASE_URL + "/orders");

        LoginPage login = new LoginPage(driver);
        boolean redirected = login.urlContains("/login");

        assertTrue(redirected,
                "Após limpar o localStorage, o usuário deveria ser redirecionado para o login");
    }

    @UiTest
    @DisplayName("Deve invalidar sessão quando o token é manipulado/corrompido")
    void shouldInvalidateSessionWhenTokenIsTampered() {
        UiTestHelper.loginViaUi(driver, BASE_URL);

        // Sobrescreve o token armazenado pelo frontend com um valor inválido
        ((JavascriptExecutor) driver).executeScript(
                "window.localStorage.setItem('@FoodFlow:token', 'token-falso-malicioso-123');"
        );

        driver.navigate().to(BASE_URL + "/orders");
        ((JavascriptExecutor) driver).executeScript("location.reload();");

        LoginPage login = new LoginPage(driver);
        boolean redirected = login.urlContains("/login");

        assertTrue(redirected,
                "Token inválido deveria invalidar a sessão e redirecionar ao login");
    }

    @UiTest
    @DisplayName("Não deve manter acesso após logout ao tentar voltar pela URL")
    void shouldNotAllowAccessAfterLogoutViaUrl() {
        UiTestHelper.loginViaUi(driver, BASE_URL);

        // Faz logout manualmente limpando o storage
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");

        // Tenta forçar a navegação para uma rota protegida
        driver.navigate().to(BASE_URL + "/dashboard");

        LoginPage login = new LoginPage(driver);
        boolean redirected = login.urlContains("/login");

        assertTrue(redirected,
                "Após logout, mesmo tentando acessar pela URL não deveria permitir entrada");
    }

    @UiTest
    @DisplayName("Login não deve aceitar credenciais com tentativa de SQL injection")
    void shouldNotAcceptSqlInjectionAttempt() {
        LoginPage login = new LoginPage(driver).open(BASE_URL);
        login.login("' OR '1'='1", "' OR '1'='1");

        // Não deve logar — usuário deve permanecer em /login
        boolean stillOnLogin = login.urlContains("/login");
        assertTrue(stillOnLogin,
                "Login não deveria aceitar tentativa de SQL injection");
    }
}