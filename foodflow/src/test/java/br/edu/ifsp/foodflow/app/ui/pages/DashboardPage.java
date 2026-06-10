package br.edu.ifsp.foodflow.app.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {

    private static final String PATH = "/dashboard";

    private final By pageTitle = By.xpath("//h1[contains(text(),'Visão Geral')]");
    private final By searchInput = By.cssSelector("input[placeholder='Buscar mesa...']");

    private final By filterAvailable = By.xpath("//button[normalize-space()='Livre']");
    private final By filterOccupied = By.xpath("//button[normalize-space()='Ocupada']");
    private final By filterReserved = By.xpath("//button[normalize-space()='Reservada']");

    private final By tableCards = By.cssSelector("button.group");
    private final By availableTableCard = By.xpath("//button[.//p[contains(text(),'Disponível')]][1]");

    private final By openOrderModalTitle = By.xpath("//h3[contains(text(),'Abrir Mesa')]");
    private final By confirmOpenOrderButton = By.xpath("//button[contains(text(),'Sim, Abrir Comanda')]");
    private final By cancelOpenOrderButton = By.xpath("//button[contains(text(),'Cancelar')]");

    private final By ordersNavLink = By.xpath("//a[normalize-space()='Comandas']");
    private final By logoutButton = By.xpath("//button[normalize-space()='Sair']");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public DashboardPage open(String baseUrl) {
        navigateTo(baseUrl + PATH);
        return this;
    }

    public boolean isAtDashboardPage() {
        return isVisible(pageTitle);
    }

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

    public DashboardPage clickFirstAvailableTable() {
        wait.until(d -> {
            try {
                d.findElement(availableTableCard).click();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        return this;
    }

    public DashboardPage clickTableByNumber(int number) {
        By locator = By.xpath("//button[.//span[contains(text(),'#" + number + "')]]");
        click(locator);
        return this;
    }

    public boolean isOpenOrderModalVisible() {
        return isVisible(openOrderModalTitle);
    }

    public void confirmOpenOrder() {
        click(confirmOpenOrderButton);
    }

    public void cancelOpenOrder() {
        click(cancelOpenOrderButton);
    }

    public void goToOrders() {
        click(ordersNavLink);
    }

    public void logout() {
        click(logoutButton);
    }

    public boolean urlContains(String fragment) {
        return waitForUrlContains(fragment);
    }
}
