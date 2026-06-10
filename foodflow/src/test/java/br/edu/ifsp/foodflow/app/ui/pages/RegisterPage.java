package br.edu.ifsp.foodflow.app.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

public class RegisterPage extends BasePage {

    private static final String PATH = "/register";

    private final By nameInput = By.id("name");
    private final By usernameInput = By.id("username");
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By roleSelect = By.id("role");
    private final By submitButton = By.cssSelector("button[type='submit']");
    private final By errorMessage = By.cssSelector(".bg-red-50");
    private final By pageTitle = By.xpath("//h2[contains(text(),'Criar nova conta')]");
    private final By backToLoginLink = By.linkText("Voltar para o login");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public RegisterPage open(String baseUrl) {
        navigateTo(baseUrl + PATH);
        return this;
    }

    public RegisterPage fillName(String name) {
        type(nameInput, name);
        return this;
    }

    public RegisterPage fillUsername(String username) {
        type(usernameInput, username);
        return this;
    }

    public RegisterPage fillEmail(String email) {
        type(emailInput, email);
        return this;
    }

    public RegisterPage fillPassword(String password) {
        type(passwordInput, password);
        return this;
    }

    public RegisterPage selectRole(String roleValue) {
        Select select = new Select(waitVisible(roleSelect));
        select.selectByValue(roleValue);
        return this;
    }

    public void clickRegister() {
        click(submitButton);
    }

    public void register(String name, String username, String email, String password, String roleValue) {
        fillName(name);
        fillUsername(username);
        fillEmail(email);
        fillPassword(password);
        selectRole(roleValue);
        clickRegister();
    }

    public boolean isAtRegisterPage() {
        return isVisible(pageTitle);
    }

    public boolean hasError() {
        return isVisible(errorMessage);
    }

    public boolean urlContains(String fragment) {
        return waitForUrlContains(fragment);
    }

    public void goBackToLogin() {
        click(backToLoginLink);
    }
}
