package br.edu.ifsp.foodflow.app.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final String PATH = "/login";

    private final By usernameInput = By.id("username");
    private final By passwordInput = By.id("password");
    private final By submitButton = By.cssSelector("button[type='submit']");
    private final By errorMessage = By.cssSelector(".bg-red-50");
    private final By registerLink = By.linkText("Solicite seu cadastro");
    private final By welcomeTitle = By.xpath("//h2[contains(text(),'Bem-vindo de volta')]");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open(String baseUrl) {
        navigateTo(baseUrl + PATH);
        return this;
    }

    public LoginPage fillUsername(String username) {
        type(usernameInput, username);
        return this;
    }

    public LoginPage fillPassword(String password) {
        type(passwordInput, password);
        return this;
    }

    public void clickLogin() {
        click(submitButton);
    }

    public void login(String username, String password) {
        fillUsername(username);
        fillPassword(password);
        clickLogin();
    }

    public boolean isAtLoginPage() {
        return isVisible(welcomeTitle);
    }

    public boolean hasError() {
        return isVisible(errorMessage);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public void goToRegister() {
        click(registerLink);
    }

    public boolean urlContains(String fragment) {
        return waitForUrlContains(fragment);
    }
}
