package br.edu.ifsp.foodflow.app.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object da tela de Login (/login).
 * Mapeia os campos de usuário, senha e o botão de entrar,
 * além de mensagens de erro e o link para a tela de cadastro.
 */
public class LoginPage extends BasePage {

    private static final String PATH = "/login";

    // Locators
    private final By usernameInput = By.id("username");
    private final By passwordInput = By.id("password");
    private final By submitButton = By.cssSelector("button[type='submit']");
    private final By errorMessage = By.cssSelector(".bg-red-50");
    private final By registerLink = By.linkText("Solicite seu cadastro");
    private final By welcomeTitle = By.xpath("//h2[contains(text(),'Bem-vindo de volta')]");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Abre a tela de login.
     */
    public LoginPage open(String baseUrl) {
        navigateTo(baseUrl + PATH);
        return this;
    }

    /**
     * Preenche o campo de usuário.
     */
    public LoginPage fillUsername(String username) {
        type(usernameInput, username);
        return this;
    }

    /**
     * Preenche o campo de senha.
     */
    public LoginPage fillPassword(String password) {
        type(passwordInput, password);
        return this;
    }

    /**
     * Clica no botão de login.
     */
    public void clickLogin() {
        click(submitButton);
    }

    /**
     * Realiza o fluxo completo de login.
     */
    public void login(String username, String password) {
        fillUsername(username);
        fillPassword(password);
        clickLogin();
    }

    /**
     * Verifica se a tela de login está sendo exibida.
     */
    public boolean isAtLoginPage() {
        return isVisible(welcomeTitle);
    }

    /**
     * Verifica se uma mensagem de erro está visível.
     */
    public boolean hasError() {
        return isVisible(errorMessage);
    }

    /**
     * Retorna o texto da mensagem de erro.
     */
    public String getErrorMessage() {
        return getText(errorMessage);
    }

    /**
     * Navega para a tela de cadastro pelo link.
     */
    public void goToRegister() {
        click(registerLink);
    }

    /**
     * Espera a URL conter o trecho informado (exposto para os testes).
     */
    public boolean urlContains(String fragment) {
        return waitForUrlContains(fragment);
    }
}