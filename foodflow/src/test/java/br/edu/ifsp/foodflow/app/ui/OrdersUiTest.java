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

    @BeforeEach
    void prepareOrder() {
        AuthHelper.RegisteredUser user = UiTestHelper.loginViaUi(driver, BASE_URL);
        Response loginResponse = given().contentType(ContentType.JSON)
                .body(Map.of("username", user.username(), "password", user.password()))
                .when().post("/auth/login").then().statusCode(200).extract().response();

        this.token = loginResponse.path("token");
        this.userId = loginResponse.path("userId");
        this.orderId = OrderTestHelper.openOrder(token, OrderTestHelper.getAvailableTableNumber(token), userId);
        driver.navigate().to(BASE_URL + "/orders");
        new OrdersPage(driver).urlContains("/orders");
    }

    @AfterEach
    void cleanupOrder() {
        try {
            try {
                OrderTestHelper.addItem(token, orderId, OrderTestHelper.getFirstMenuItemId(token), userId);
            } catch (Throwable ignored) {}
            given().header("Authorization", "Bearer " + token).contentType(ContentType.JSON)
                    .body(Map.of("numberOfPeople", 1)).when().post("/orders/" + orderId + "/close");
        } catch (Throwable ignored) {}
    }

    @UiTest
    @DisplayName("UI 27: Deve exibir a tela de comandas")
    void shouldDisplayOrdersPage() {
        assertTrue(new OrdersPage(driver).isAtOrdersPage(), "Deveria exibir a tela de Comandas");
    }

    @UiTest
    @DisplayName("UI 28: Deve listar ao menos uma comanda ativa")
    void shouldListAtLeastOneOrder() {
        assertTrue(new OrdersPage(driver).countOrders() >= 1, "Deveria haver pelo menos uma comanda ativa");
    }

    @UiTest
    @DisplayName("UI 29: Deve abrir o modal de Adicionar Item ao clicar no botão")
    void shouldOpenAddItemModal() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem();
        assertTrue(orders.isAddItemModalVisible(), "O modal de Adicionar Item deveria estar visível");
    }

    @UiTest
    @DisplayName("UI 30: Deve adicionar um item com observação à comanda")
    void shouldAddItemWithObservation() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem().selectFirstMenuItem().fillObservations(faker.lorem().sentence(3)).confirmAddItem();
        assertFalse(orders.isAddItemModalVisible(), "O modal deveria ter sido fechado após lançar o item");
    }

    @UiTest
    @DisplayName("UI 31: Deve abrir o modal de Fechar Comanda ao clicar no botão")
    void shouldOpenCloseOrderModal() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickCloseOrder();
        assertTrue(orders.isCloseModalVisible(), "O modal de Fechar Comanda deveria estar visível");
    }

    @UiTest
    @DisplayName("UI 32: Deve cancelar o fechamento da comanda ao clicar em Cancelar")
    void shouldCancelCloseOrder() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickCloseOrder().cancelCloseOrder();
        assertFalse(orders.isCloseModalVisible(), "O modal de Fechar Comanda deveria ter sido fechado");
    }

    @UiTest
    @DisplayName("UI 33: Deve fechar a comanda com sucesso após adicionar um item")
    void shouldCloseOrderSuccessfully() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickAddItem().selectFirstMenuItem().confirmAddItem();
        orders.clickCloseOrder().fillPeopleCount("2").confirmCloseOrder();
        assertTrue(orders.isCloseSuccessVisible(), "Deveria exibir a tela de sucesso após fechar a comanda");
        orders.finalizeCloseSuccess();
    }

    @UiTest
    @DisplayName("UI 34: Deve abrir o modal de detalhes da comanda")
    void shouldOpenDetailsModal() {
        OrdersPage orders = new OrdersPage(driver);
        orders.clickDetails();
        assertTrue(orders.isDetailsModalVisible(), "O modal de Detalhes deveria estar visível");
    }

    @UiTest
    @DisplayName("UI 35: Deve navegar de volta para o dashboard pelo menu lateral")
    void shouldNavigateBackToDashboard() {
        OrdersPage orders = new OrdersPage(driver);
        orders.goToDashboard();
        assertTrue(orders.urlContains("/dashboard"), "Deveria voltar para o /dashboard pelo link da sidebar");
    }
}
