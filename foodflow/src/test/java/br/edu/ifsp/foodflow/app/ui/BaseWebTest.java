package br.edu.ifsp.foodflow.app.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * Classe base para os testes de UI.
 * Configura o WebDriver do Firefox antes de cada teste e o encerra ao final.
 * Usa esperas implícitas mínimas; as esperas explícitas ficam nos Page Objects.
 */
public abstract class BaseWebTest {

    protected WebDriver driver;

    protected static final String BASE_URL = "http://localhost:5173";

    @BeforeEach
    void setupDriver() {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();
        // Descomente a linha abaixo para rodar sem abrir a janela (headless)
        // options.addArguments("-headless");
        options.addArguments("--window-size=1920,1080");

        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterEach
    void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}