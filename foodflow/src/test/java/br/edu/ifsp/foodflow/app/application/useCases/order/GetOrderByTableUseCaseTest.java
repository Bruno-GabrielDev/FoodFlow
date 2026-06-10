package br.edu.ifsp.foodflow.app.application.useCases.order;

import br.edu.ifsp.foodflow.app.domain.addOn.AddOn;
import br.edu.ifsp.foodflow.app.domain.exceptions.OrderNotFoundException;
import br.edu.ifsp.foodflow.app.domain.exceptions.TableNotFoundException;
import br.edu.ifsp.foodflow.app.domain.menuItem.MenuItem;
import br.edu.ifsp.foodflow.app.domain.order.Order;
import br.edu.ifsp.foodflow.app.domain.order.OrderRepository;
import br.edu.ifsp.foodflow.app.domain.order.dto.OrderDetailsDTO;
import br.edu.ifsp.foodflow.app.domain.orderItem.OrderItem;
import br.edu.ifsp.foodflow.app.domain.orderItem.OrderItemStatus;
import br.edu.ifsp.foodflow.app.domain.orderItem.dto.AddOnSummaryDTO;
import br.edu.ifsp.foodflow.app.domain.orderItem.dto.AddOnSummaryDTO;
import br.edu.ifsp.foodflow.app.domain.orderItem.dto.OrderItemDetailsDTO;
import br.edu.ifsp.foodflow.app.domain.table.Table;
import br.edu.ifsp.foodflow.app.domain.table.TableRepository;
import br.edu.ifsp.foodflow.app.domain.user.User;
import br.edu.ifsp.foodflow.app.domain.user.UserRole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class GetOrderByTableUseCaseTest {
    private Table table;
    private User user;
    private Order order;

    @InjectMocks
    private GetOrderByTableUseCase service;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TableRepository tableRepository;

    @BeforeEach
    void setUp(){
        table = new Table(1);
        user = new User("João", "João", "email", "123", UserRole.WAITER);
        order = new Order(table, user);
    }

    @Nested
    @Tag("TDD")
    class TDDTests {
        @Test
        @DisplayName("Deve lançar NullPointerException quando o ID da mesa for nulo")
        void shouldThrowExceptionWhenOrderIsNull() {
            NullPointerException exception = assertThrows(NullPointerException.class,
                    () -> service.getOrderByTable(null));

            assertEquals("O Id da mesa é obrigatório.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar TableNotFoundException quando a mesa não existir")
        void shouldThrowExceptionWhenTableDoesNotExist() {
            Integer tableIdNotExist = 99999;
            when(tableRepository.findByTableNumber(tableIdNotExist)).thenReturn(Optional.empty());

            TableNotFoundException exception = assertThrows(TableNotFoundException.class,
                    () -> service.getOrderByTable(tableIdNotExist));

            assertEquals("Mesa não encontrada.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar OrderNotFoundException quando não encontrar comanda na mesa")
        void shouldThrowExceptionWhenTableHasNoActiveOrder() {

            when(tableRepository.findByTableNumber(table.getTableNumber()))
                    .thenReturn(Optional.of(table));
            when(orderRepository.findActiveOrderByTable(table))
                    .thenReturn(Optional.empty());

            OrderNotFoundException exception = assertThrows(
                    OrderNotFoundException.class,
                    () -> service.getOrderByTable(table.getTableNumber())
            );

            assertEquals("Não existe comanda ativa para essa mesa.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve retornar OrderDetailsResponse com itens quando houver pedido ativo para a mesa")
        void shouldReturnOrderDTOWhenActiveOrderExists() {

            MenuItem menuItem = new MenuItem(UUID.randomUUID(), "Prato", "Descrição do prato", 50.0, 10);
            OrderItem item = new OrderItem(UUID.randomUUID(), menuItem, List.of(), user, "");

            order.addOrderItem(item);
            order.addOrderItem(item);

            when(tableRepository.findByTableNumber(1))
                    .thenReturn(Optional.of(table));

            when(orderRepository.findActiveOrderByTable(table))
                    .thenReturn(Optional.of(order));

            OrderDetailsDTO result = service.getOrderByTable(1);

            assertNotNull(result);
            assertEquals(table.getTableNumber(), result.tableNumber());
            assertEquals(user.getUsername(), result.userName());
            assertEquals(2, result.items().size());
            assertEquals(50.0, result.items().getFirst().price());
            assertEquals(100.0, result.total());
        }
    }

    @Nested
    @Tag("Functional")
    class FunctionalTests {
        @Test
        @DisplayName("Deve retornar OrderDetailsResponse com lista de itens vazia quando o pedido não tiver itens")
        void shouldReturnEmptyItemsWhenOrderHasNoItems() {

            when(tableRepository.findByTableNumber(1))
                    .thenReturn(Optional.of(table));

            when(orderRepository.findActiveOrderByTable(table))
                    .thenReturn(Optional.of(order));

            OrderDetailsDTO result = service.getOrderByTable(1);
            assertNotNull(result);
            assertEquals(table.getTableNumber(), result.tableNumber());
            assertEquals(user.getUsername(), result.userName());
            assertNotNull(result.items());
            assertTrue(result.items().isEmpty());
            assertEquals(0.0, result.total());
        }

        @Test
        @DisplayName("Deve aplicar desconto correto no OrderDetailsResponse")
        void shouldApplyDiscountCorrectly() {
            MenuItem menuItem1 = new MenuItem(UUID.randomUUID(), "Prato 1", "desc", 120.0, 10);
            OrderItem item1 = new OrderItem(UUID.randomUUID(), menuItem1, List.of(), user, "");

            order.addOrderItem(item1);

            when(tableRepository.findByTableNumber(1))
                    .thenReturn(Optional.of(table));

            when(orderRepository.findActiveOrderByTable(table))
                    .thenReturn(Optional.of(order));

            OrderDetailsDTO result = service.getOrderByTable(1);

            assertEquals(120.0, result.total());
            assertEquals(0.05, result.discount());
        }

        @Test
        @DisplayName("Deve somar corretamente o preço do item com adicionais")
        void shouldCalculateItemPriceWithAddOns() {
            MenuItem menuItem = new MenuItem(UUID.randomUUID(), "Hamburguer", "desc", 20.0, 10);
            AddOn extraQueijo = new AddOn(UUID.randomUUID(), "Queijo", 5.0);
            AddOn bacon = new AddOn(UUID.randomUUID(), "Bacon", 7.0);

            OrderItem item = new OrderItem(
                    UUID.randomUUID(),
                    menuItem,
                    List.of(extraQueijo, bacon),
                    user,
                    ""
            );

            order.addOrderItem(item);

            when(tableRepository.findByTableNumber(1))
                    .thenReturn(Optional.of(table));

            when(orderRepository.findActiveOrderByTable(table))
                    .thenReturn(Optional.of(order));

            OrderDetailsDTO result = service.getOrderByTable(1);
            assertEquals(32.0, result.items().getFirst().price());
            assertEquals(32.0, result.total());
        }

        @Test
        @DisplayName("Deve retornar o status atual de cada item do pedido")
        void shouldReturnOrderItemStatus() {
            MenuItem menuItem = new MenuItem(UUID.randomUUID(), "Pizza", "desc", 40.0, 10);
            OrderItem item = new OrderItem(UUID.randomUUID(), menuItem, List.of(), user, "");

            item.upgradeProgress();

            order.addOrderItem(item);

            when(tableRepository.findByTableNumber(1))
                    .thenReturn(Optional.of(table));
            when(orderRepository.findActiveOrderByTable(table))
                    .thenReturn(Optional.of(order));

            OrderDetailsDTO result = service.getOrderByTable(1);
            assertEquals(OrderItemStatus.PREPARATION, result.items().getFirst().status());
        }
    }

    @Nested
    @Tag("Structural")
    class StructuralTests {
        @Test
        @DisplayName("Deve retornar Sistema no campo waiter quando o item não possuir garçom atribuído")
        void shouldReturnSistemaWhenOrderItemHasNoWaiter() {
            MenuItem menuItem = new MenuItem(UUID.randomUUID(), "Prato", "desc", 30.0, 10);
            OrderItem item = new OrderItem(
                    UUID.randomUUID(),
                    menuItem,
                    List.of(),
                    null,
                    "Sem cebola"
            );

            order.addOrderItem(item);

            when(tableRepository.findByTableNumber(1))
                    .thenReturn(Optional.of(table));
            when(orderRepository.findActiveOrderByTable(table))
                    .thenReturn(Optional.of(order));

            OrderDetailsDTO result = service.getOrderByTable(1);
            assertNotNull(result);
            assertEquals(1, result.items().size());
            assertEquals("Sistema", result.items().getFirst().waiterName());
        }
    }

    @Nested
    @Tag("Mutation")
    @DisplayName("Testes de Mutação")
    class MutationTests {

        @Test
        @DisplayName("deve testar retorno quando item tem adicionais")
        void shouldTestWhenItemHasAdditional() {
            MenuItem menuItem = new MenuItem(UUID.randomUUID(), "Pizza", "desc", 40.0, 10);
            List<AddOn> addOns = List.of(
                    new AddOn("Bacon extra", 5.0),
                    new AddOn("Queijo Extra", 3.0)
            );
            OrderItem item = new OrderItem(UUID.randomUUID(), menuItem, addOns, user, "");
            order.addOrderItem(item);

            when(tableRepository.findByTableNumber(1))
                    .thenReturn(Optional.of(table));
            when(orderRepository.findActiveOrderByTable(table))
                    .thenReturn(Optional.of(order));

            OrderDetailsDTO result = service.getOrderByTable(1);
            assertThat(result.items().getFirst().additions()).isEqualTo(
                    addOns.stream().map(addOn -> new AddOnSummaryDTO(addOn.getName(), addOn.getPrice())).toList()
            );
        }
    }

}
