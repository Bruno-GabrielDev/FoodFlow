package br.edu.ifsp.foodflow.app.ui;

import br.edu.ifsp.foodflow.app.annotation.UiTest;
import br.edu.ifsp.foodflow.app.ui.pages.DashboardPage;
import br.edu.ifsp.foodflow.app.ui.pages.LoginPage;
import br.edu.ifsp.foodflow.app.util.UiTestHelper;
import org.junit.jupiter.api.DisplayName;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de UI - Responsividade")
class ResponsiveUiTest extends BaseWebTest {

    private static final Dimension MOBILE = new Dimension(375, 667);
    private static final Dimension TABLET = new Dimension(768, 1024);
    private static final Dimension DESKTOP = new Dimension(1920, 1080);

    private static final Dimension MIN_VIEWPORT = new Dimension(320, 480);
    private static final Dimension MAX_VIEWPORT = new Dimension(2560, 1440);

    @UiTest
    @DisplayName("UI 45: Login deve ser renderizado e usável em viewport mobile (375x667)")
    void shouldRenderLoginOnMobile() {
        driver.manage().window().setSize(MOBILE);
        LoginPage login = new LoginPage(driver).open(BASE_URL);
        assertTrue(login.isAtLoginPage(), "A tela de login deveria ser visível em viewport mobile");
    }

    @UiTest
    @DisplayName("UI 46: Login deve ser renderizado em viewport tablet (768x1024)")
    void shouldRenderLoginOnTablet() {
        driver.manage().window().setSize(TABLET);
        LoginPage login = new LoginPage(driver).open(BASE_URL);
        assertTrue(login.isAtLoginPage(), "A tela de login deveria ser visível em viewport tablet");
    }

    @UiTest
    @DisplayName("UI 47: Dashboard deve listar mesas em viewport mobile")
    void shouldListTablesOnMobile() {
        driver.manage().window().setSize(MOBILE);
        UiTestHelper.loginViaUi(driver, BASE_URL);
        assertTrue(new DashboardPage(driver).countVisibleTables() > 0, "Mesas deveriam ser listadas mesmo em viewport mobile");
    }

    @UiTest
    @DisplayName("UI 48: Dashboard deve listar mesas em viewport tablet")
    void shouldListTablesOnTablet() {
        driver.manage().window().setSize(TABLET);
        UiTestHelper.loginViaUi(driver, BASE_URL);
        assertTrue(new DashboardPage(driver).countVisibleTables() > 0, "Mesas deveriam ser listadas em viewport tablet");
    }

    @UiTest
    @DisplayName("UI 49: Dashboard deve listar mesas em viewport desktop")
    void shouldListTablesOnDesktop() {
        driver.manage().window().setSize(DESKTOP);
        UiTestHelper.loginViaUi(driver, BASE_URL);
        assertTrue(new DashboardPage(driver).countVisibleTables() > 0, "Mesas deveriam ser listadas em viewport desktop");
    }

    @UiTest
    @DisplayName("UI 50: Quantidade de mesas exibidas deve ser igual entre desktop e mobile")
    void shouldShowSameDataOnAllViewports() {
        UiTestHelper.loginViaUi(driver, BASE_URL);
        DashboardPage dashboard = new DashboardPage(driver);
        driver.manage().window().setSize(DESKTOP);
        int desktopCount = dashboard.countVisibleTables();
        driver.manage().window().setSize(MOBILE);
        int mobileCount = dashboard.countVisibleTables();
        assertEquals(desktopCount, mobileCount, "A quantidade de mesas exibidas deveria ser a mesma");
    }

    @UiTest
    @DisplayName("UI 51: Layout deve se adaptar ao redimensionar a tela em tempo real")
    void shouldAdaptOnRuntimeResize() {
        UiTestHelper.loginViaUi(driver, BASE_URL);
        DashboardPage dashboard = new DashboardPage(driver);
        driver.manage().window().setSize(DESKTOP);
        assertTrue(dashboard.isAtDashboardPage(), "Dashboard deveria continuar acessível em desktop");
        driver.manage().window().setSize(MOBILE);
        assertTrue(dashboard.isAtDashboardPage(), "Dashboard deveria continuar acessível após mudar para mobile");
        driver.manage().window().setSize(TABLET);
        assertTrue(dashboard.isAtDashboardPage(), "Dashboard deveria continuar acessível após mudar para tablet");
    }

    @UiTest
    @DisplayName("UI 66: Deve renderizar login e dashboard no viewport MÍNIMO (320x480)")
    void shouldRenderOnMinimumViewport() {

        WebDriver headless = newHeadlessDriver(MIN_VIEWPORT);
        try {
            LoginPage login = new LoginPage(headless).open(BASE_URL);
            assertTrue(login.isAtLoginPage(), "O login deveria renderizar no viewport mínimo (320px)");

            UiTestHelper.loginViaUi(headless, BASE_URL);
            assertTrue(new DashboardPage(headless).countVisibleTables() > 0,
                    "As mesas deveriam ser listadas no viewport mínimo (320px)");
        } finally {
            headless.quit();
        }
    }

    @UiTest
    @DisplayName("UI 67: Deve renderizar login e dashboard no viewport MÁXIMO (2560x1440)")
    void shouldRenderOnMaximumViewport() {
        WebDriver headless = newHeadlessDriver(MAX_VIEWPORT);
        try {
            LoginPage login = new LoginPage(headless).open(BASE_URL);
            assertTrue(login.isAtLoginPage(), "O login deveria renderizar no viewport máximo (2560px)");

            UiTestHelper.loginViaUi(headless, BASE_URL);
            assertTrue(new DashboardPage(headless).countVisibleTables() > 0,
                    "As mesas deveriam ser listadas no viewport máximo (2560px)");
        } finally {
            headless.quit();
        }
    }

    private WebDriver newHeadlessDriver(Dimension size) {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-headless");
        options.addArguments("--width=" + size.getWidth(), "--height=" + size.getHeight());
        WebDriver d = new FirefoxDriver(options);
        d.manage().window().setSize(size);
        d.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        return d;
    }
}
