package br.edu.ifsp.foodflow.app.ui;

import br.edu.ifsp.foodflow.app.annotation.UiTest;
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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstração das últimas falhas sistêmicas (UTF-8 e Layout sob Carga).
 * Utiliza as três formas de espera citadas no material: Implícita, Explícita e Fluente.
 */
@DisplayName("Demonstração de Issues Finais - Etapa 4")
class IssueDemonstrationUiTest extends BaseWebTest {

    private String token;
    private String userId;
    private String orderId;

    @BeforeEach
    void setup() {
        // 1. ESPERA IMPLÍCITA (Configurada globalmente para o driver)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        AuthHelper.RegisteredUser user = UiTestHelper.loginViaUi(driver, BASE_URL);
        Response loginResponse = given().contentType(ContentType.JSON)
                .body(Map.of("username", user.username(), "password", user.password()))
                .when().post("/auth/login").then().statusCode(200).extract().response();

        this.token = loginResponse.path("token");
        this.userId = loginResponse.path("userId");
    }

    private void visualPause() {
        try { Thread.sleep(3000L); } catch (InterruptedException ignored) {}
    }

    @UiTest
    @DisplayName("Issue: Falha de Suporte a Caracteres Especiais (Acentuação)")
    void demoSpecialCharactersBug() {
        /*
         REPRODUÇÃO: Tentar salvar observações com caracteres acentuados.
         ERRO: O sistema não processa a requisição e o modal fica travado.
        */
        this.orderId = OrderTestHelper.openOrder(token, OrderTestHelper.getAvailableTableNumber(token), userId);
        OrdersPage orders = new OrdersPage(driver).open(BASE_URL);
        
        // 2. ESPERA EXPLÍCITA (Para garantir que o botão está pronto para clique)
        WebDriverWait explicitWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        explicitWait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Adicionar Item')]")));
        
        orders.clickAddItem().selectFirstMenuItem();
        
        String payload = "Atenção: Carne mal-passada! á é í ó ú ç ã ñ";
        orders.fillObservations(payload);

        orders.confirmAddItem();

        // Pausa Visual: Permite ver que o modal NÃO FECHOU apesar do clique.
        visualPause();

        assertTrue(orders.isAddItemModalVisible(), "BUG: O modal deveria ter fechado.");
    }

    @UiTest
    @DisplayName("Issue: Quebra de Layout e Performance (Grande Volume)")
    void demoLayoutVolumeBug() {
        /*
         REPRODUÇÃO: Exibir comanda com 50 itens.
         ERRO: Componentes visuais perdem formatação e o 'R$' desaparece.
        */
        this.orderId = OrderTestHelper.openOrder(token, OrderTestHelper.getAvailableTableNumber(token), userId);
        String menuItemId = OrderTestHelper.getFirstMenuItemId(token);

        for(int i = 0; i < 50; i++) {
            OrderTestHelper.addItem(token, orderId, menuItemId, userId);
        }

        OrdersPage orders = new OrdersPage(driver).open(BASE_URL);
        orders.clickDetails();
        
        // 3. ESPERA FLUENTE (Para aguardar a renderização da lista massiva de 50 itens)
        Wait<WebDriver> fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(15))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class);

        WebElement itemsList = fluentWait.until(d -> d.findElement(By.xpath("//h3[contains(text(),'Detalhes')]")));
        
        // Pausa Visual: Permite conferir o layout corrompido e a ausência do R$ nos totais.
        visualPause();

        assertTrue(itemsList.isDisplayed());
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
