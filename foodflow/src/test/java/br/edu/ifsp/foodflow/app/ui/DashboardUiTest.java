package br.edu.ifsp.foodflow.app.ui;

import br.edu.ifsp.foodflow.app.annotation.UiTest;
import br.edu.ifsp.foodflow.app.ui.pages.DashboardPage;
import br.edu.ifsp.foodflow.app.util.UiTestHelper;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de UI - Tela do Dashboard")
class DashboardUiTest extends BaseWebTest {

    @UiTest
    @DisplayName("Deve exibir o dashboard após login com sucesso")
    void shouldDisplayDashboardAfterLogin() {
        UiTestHelper.loginViaUi(driver, BASE_URL);

        DashboardPage dashboard = new DashboardPage(driver);
        assertTrue(dashboard.isAtDashboardPage(), "Deveria exibir a tela do dashboard");
    }

    @UiTest
    @DisplayName("Deve listar mesas no dashboard")
    void shouldListTables() {
        UiTestHelper.loginViaUi(driver, BASE_URL);

        DashboardPage dashboard = new DashboardPage(driver);
        int tableCount = dashboard.countVisibleTables();

        assertTrue(tableCount > 0, "Deveria exibir pelo menos uma mesa no dashboard");
    }

    @UiTest
    @DisplayName("Deve filtrar mesas por status 'Livre'")
    void shouldFilterByAvailable() {
        UiTestHelper.loginViaUi(driver, BASE_URL);

        DashboardPage dashboard = new DashboardPage(driver);
        int totalBefore = dashboard.countVisibleTables();

        dashboard.filterByAvailable();
        int filteredCount = dashboard.countVisibleTables();

        assertTrue(filteredCount <= totalBefore,
                "Após filtrar por 'Livre', a quantidade de mesas deveria ser menor ou igual ao total");
    }

    @UiTest
    @DisplayName("Deve filtrar mesas por status 'Ocupada'")
    void shouldFilterByOccupied() {
        UiTestHelper.loginViaUi(driver, BASE_URL);

        DashboardPage dashboard = new DashboardPage(driver);
        int totalBefore = dashboard.countVisibleTables();

        dashboard.filterByOccupied();
        int filteredCount = dashboard.countVisibleTables();

        assertTrue(filteredCount <= totalBefore,
                "Após filtrar por 'Ocupada', a quantidade de mesas deveria ser menor ou igual ao total");
    }

    @UiTest
    @DisplayName("Deve abrir o modal ao clicar em uma mesa disponível")
    void shouldOpenModalOnAvailableTableClick() {
        UiTestHelper.loginViaUi(driver, BASE_URL);

        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.filterByAvailable();
        dashboard.clickFirstAvailableTable();

        assertTrue(dashboard.isOpenOrderModalVisible(),
                "Deveria abrir o modal de 'Abrir Comanda' ao clicar em mesa disponível");
    }

    @UiTest
    @DisplayName("Deve fechar o modal ao clicar em 'Cancelar'")
    void shouldCloseModalOnCancel() {
        UiTestHelper.loginViaUi(driver, BASE_URL);

        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.filterByAvailable();
        dashboard.clickFirstAvailableTable();
        dashboard.cancelOpenOrder();

        assertFalse(dashboard.isOpenOrderModalVisible(),
                "O modal deveria fechar ao clicar em 'Cancelar'");
    }

    @UiTest
    @DisplayName("Deve abrir uma comanda e redirecionar para a tela de comandas")
    void shouldOpenOrderAndNavigateToOrders() {
        UiTestHelper.loginViaUi(driver, BASE_URL);

        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.filterByAvailable();
        dashboard.clickFirstAvailableTable();
        dashboard.confirmOpenOrder();

        boolean navigated = dashboard.urlContains("/orders");
        assertTrue(navigated, "Deveria navegar para a tela de comandas após abrir uma comanda");
    }

    @UiTest
    @DisplayName("Deve navegar para a tela de comandas pelo menu lateral")
    void shouldNavigateToOrdersViaSidebar() {
        UiTestHelper.loginViaUi(driver, BASE_URL);

        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.goToOrders();

        boolean navigated = dashboard.urlContains("/orders");
        assertTrue(navigated, "Deveria navegar para /orders pelo link da sidebar");
    }

    @UiTest
    @DisplayName("Deve fazer logout e voltar para a tela de login")
    void shouldLogoutAndReturnToLogin() {
        UiTestHelper.loginViaUi(driver, BASE_URL);

        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.logout();

        boolean navigated = dashboard.urlContains("/login");
        assertTrue(navigated, "Deveria voltar para o login após o logout");
    }
}