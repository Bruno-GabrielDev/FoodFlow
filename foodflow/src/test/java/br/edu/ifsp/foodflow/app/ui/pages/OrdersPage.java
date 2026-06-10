package br.edu.ifsp.foodflow.app.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class OrdersPage extends BasePage {

    private static final String PATH = "/orders";

    private final By pageTitle = By.xpath("//h1[contains(text(),'Comandas Ativas')]");
    private final By searchInput = By.cssSelector("input[placeholder='Mesa ou garçom...']");
    private final By emptyStateTitle = By.xpath("//h3[contains(text(),'Nenhuma comanda ativa')]");

    private final By orderCards = By.xpath("//div[contains(@class,'rounded-3xl') and " +
            ".//h4[contains(text()," +
            "'Itens da Comanda')]]");
    private final By addItemButton = By.xpath("(//button[normalize-space()='Adicionar Item'])[1]");
    private final By closeOrderButton = By.xpath("(//button[normalize-space()='Fechar comanda'])[1]");
    private final By detailsButton = By.xpath("(//button[contains(.,'Detalhes')])[1]");

    private final By addItemModalTitle = By.xpath("//h3[normalize-space()='Adicionar Item']");
    private final By menuItemSearch = By.cssSelector("input[placeholder='Pesquise por nome ou descrição...']");
    private final By firstMenuItem = By.cssSelector("div.grid > button:first-of-type");
    private final By observationsTextarea = By.cssSelector("textarea[placeholder*='Ponto da carne']");
    private final By confirmAddItemButton = By.xpath("//button[contains(.,'Lançar na Comanda')]");

    private final By closeModalTitle = By.xpath("//h2[normalize-space()='Fechar comanda']");
    private final By peopleCountInput = By.cssSelector("input[type='number']");
    private final By confirmCloseButton = By.xpath("//button[normalize-space()='Confirmar']");
    private final By cancelCloseButton = By.xpath("//h2[normalize-space()='Fechar comanda']/following::" +
            "button" +
            "[normalize-space()='Cancelar'][1]");

    private final By closeSuccessTitle = By.xpath("//h2[contains(text(),'Comanda encerrada')]");
    private final By finalizeButton = By.xpath("//button[normalize-space()='Finalizar']");
    private final By perPersonValue = By.xpath("//span[normalize-space()='Por pessoa']/following-sibling::span");

    private final By detailsModalTitle = By.xpath("//h3[normalize-space()='Detalhes da Comanda']");

    private final By dashboardNavLink = By.xpath("//a[normalize-space()='Mesas']");

    public OrdersPage(WebDriver driver) {
        super(driver);
    }

    public OrdersPage open(String baseUrl) {
        navigateTo(baseUrl + PATH);
        return this;
    }

    public boolean isAtOrdersPage() {
        return isVisible(pageTitle);
    }

    public boolean isEmptyState() {
        return isVisible(emptyStateTitle);
    }

    public int countOrders() {
        waitVisible(pageTitle);
        return driver.findElements(orderCards).size();
    }

    public OrdersPage searchOrder(String text) {
        type(searchInput, text);
        return this;
    }

    public OrdersPage clickAddItem() {
        click(addItemButton);
        return this;
    }

    public boolean isAddItemModalVisible() {
        return isVisible(addItemModalTitle);
    }

    public OrdersPage searchMenuItem(String text) {
        type(menuItemSearch, text);
        return this;
    }

    public OrdersPage selectFirstMenuItem() {
        click(firstMenuItem);
        return this;
    }

    public OrdersPage fillObservations(String text) {
        type(observationsTextarea, text);
        return this;
    }

    public void confirmAddItem() {
        click(confirmAddItemButton);
    }

    public OrdersPage clickCloseOrder() {
        wait.until(d -> {
            try {
                d.findElement(closeOrderButton).click();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        return this;
    }

    public boolean isCloseModalVisible() {
        return isVisible(closeModalTitle);
    }

    public OrdersPage fillPeopleCount(String value) {
        type(peopleCountInput, value);
        return this;
    }

    public String getPeopleCountValue() {
        return driver.findElement(peopleCountInput).getAttribute("value");
    }

    public void confirmCloseOrder() {
        click(confirmCloseButton);
    }

    public boolean isConfirmCloseEnabled() {
        try {
            return driver.findElement(confirmCloseButton).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void cancelCloseOrder() {
        click(cancelCloseButton);
    }

    public boolean isCloseSuccessVisible() {
        return isVisible(closeSuccessTitle);
    }

    public void finalizeCloseSuccess() {
        click(finalizeButton);
    }

    public String getPerPersonValue() {
        return getText(perPersonValue);
    }

    public OrdersPage clickDetails() {
        click(detailsButton);
        return this;
    }

    public boolean isDetailsModalVisible() {
        return isVisible(detailsModalTitle);
    }

    public void goToDashboard() {
        click(dashboardNavLink);
        }

    public boolean urlContains(String fragment) {
        return waitForUrlContains(fragment);
    }
}
