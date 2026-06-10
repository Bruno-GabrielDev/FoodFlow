package br.edu.ifsp.foodflow.app.application.useCases.order;

import br.edu.ifsp.foodflow.app.domain.exceptions.OrderItemAlreadyFinishedException;
import br.edu.ifsp.foodflow.app.domain.exceptions.OrderItemNotFoundException;
import br.edu.ifsp.foodflow.app.domain.exceptions.OrderNotFoundException;
import br.edu.ifsp.foodflow.app.domain.menuItem.MenuItem;
import br.edu.ifsp.foodflow.app.domain.order.Order;
import br.edu.ifsp.foodflow.app.domain.order.OrderRepository;
import br.edu.ifsp.foodflow.app.domain.order.dto.AdvanceOrderItemStatusDTO;
import br.edu.ifsp.foodflow.app.domain.orderItem.OrderItem;
import br.edu.ifsp.foodflow.app.domain.orderItem.OrderItemStatus;
import br.edu.ifsp.foodflow.app.domain.table.Table;
import br.edu.ifsp.foodflow.app.domain.user.User;
import br.edu.ifsp.foodflow.app.domain.user.UserRole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class AdvanceOrderItemStatusUseCaseTest {
    private br.edu.ifsp.foodflow.app.domain.order.Order order;
    private Table table;
    private User user;
    private OrderItem orderItem;
    private MenuItem menuItem;
    private UUID orderId;
    private UUID itemId;

    @InjectMocks
    private AdvanceOrderItemStatusUseCase statusUseCaseTest;

    @Mock
    private OrderRepository orderRepository;

    @BeforeEach
    void setup(){
        orderId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        table = new Table(1);
        user = new User("João Silva", "João", "joao@gmail.com", "1234", UserRole.WAITER);
        order = new Order(table, user);
        menuItem = new MenuItem(UUID.randomUUID(), "Prato", "desc", 50.0, 1);
        orderItem = new OrderItem(itemId, menuItem, List.of(), user, "");
    }

    @Nested
    @Tag("TDD")
    @DisplayName("Testes criados com TDD")
    class tddTests {

        @Test
        @DisplayName("Dado que a comanda informada é nula, quando o garçom tentar avançar o status do item, " +
                "então o sistema deve lançar um erro informando que a comanda não pode ser nula")
        void shouldThrowNullPointerExceptionWhenOrderIdIsNull() {
            assertThatNullPointerException().isThrownBy(() -> statusUseCaseTest.advanceStatus(null, orderId));

        }

        @Test
        @DisplayName("Dado que o id do item informado é nulo, quando o garçom tentar avançar o status, então o sistema deve" +
                " lançar um erro informando que o id do item não pode ser nulo.")
        void shouldThrowNullPointerExceptionWhenOrItemIdIsNull() {
            assertThatNullPointerException().isThrownBy(() -> statusUseCaseTest.advanceStatus(orderId, null));

        }

        @Test
        @DisplayName("Dado que a comanda não existe, quando o garçom tentar avançar o status do item, " +
                "então o sistema deve lançar um erro informando que a comanda não foi encontrada")
        void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
            assertThatExceptionOfType(OrderNotFoundException.class)
                    .isThrownBy(() -> statusUseCaseTest.advanceStatus(orderId, itemId));
        }

        @Test
        @DisplayName("Dado que o item informado não está na comanda, quando o garçom tentar avançar o status, " +
                "então o sistema deve lançar um erro informando que o item não foi encontrado na comanda")
        void shouldThrowOrderItemNotFoundExceptionWhenItemIsNotInOrder() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            assertThatExceptionOfType(OrderItemNotFoundException.class)
                    .isThrownBy(() -> statusUseCaseTest.advanceStatus(orderId, itemId));
        }

        @Test
        @DisplayName("Dado que o status do item está como pendente, quando o garçom solicitar o avanço, " +
                "então o status deve ser alterado para em preparo")
        void shouldAdvanceStatusFromPendingToPreparation() {
            order.addOrderItem(orderItem);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            statusUseCaseTest.advanceStatus(orderId, itemId);
            assertThat(orderItem.getStatus()).isEqualTo(OrderItemStatus.PREPARATION);
        }

        @Test
        @DisplayName("Dado que o status do item está como em preparo, quando o garçom solicitar o avanço," +
                "então o status deve ser alterado para finalizado")
        void shouldAdvanceStatusFromPreparationToFinish() {
            order.addOrderItem(orderItem);
            orderItem.upgradeProgress();
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            statusUseCaseTest.advanceStatus(orderId, itemId);
            assertThat(orderItem.getStatus()).isEqualTo(OrderItemStatus.FINISHED);
        }

        @Test
        @DisplayName("Dado que o status do item está como finalizado, quando o garçom tentar solicitar o avanço, " +
                "então o sistema deve lançar um erro informando que um item finalizado não pode ter seu status alterado")
        void shouldThrowOrderItemAlreadyFinishedExceptionWhenItemIsAlreadyFinished() {
            order.addOrderItem(orderItem);
            orderItem.upgradeProgress();
            orderItem.upgradeProgress();
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            assertThrows(OrderItemAlreadyFinishedException.class, () -> statusUseCaseTest.advanceStatus(orderId, itemId));
        }

        @Test
        @DisplayName("Dado que o status da comanda foi alterada, quando o sistema processar, então deve salvar a comanda")
        void shouldSaveOrderAfterAdvancingStatus() {
            order.addOrderItem(orderItem);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            statusUseCaseTest.advanceStatus(orderId, itemId);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Dado que o status do item foi avançado, quando o sistema processar a mudança, " +
                "então deve ser armazenado o horário da alteração")
        void shouldUpdateTimestampWhenStatusIsAdvanced() {                                                                                                                                      order.addOrderItem(orderItem);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            LocalDateTime before = orderItem.getUpdateAt();
            statusUseCaseTest.advanceStatus(orderId, itemId);
            assertThat(orderItem.getUpdateAt()).isAfterOrEqualTo(before);
        }
        @Test
        @DisplayName("Dado que o status do item foi avançado, quando o sistema processar, " +
                "então deve retornar um DTO com o novo status do item")
        void shouldReturnDtoWithUpdatedStatus() {
            order.addOrderItem(orderItem);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            AdvanceOrderItemStatusDTO response = statusUseCaseTest.advanceStatus(orderId, itemId);
            assertThat(response.itemId()).isEqualTo(itemId);
            assertThat(response.orderId()).isEqualTo(orderId);
            assertThat(response.status()).isEqualTo(OrderItemStatus.PREPARATION);
        }

    }

    @Nested
    @Tag("Mutation")
    @DisplayName("Testes de Mutação")
    class MutationTests{
        @Test
        @DisplayName("Deve avançar o status do item correto independente da posição na lista ")
        void shouldChangeStatusRegardlessOfPositionInList() {
            OrderItem otherItem = new OrderItem(UUID.randomUUID(), menuItem, List.of(), user, "");
            order.addOrderItem(otherItem);
            order.addOrderItem(orderItem);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            statusUseCaseTest.advanceStatus(orderId, itemId);

            assertThat(orderItem.getStatus()).isEqualTo(OrderItemStatus.PREPARATION);
            assertThat(otherItem.getStatus()).isEqualTo(OrderItemStatus.PENDING);
        }

    }

}
