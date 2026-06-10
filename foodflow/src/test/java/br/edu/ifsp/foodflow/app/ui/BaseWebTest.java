package br.edu.ifsp.foodflow.app.ui;

import br.edu.ifsp.foodflow.app.util.DbTestHelper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

public abstract class BaseWebTest {

    protected WebDriver driver;

    protected static final String BASE_URL = "http://localhost:5173";

    @BeforeAll
    static void resetDatabaseBeforeClass() {

        DbTestHelper.resetDatabase();
    }

    @BeforeEach
    void setupDriver() {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();

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
