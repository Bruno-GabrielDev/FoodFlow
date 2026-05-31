package br.edu.ifsp.foodflow.app.ui;

import br.edu.ifsp.foodflow.app.annotation.UiTest;
import br.edu.ifsp.foodflow.app.ui.pages.DashboardPage;
import br.edu.ifsp.foodflow.app.ui.pages.LoginPage;
import br.edu.ifsp.foodflow.app.util.UiTestHelper;
import org.junit.jupiter.api.DisplayName;
import org.openqa.selenium.Dimension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de UI focados em responsividade.
 * Verificam o comportamento do frontend em diferentes tamanhos de viewport:
 * mobile (375x667 ~ iPhone SE), tablet (768x1024 ~ iPad) e desktop (1920x1080).
 */
@DisplayName("Testes de UI - Responsividade")
class ResponsiveUiTest extends BaseWebTest {

    // Tamanhos de viewport baseados em dispositivos reais
    private static final Dimension MOBILE = new Dimension(375, 667);
    private static final Dimension TABLET = new Dimension(768, 1024);
    private static final Dimension DESKTOP = new Dimension(1920, 1080);

    @UiTest
    @DisplayName("Login deve ser renderizado e usável em viewport mobile (375x667)")
    void shouldRenderLoginOnMobile() {
        driver.manage().window().setSize(MOBILE);

        LoginPage login = new LoginPage(driver).open(BASE_URL);
        assertTrue(login.isAtLoginPage(),
                "A tela de login deveria ser visível em viewport mobile");
    }

    @UiTest
    @DisplayName("Login deve ser renderizado em viewport tablet (768x1024)")
    void shouldRenderLoginOnTablet() {
        driver.manage().window().setSize(TABLET);

        LoginPage login = new LoginPage(driver).open(BASE_URL);
        assertTrue(login.isAtLoginPage(),
                "A tela de login deveria ser visível em viewport tablet");
    }

    @UiTest
    @DisplayName("Dashboard deve listar mesas em viewport mobile")
    void shouldListTablesOnMobile() {
        driver.manage().window().setSize(MOBILE);
        UiTestHelper.loginViaUi(driver, BASE_URL);

        DashboardPage dashboard = new DashboardPage(driver);
        assertTrue(dashboard.countVisibleTables() > 0,
                "Mesas deveriam ser listadas mesmo em viewport mobile");
    }

    @UiTest
    @DisplayName("Dashboard deve listar mesas em viewport tablet")
    void shouldListTablesOnTablet() {
        driver.manage().window().setSize(TABLET);
        UiTestHelper.loginViaUi(driver, BASE_URL);

        DashboardPage dashboard = new DashboardPage(driver);
        assertTrue(dashboard.countVisibleTables() > 0,
                "Mesas deveriam ser listadas em viewport tablet");
    }

    @UiTest
    @DisplayName("Dashboard deve listar mesas em viewport desktop")
    void shouldListTablesOnDesktop() {
        driver.manage().window().setSize(DESKTOP);
        UiTestHelper.loginViaUi(driver, BASE_URL);

        DashboardPage dashboard = new DashboardPage(driver);
        assertTrue(dashboard.countVisibleTables() > 0,
                "Mesas deveriam ser listadas em viewport desktop");
    }

    @UiTest
    @DisplayName("Quantidade de mesas exibidas deve ser igual entre desktop e mobile")
    void shouldShowSameDataOnAllViewports() {
        UiTestHelper.loginViaUi(driver, BASE_URL);
        DashboardPage dashboard = new DashboardPage(driver);

        driver.manage().window().setSize(DESKTOP);
        int desktopCount = dashboard.countVisibleTables();

        driver.manage().window().setSize(MOBILE);
        int mobileCount = dashboard.countVisibleTables();

        assertEquals(desktopCount, mobileCount,
                "A quantidade de mesas exibidas deveria ser a mesma — apenas o layout muda");
    }

    @UiTest
    @DisplayName("Layout deve se adaptar ao redimensionar a tela em tempo real")
    void shouldAdaptOnRuntimeResize() {
        UiTestHelper.loginViaUi(driver, BASE_URL);
        DashboardPage dashboard = new DashboardPage(driver);

        driver.manage().window().setSize(DESKTOP);
        assertTrue(dashboard.isAtDashboardPage(),
                "Dashboard deveria continuar acessível em desktop");

        driver.manage().window().setSize(MOBILE);
        assertTrue(dashboard.isAtDashboardPage(),
                "Dashboard deveria continuar acessível após mudar para mobile");

        driver.manage().window().setSize(TABLET);
        assertTrue(dashboard.isAtDashboardPage(),
                "Dashboard deveria continuar acessível após mudar para tablet");
    }
}