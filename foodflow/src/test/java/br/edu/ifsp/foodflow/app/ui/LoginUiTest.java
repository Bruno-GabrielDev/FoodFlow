package br.edu.ifsp.foodflow.app.ui;

import br.edu.ifsp.foodflow.app.annotation.UiTest;
import br.edu.ifsp.foodflow.app.ui.pages.LoginPage;
import br.edu.ifsp.foodflow.app.util.AuthHelper;
import br.edu.ifsp.foodflow.app.util.UiTestHelper;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de UI - Tela de Login")
class LoginUiTest extends BaseWebTest {

    @UiTest
    @DisplayName("UI 14: Deve fazer login com credenciais válidas e redirecionar para o dashboard")
    void shouldLoginWithValidCredentials() {
        AuthHelper.RegisteredUser user = UiTestHelper.createUserViaApi();

        LoginPage loginPage = new LoginPage(driver).open(BASE_URL);
        loginPage.login(user.username(), user.password());

        boolean redirected = loginPage.urlContains("/dashboard");
        assertTrue(redirected, "Deveria redirecionar para o dashboard após login válido");
    }

    @UiTest
    @DisplayName("UI 15: Deve exibir mensagem de erro ao tentar login com senha incorreta")
    void shouldShowErrorWithWrongPassword() {
        AuthHelper.RegisteredUser user = UiTestHelper.createUserViaApi();

        LoginPage loginPage = new LoginPage(driver).open(BASE_URL);
        loginPage.login(user.username(), "senha-errada");

        assertTrue(loginPage.hasError(), "Deveria exibir mensagem de erro com senha incorreta");
    }

    @UiTest
    @DisplayName("UI 16: Deve exibir mensagem de erro ao tentar login com usuário inexistente")
    void shouldShowErrorWithNonExistentUser() {
        LoginPage loginPage = new LoginPage(driver).open(BASE_URL);
        loginPage.login("usuario_que_nao_existe_123", "qualquer");

        assertTrue(loginPage.hasError(), "Deveria exibir mensagem de erro com usuário inexistente");
    }

    @UiTest
    @DisplayName("UI 17: Deve permanecer na tela de login ao submeter sem preencher os campos")
    void shouldStayOnLoginWhenFieldsAreEmpty() {
        LoginPage loginPage = new LoginPage(driver).open(BASE_URL);
        loginPage.clickLogin();

        assertTrue(loginPage.isAtLoginPage(),
                "Deveria permanecer na tela de login (campos obrigatórios)");
    }

    @UiTest
    @DisplayName("UI 18: Deve navegar para a tela de cadastro ao clicar no link")
    void shouldNavigateToRegister() {
        LoginPage loginPage = new LoginPage(driver).open(BASE_URL);
        loginPage.goToRegister();

        boolean navigated = loginPage.urlContains("/register");
        assertTrue(navigated, "Deveria navegar para a tela de cadastro");
    }
}
