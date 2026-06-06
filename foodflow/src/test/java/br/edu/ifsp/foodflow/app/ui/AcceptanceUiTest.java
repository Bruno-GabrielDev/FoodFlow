package br.edu.ifsp.foodflow.app.ui;

import br.edu.ifsp.foodflow.app.annotation.UiTest;
import br.edu.ifsp.foodflow.app.ui.pages.DashboardPage;
import br.edu.ifsp.foodflow.app.ui.pages.OrdersPage;
import br.edu.ifsp.foodflow.app.util.AuthHelper;
import br.edu.ifsp.foodflow.app.util.OrderTestHelper;
import br.edu.ifsp.foodflow.app.util.UiTestHelper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de UI - Fluxos de Aceitação e Robustez")
class AcceptanceUiTest extends BaseWebTest {

    private String token;
    private String userId;
    private String orderId;

    @BeforeEach
    void setup() {
        AuthHelper.RegisteredUser user = UiTestHelper.loginViaUi(driver, BASE_URL);
        Response loginResponse = given().contentType(ContentType.JSON)
                .body(Map.of("username", user.username(), "password", user.password()))
                .when().post("/auth/login").then().statusCode(200).extract().response();

        this.token = loginResponse.path("token");
        this.userId = loginResponse.path("userId");
    }

    @UiTest
    @DisplayName("UI 10: Deve abrir uma nova comanda através do Dashboard")
    void shouldOpenNewOrderFromDashboard() {
        DashboardPage dashboard = new DashboardPage(driver).open(BASE_URL);
        assertTrue(dashboard.isAtDashboardPage(), "Deveria estar na página de Dashboard");
        dashboard.filterByAvailable().clickFirstAvailableTable();
        assertTrue(dashboard.isOpenOrderModalVisible(), "O modal de abertura de comanda deveria aparecer");
        dashboard.confirmOpenOrder();
        assertTrue(dashboard.urlContains("/orders"), "Deveria redirecionar para a tela de comandas");
        assertTrue(new OrdersPage(driver).countOrders() >= 1, "Deveria listar a nova comanda aberta");
    }

    @UiTest
    @DisplayName("UI 11: Deve lançar item com adicionais e validar o processo")
    void shouldAddItemWithAddOns() {
        int tableNumber = OrderTestHelper.getAvailableTableNumber(token);
        this.orderId = OrderTestHelper.openOrder(token, tableNumber, userId);
        OrdersPage orders = new OrdersPage(driver).open(BASE_URL);
        orders.clickAddItem();
        assertTrue(orders.isAddItemModalVisible(), "Modal de adicionar item deve estar visível");
        orders.selectFirstMenuItem();
        String obs = "Sem cebola. Adicionar molho especial.";
        orders.fillObservations(obs).confirmAddItem();
        assertFalse(orders.isAddItemModalVisible(), "O modal deveria fechar após o lançamento");
        orders.clickDetails();
        assertTrue(driver.getPageSource().contains(obs), "A observação do item deve estar visível nos detalhes");
    }

    @UiTest
    @DisplayName("UI 12: Deve evitar duplicidade de itens ao clicar rapidamente")
    void shouldPreventDoubleItemSubmission() {
        int tableNumber = OrderTestHelper.getAvailableTableNumber(token);
        this.orderId = OrderTestHelper.openOrder(token, tableNumber, userId);
        OrdersPage orders = new OrdersPage(driver).open(BASE_URL);
        orders.clickAddItem().selectFirstMenuItem();
        
        WebElement submitBtn = driver.findElement(By.xpath("//button[contains(.,'Lançar na Comanda')]"));
        
        submitBtn.click();
        try { submitBtn.click(); } catch (Exception ignored) {}
        
        orders.urlContains("/orders");
        orders.clickDetails();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        List<WebElement> items = wait.until(d -> d.findElements(By.xpath("//h5[contains(@class,'font-bold')]")));
        
        assertEquals(1, items.size(), "FALHA SISTÊMICA: O sistema permitiu lançar o mesmo item duas vezes!");
    }

    @UiTest
    @DisplayName("UI 13: Deve limpar o estado do modal ao abrir novamente")
    void shouldResetModalStateBetweenUses() {
        OrdersPage orders = new OrdersPage(driver).open(BASE_URL);
        orders.clickAddItem().searchMenuItem("Coca-Cola");
        driver.findElement(By.cssSelector("button .rotate-45")).click(); 
        orders.clickAddItem();
        String currentValue = driver.findElement(By.cssSelector("input[placeholder*='Pesquise']")).getAttribute("value");
        assertEquals("", currentValue, "FALHA DE UX: O modal não limpou a pesquisa anterior ao ser reaberto!");
    }

    @UiTest
    @DisplayName("UI 59: Deve suportar uma comanda com grande volume de itens (50+)")
    void shouldHandleLargeVolumeOfItems() {
        int tableNumber = OrderTestHelper.getAvailableTableNumber(token);
        this.orderId = OrderTestHelper.openOrder(token, tableNumber, userId);
        
        OrdersPage orders = new OrdersPage(driver).open(BASE_URL);
        
        // Simula a adição de 50 itens via INTERFACE (UI)
        for(int i = 0; i < 50; i++) {
            orders.clickAddItem()
                  .selectFirstMenuItem()
                  .confirmAddItem();
            
            // Lida com possíveis popups de confirmação ou espera o modal fechar para o próximo
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//h3[contains(.,'Adicionar Item')]")));
        }

        orders.clickDetails();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        List<WebElement> items = wait.until(d -> d.findElements(By.xpath("//h5[contains(@class,'font-bold')]")));
        assertTrue(items.size() >= 50, "A UI deveria listar todos os 50 itens lançados manualmente. Encontrados: " + items.size());
    }

    @UiTest
    @DisplayName("UI 60: Deve formatar corretamente valores monetários elevados (Limite Financeiro)")
    void shouldFormatHighCurrencyValues() {
        int tableNumber = OrderTestHelper.getAvailableTableNumber(token);
        this.orderId = OrderTestHelper.openOrder(token, tableNumber, userId);
        
        OrdersPage orders = new OrdersPage(driver).open(BASE_URL);
        orders.clickAddItem().selectFirstMenuItem().confirmAddItem();
        
        String menuItemId = OrderTestHelper.getFirstMenuItemId(token);
        for(int i = 0; i < 20; i++) {
            OrderTestHelper.addItem(token, orderId, menuItemId, userId);
        }
        
        driver.navigate().refresh();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        boolean currencyVisible = wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "R$"));
        assertTrue(currencyVisible, "O sistema deve exibir valores em formato monetário (R$)");
    }

    @AfterEach
    void cleanup() {
        if (token != null && orderId != null) {
            try {
                OrderTestHelper.addItem(token, orderId, OrderTestHelper.getFirstMenuItemId(token), userId);
                given().header("Authorization", "Bearer " + token).contentType(ContentType.JSON)
                    .body(Map.of("numberOfPeople", 1)).when().post("/orders/" + orderId + "/close");
            } catch (Exception ignored) {}
        }
    }
}
