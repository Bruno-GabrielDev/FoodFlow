package br.edu.ifsp.foodflow.app.ui;

import br.edu.ifsp.foodflow.app.annotation.UiTest;
import br.edu.ifsp.foodflow.app.ui.pages.OrdersPage;
import br.edu.ifsp.foodflow.app.util.AuthHelper;
import br.edu.ifsp.foodflow.app.util.OrderTestHelper;
import br.edu.ifsp.foodflow.app.util.UiTestHelper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de UI - Tela de Comandas")
class OrdersUiTest extends BaseWebTest {

    private final Faker faker = new Faker();
    private String token;
    private String userId;
    private String orderId;

    /**
     * Antes de cada teste: registra usuário, loga via UI (sessão no browser),
     * abre uma comanda via API (já guarda orderId) e navega para /orders.
     */
    @BeforeEach
    void prepareOrder() {
        // Login UI - garante que o browser tenha a sessão/token no localStorage
        AuthHelper.RegisteredUser user = UiTestHelper.loginViaUi(driver, BASE_URL);

        // Login via API para obter token + userId
        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", user.username(), "password", user.password()))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        this.token = loginResponse.path("token");
        this.userId = loginResponse.path("userId");

        // Abre comanda via API (mais rápido e confiável que via UI)
        int table = OrderTestHelper.getAvailableTableNumber(token);
        this.orderId = OrderTestHelper.openOrder(token, table, userId);

        // Navega o browser para /orders
        driver.navigate().to(BASE_URL + "/orders");
        new OrdersPage(driver).urlContains("/orders");
    }

    /**
     * Depois de cada teste: garante que a comanda seja fechada via API,
     * liberando a mesa para o próximo teste. Adiciona um item se preciso
     * (close de comanda vazia retorna 422).
     */
    @AfterEach
    void cleanupOrder() {
        try {
            // Tenta adicionar um item (se a comanda já estiver fechada, falha silenciosamente)
            try {
                String menuItemId = OrderTestHelper.getFirstMenuItemId(token);
                OrderTestHelper.addItem(token, orderId, menuItemId, userId);
            } catch (Exception ignored) {
                // Comanda pode já ter sido fechada pelo teste ou já ter item
            }

            // Tenta fechar a comanda
            given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of("numberOfPeople", 1))
                    .when()
                    .post("/orders/" + orderId + "/close");
        } catch (Exception ignored) {
            // Limpeza não deve falhar o teste
        }
    }

    @UiTest
    @DisplayName("Deve exibir a tela de comandas")
    void shouldDisplayOrdersPage() {
        OrdersPage orders = new OrdersPage(driver);
        assertTrue(orders.isAtOrdersPage(), "Deveria exibir a tela de Comandas");
    }

    @UiTest
    @DisplayName("Deve listar ao menos uma comanda ativa")
    void shouldListAtLeastOneOrder() {
        OrdersPage orders = new OrdersPage(driver);
        assertTrue(orders.countOrders() >= 1, "Deveria haver pelo menos uma comanda ativa");
    }

    @UiTest
    @DisplayName("Deve abrir o modal de Adicionar Item ao clicar no botão")
    void shouldOpenAddItemModal() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem();
        assertTrue(orders.isAddItemModalVisible(), "O modal de Adicionar Item deveria estar visível");
    }

    @UiTest
    @DisplayName("Deve adicionar um item com observação à comanda")
    void shouldAddItemWithObservation() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem()
                .selectFirstMenuItem()
                .fillObservations(faker.lorem().sentence(3))
                .confirmAddItem();

        assertFalse(orders.isAddItemModalVisible(),
                "O modal deveria ter sido fechado após lançar o item");
    }

    @UiTest
    @DisplayName("Deve abrir o modal de Fechar Comanda ao clicar no botão")
    void shouldOpenCloseOrderModal() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickCloseOrder();
        assertTrue(orders.isCloseModalVisible(), "O modal de Fechar Comanda deveria estar visível");
    }

    @UiTest
    @DisplayName("Deve cancelar o fechamento da comanda ao clicar em Cancelar")
    void shouldCancelCloseOrder() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickCloseOrder();
        orders.cancelCloseOrder();
        assertFalse(orders.isCloseModalVisible(),
                "O modal de Fechar Comanda deveria ter sido fechado");
    }

    @UiTest
    @DisplayName("Deve fechar a comanda com sucesso após adicionar um item")
    void shouldCloseOrderSuccessfully() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem()
                .selectFirstMenuItem()
                .confirmAddItem();

        orders.clickCloseOrder()
                .fillPeopleCount("2")
                .confirmCloseOrder();

        assertTrue(orders.isCloseSuccessVisible(),
                "Deveria exibir a tela de sucesso após fechar a comanda");
        orders.finalizeCloseSuccess();
    }

    @UiTest
    @DisplayName("Deve abrir o modal de detalhes da comanda")
    void shouldOpenDetailsModal() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickDetails();
        assertTrue(orders.isDetailsModalVisible(), "O modal de Detalhes deveria estar visível");
    }

    @UiTest
    @DisplayName("Deve navegar de volta para o dashboard pelo menu lateral")
    void shouldNavigateBackToDashboard() {
        OrdersPage orders = new OrdersPage(driver);
        orders.goToDashboard();
        boolean navigated = orders.urlContains("/dashboard");
        assertTrue(navigated, "Deveria voltar para o /dashboard pelo link da sidebar");
    }
}