package br.edu.ifsp.foodflow.app.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object da tela do Dashboard (/dashboard).
 * Permite filtrar mesas por status, selecionar mesas, abrir comandas
 * e navegar para outras seções do sistema.
 */
public class DashboardPage extends BasePage {

    private static final String PATH = "/dashboard";

    // Locators
    private final By pageTitle = By.xpath("//h1[contains(text(),'Visão Geral')]");
    private final By searchInput = By.cssSelector("input[placeholder='Buscar mesa...']");

    // Filtros
    private final By filterAvailable = By.xpath("//button[normalize-space()='Livre']");
    private final By filterOccupied = By.xpath("//button[normalize-space()='Ocupada']");
    private final By filterReserved = By.xpath("//button[normalize-space()='Reservada']");

    // Cards de mesa
    private final By tableCards = By.cssSelector("button.group");
    private final By availableTableCard = By.xpath("//button[.//p[contains(text(),'Disponível')]][1]");

    // Modal de abrir comanda
    private final By openOrderModalTitle = By.xpath("//h3[contains(text(),'Abrir Mesa')]");
    private final By confirmOpenOrderButton = By.xpath("//button[contains(text(),'Sim, Abrir Comanda')]");
    private final By cancelOpenOrderButton = By.xpath("//button[contains(text(),'Cancelar')]");

    // Navegação
    private final By ordersNavLink = By.xpath("//a[normalize-space()='Comandas']");
    private final By logoutButton = By.xpath("//button[normalize-space()='Sair']");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Abre o dashboard. Requer usuário autenticado.
     */
    public DashboardPage open(String baseUrl) {
        navigateTo(baseUrl + PATH);
        return this;
    }

    public boolean isAtDashboardPage() {
        return isVisible(pageTitle);
    }

    /**
     * Espera carregar a página e retorna a quantidade de cards de mesa visíveis.
     */
    public int countVisibleTables() {
        waitVisible(pageTitle);
        return driver.findElements(tableCards).size();
    }

    public DashboardPage filterByAvailable() {
        click(filterAvailable);
        return this;
    }

    public DashboardPage filterByOccupied() {
        click(filterOccupied);
        return this;
    }

    public DashboardPage filterByReserved() {
        click(filterReserved);
        return this;
    }

    /**
     * Clica na primeira mesa com status 'Disponível'.
     */
    public DashboardPage clickFirstAvailableTable() {
        click(availableTableCard);
        return this;
    }

    /**
     * Clica numa mesa pelo número (ex: 7).
     */
    public DashboardPage clickTableByNumber(int number) {
        By locator = By.xpath("//button[.//span[contains(text(),'#" + number + "')]]");
        click(locator);
        return this;
    }

    /**
     * Verifica se o modal de abrir comanda está visível.
     */
    public boolean isOpenOrderModalVisible() {
        return isVisible(openOrderModalTitle);
    }

    /**
     * Confirma a abertura da comanda no modal.
     */
    public void confirmOpenOrder() {
        click(confirmOpenOrderButton);
    }

    /**
     * Cancela a abertura da comanda no modal.
     */
    public void cancelOpenOrder() {
        click(cancelOpenOrderButton);
    }

    /**
     * Vai para a tela de Comandas pelo menu lateral.
     */
    public void goToOrders() {
        click(ordersNavLink);
    }

    /**
     * Faz logout.
     */
    public void logout() {
        click(logoutButton);
    }

    /**
     * Espera a URL conter o trecho informado (exposto para os testes).
     */
    public boolean urlContains(String fragment) {
        return waitForUrlContains(fragment);
    }
}