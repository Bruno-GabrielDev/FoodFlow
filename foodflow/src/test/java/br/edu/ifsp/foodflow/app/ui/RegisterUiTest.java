package br.edu.ifsp.foodflow.app.ui;

import br.edu.ifsp.foodflow.app.annotation.UiTest;
import br.edu.ifsp.foodflow.app.ui.pages.RegisterPage;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de UI - Tela de Cadastro")
class RegisterUiTest extends BaseWebTest {

    private final Faker faker = new Faker();

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @UiTest
    @DisplayName("UI 52: Deve cadastrar um novo usuário com dados válidos e redirecionar para o login")
    void shouldRegisterWithValidData() {
        String suffix = uniqueSuffix();
        RegisterPage registerPage = new RegisterPage(driver).open(BASE_URL);
        registerPage.register(faker.name().fullName(), "user_" + suffix, suffix + "@test.com", "senha123", "WAITER");
        assertTrue(registerPage.urlContains("/login"), "Deveria redirecionar para o login após cadastro válido");
    }

    @UiTest
    @DisplayName("UI 53: Deve permanecer na tela de cadastro ao informar email inválido")
    void shouldStayOnRegisterWithInvalidEmail() {
        String suffix = uniqueSuffix();
        RegisterPage registerPage = new RegisterPage(driver).open(BASE_URL);
        registerPage.fillName(faker.name().fullName()).fillUsername("user_" + suffix).fillEmail("email-invalido").fillPassword("senha123").selectRole("WAITER").clickRegister();
        assertTrue(registerPage.isAtRegisterPage(), "Deveria permanecer na tela de cadastro com email inválido");
    }

    @UiTest
    @DisplayName("UI 54: Deve permanecer na tela de cadastro ao submeter campos vazios")
    void shouldStayOnRegisterWhenFieldsAreEmpty() {
        RegisterPage registerPage = new RegisterPage(driver).open(BASE_URL);
        registerPage.clickRegister();
        assertTrue(registerPage.isAtRegisterPage(), "Deveria permanecer na tela de cadastro (campos obrigatórios)");
    }

    @UiTest
    @DisplayName("UI 55: Deve permitir selecionar diferentes cargos no cadastro")
    void shouldAllowSelectingDifferentRoles() {
        String suffix = uniqueSuffix();
        RegisterPage registerPage = new RegisterPage(driver).open(BASE_URL);
        registerPage.fillName(faker.name().fullName()).fillUsername("cook_" + suffix).fillEmail(suffix + "@test.com").fillPassword("senha123").selectRole("COOK").clickRegister();
        assertTrue(registerPage.urlContains("/login"), "Deveria cadastrar com o cargo COOK e ir para o login");
    }

    @UiTest
    @DisplayName("UI 56: Deve voltar para a tela de login ao clicar no link de retorno")
    void shouldNavigateBackToLogin() {
        RegisterPage registerPage = new RegisterPage(driver).open(BASE_URL);
        registerPage.goBackToLogin();
        assertTrue(registerPage.urlContains("/login"), "Deveria navegar de volta para o login");
    }
}
