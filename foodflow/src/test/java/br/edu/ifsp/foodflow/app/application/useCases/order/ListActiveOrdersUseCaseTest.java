package br.edu.ifsp.foodflow.app.application.useCases.order;

import br.edu.ifsp.foodflow.app.application.useCases.order.ListActiveOrdersUseCase;
import br.edu.ifsp.foodflow.app.domain.addOn.AddOn;
import br.edu.ifsp.foodflow.app.domain.menuItem.MenuItem;
import br.edu.ifsp.foodflow.app.domain.order.Order;
import br.edu.ifsp.foodflow.app.domain.order.OrderRepository;
import br.edu.ifsp.foodflow.app.domain.order.dto.OrderDetailsDTO;
import br.edu.ifsp.foodflow.app.domain.orderItem.OrderItem;
import br.edu.ifsp.foodflow.app.domain.orderItem.dto.OrderItemDetailsDTO;
import br.edu.ifsp.foodflow.app.domain.table.Table;
import br.edu.ifsp.foodflow.app.domain.user.User;
import br.edu.ifsp.foodflow.app.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
public class ListActiveOrdersUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ListActiveOrdersUseCase sut;

    @Tag("Structural")
    @Nested
    @DisplayName("Testes Estruturais (com instâncias reais)")
    class StructuralTests {
        @Test
        @DisplayName("Deve mapear corretamente um pedido quando o item não tiver um garçom responsável, atribuindo a string 'Sistema'")
        void shouldMapOrderAndItemsWhenItemHasNoWaiter() {
            Table table = new Table(2);
            User waiter = new User("Joao", "Garcom", "joao@email.com", "123", UserRole.WAITER);
            Order order = new Order(table, waiter);

            MenuItem menuItem = new MenuItem(UUID.randomUUID(), "Refrigerante", "Lata 350ml", 10.0, 0);

            OrderItem item = new OrderItem(UUID.randomUUID(), menuItem, List.of(), null, "Com gelo");
            order.addOrderItem(item);

            when(orderRepository.findAllActive()).thenReturn(List.of(order));

            List<OrderDetailsDTO> result = sut.execute();

            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(1);

            OrderDetailsDTO orderDetails = result.getFirst();
            assertThat(orderDetails.items().size()).isEqualTo(1);
            assertThat(orderDetails.items().getFirst().waiterName()).isEqualTo("Sistema");

            verify(orderRepository, times(1)).findAllActive();
        }

        @Test
        @DisplayName("Deve mapear corretamente um pedido e seus itens quando o item possuir um garçom responsável")
        void shouldMapOrderAndItemsWhenItemHasWaiter() {
            Table table = new Table(5);
            User waiter = new User("Joao", "joao", "joao@email.com", "123", UserRole.WAITER);
            Order order = new Order(table, waiter);

            MenuItem menuItem = new MenuItem(UUID.randomUUID(), "X-Bacon", "Ingredientes", 35.0, 15);

            OrderItem item = new OrderItem(UUID.randomUUID(), menuItem, List.of(), waiter, "Sem cebola");
            order.addOrderItem(item);

            when(orderRepository.findAllActive()).thenReturn(List.of(order));

            List<OrderDetailsDTO> result = sut.execute();

            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(1);

            OrderDetailsDTO orderDetails = result.getFirst();
            assertThat(orderDetails.tableNumber()).isEqualTo(5);
            assertThat(orderDetails.userName()).isEqualTo("joao");
            assertThat(orderDetails.total()).isEqualTo(35.0);

            assertThat(orderDetails.items().size()).isEqualTo(1);
            assertThat(orderDetails.items().getFirst().waiterName()).isEqualTo("joao");

            verify(orderRepository, times(1)).findAllActive();
        }
    }

    @Tag("Mutation")
    @Nested
    class MutationTests {
        @Test
        @DisplayName("Deve mapear corretamente os adicionais (add-ons) dos itens do pedido")
        void shouldMapAddOnsCorrectly() {
            Table table = new Table(1);
            User waiter = new User("Joao", "joao", "joao@email.com", "123", UserRole.WAITER);

            Order order = new Order(table, waiter);

            MenuItem menuItem = new MenuItem(UUID.randomUUID(), "Hamburguer", "desc", 20.0, 10);

            AddOn queijo = new AddOn(UUID.randomUUID(), "Queijo", 5.0);
            AddOn bacon = new AddOn(UUID.randomUUID(), "Bacon", 7.0);

            OrderItem item = new OrderItem(
                    UUID.randomUUID(),
                    menuItem,
                    List.of(queijo, bacon),
                    waiter,
                    "sem cebola"
            );

            order.addOrderItem(item);

            when(orderRepository.findAllActive()).thenReturn(List.of(order));

            List<OrderDetailsDTO> result = sut.execute();

            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(1);

            OrderDetailsDTO dto = result.getFirst();
            OrderItemDetailsDTO itemDto = dto.items().getFirst();

            assertThat(itemDto.additions()).hasSize(2);

            assertThat(itemDto.additions().get(0).name()).isEqualTo("Queijo");
            assertThat(itemDto.additions().get(0).price()).isEqualTo(5.0);

            assertThat(itemDto.additions().get(1).name()).isEqualTo("Bacon");
            assertThat(itemDto.additions().get(1).price()).isEqualTo(7.0);

            verify(orderRepository, times(1)).findAllActive();
        }
    }

}
