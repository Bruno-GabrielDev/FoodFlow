package br.edu.ifsp.foodflow.app.application.useCases.order;

import br.edu.ifsp.foodflow.app.domain.addOn.AddOn;
import br.edu.ifsp.foodflow.app.domain.addOn.AddOnRepository;
import br.edu.ifsp.foodflow.app.domain.menuItem.MenuItem;
import br.edu.ifsp.foodflow.app.domain.menuItem.MenuItemRepository;
import br.edu.ifsp.foodflow.app.domain.order.Order;
import br.edu.ifsp.foodflow.app.domain.order.OrderRepository;
import br.edu.ifsp.foodflow.app.domain.order.dto.AddItemToOrderDTO;
import br.edu.ifsp.foodflow.app.domain.order.dto.OrderResultDTO;
import br.edu.ifsp.foodflow.app.domain.orderItem.OrderItem;
import br.edu.ifsp.foodflow.app.domain.table.Table;
import br.edu.ifsp.foodflow.app.domain.user.User;
import br.edu.ifsp.foodflow.app.domain.user.UserRepository;
import br.edu.ifsp.foodflow.app.domain.exceptions.OrderAlreadyClosedException;
import br.edu.ifsp.foodflow.app.domain.exceptions.UnavailableItemException;
import br.edu.ifsp.foodflow.app.domain.exceptions.UserNotFoundException;
import br.edu.ifsp.foodflow.app.domain.user.UserRole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
public class AddItemToOrderUseCaseTest {
    private UUID orderId;
    private UUID menuItemId;
    private UUID waiterId;

    @Mock private OrderRepository orderRepository;
    @Mock private MenuItemRepository menuItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private AddOnRepository addOnRepository;

    @InjectMocks private AddItemToOrderUseCase sut;

    @BeforeEach
    void setup(){
        orderId = UUID.randomUUID();
        menuItemId = UUID.randomUUID();
        waiterId = UUID.randomUUID();
    }

    @Tag("TDD")
    @Nested
    @DisplayName("Testes criados com TDD")
    class TDDTests{
        @Test
        @DisplayName("Dado que o usuário esteja registrado e uma determinada mesa possua uma comanda ativa, " +
                "quando o usuário adicionar um item à comanda, então o item deve ser registrado " +
                "na comanda e o preço da comanda ser atualizado.")
        void shouldAddItemAnExistingOrder(){
            Table table = new Table(10);
            User user = new User("Estrupicio", "Pereira", "estrupicio@gmail.com", "1234", br.edu.ifsp.foodflow.app.domain.user.UserRole.WAITER);
            br.edu.ifsp.foodflow.app.domain.order.Order order = new br.edu.ifsp.foodflow.app.domain.order.Order(table, user);

            MenuItem menuItem = new MenuItem(menuItemId, "X-Tudo", "Ingredientes", 40.0, 10);
            AddItemToOrderDTO request = new AddItemToOrderDTO(orderId, menuItemId, "Sem milho", null, waiterId);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
            when(userRepository.findById(waiterId)).thenReturn(Optional.of(user));

            assertThat(order.getTotalPriceOfOrder()).isEqualTo(0);

            OrderResultDTO response = sut.execute(request);

            assertThat(response).isNotNull();

            assertThat(order.getOrderItems().getFirst().getMenuItem().getName()).isEqualTo("X-Tudo");
            assertThat(order.getOrderItems().getFirst().getObservations()).isEqualTo("Sem milho");
            assertThat(order.getTotalPriceOfOrder()).isEqualTo(40);
            verify(orderRepository, times(1)).save(order);
        }

        @Test
        @DisplayName("Dado que o usuário não esteja registrado, quando tentar adicionar um item à alguma comanda, " +
                "então a operação deve ser bloqueada e o sistema retornar erro de usuário não identificado.\n")
        void shouldThrowExceptionWhenWaiterIsNotRegistered() {
            AddItemToOrderDTO request = new AddItemToOrderDTO(orderId, menuItemId, "Sem milho", null, waiterId);

            br.edu.ifsp.foodflow.app.domain.order.Order mockOrder = mock(br.edu.ifsp.foodflow.app.domain.order.Order.class);
            when(mockOrder.getActive()).thenReturn(true);
            MenuItem menuItem = new MenuItem(menuItemId, "X-Tudo", "Ingredientes", 40.0, 10);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
            when(userRepository.findById(waiterId)).thenReturn(Optional.empty());

            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> sut.execute(request));

            assertEquals("O garçom informado não foi encontrado.", exception.getMessage());

            verify(orderRepository, never()).save(any());
            verify(mockOrder, never()).addOrderItem(any());
        }

        @Test
        @DisplayName("Dado que uma mesa possua uma comanda ativa e um item esteja marcado como indisponível, " +
                "quando o usuário tentar adicionar esse item à comanda, então a operação deve falhar e " +
                "o sistema informar a indisponibilidade do produto.")
        void shouldThrowExceptionWhenItemIsUnavailable(){
            AddItemToOrderDTO request = new AddItemToOrderDTO(orderId, menuItemId, "Sem milho", null, waiterId);

            br.edu.ifsp.foodflow.app.domain.order.Order mockOrder = mock(br.edu.ifsp.foodflow.app.domain.order.Order.class);
            when(mockOrder.getActive()).thenReturn(true);
            MenuItem menuItem = new MenuItem(menuItemId, "X-Tudo", "Ingredientes", 40.0, 0);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));

            UnavailableItemException exception = assertThrows(UnavailableItemException.class, () -> sut.execute(request));
            assertEquals("O item solicitado encontra-se indisponível.", exception.getMessage());
        }

        @Test
        @DisplayName("Dado que a comanda informada seja nula, quando o usuário tentar adicionar um item, então o " +
                "sistema deve disparar uma exceção informando que a comanda deve ser válida.")
        void shouldThrowExceptionWhenOrderIdIsNull(){
            AddItemToOrderDTO request = new AddItemToOrderDTO(null, menuItemId, "Sem milho", null, waiterId);
            NullPointerException exception = assertThrows(NullPointerException.class, () -> sut.execute(request));
            assertEquals("O ID do pedido não pode ser nulo", exception.getMessage());
        }

        @Test
        @DisplayName("Dado que a comanda informada seja inexistente, quando o usuário tentar adicionar um item, " +
                "então o sistema deve disparar uma exceção informando que a comanda deve ser válida.")
        void shouldThrowExceptionWhenOrderIdNotExists(){
            AddItemToOrderDTO request = new AddItemToOrderDTO(orderId, menuItemId, "Sem milho", null, waiterId);
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
            NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> sut.execute(request));
            assertEquals("Pedido não encontrado para o ID: " + orderId, exception.getMessage());
        }

        @Test
        @DisplayName("Dado que a comanda informada esteja encerrada, quando o usuário tentar adicionar um item, " +
                "então receberá uma informação de que é impossível adicionar um item a uma " +
                "comanda encerrada e a operação não será concluída.")
        void shouldThrowExceptionWhenAddOrderItemToAFinishedOrder(){
            AddItemToOrderDTO request = new AddItemToOrderDTO(orderId, menuItemId, "Sem milho", null, waiterId);
            br.edu.ifsp.foodflow.app.domain.order.Order mockOrder = mock(br.edu.ifsp.foodflow.app.domain.order.Order.class);
            when(mockOrder.getActive()).thenReturn(false);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            OrderAlreadyClosedException exception = assertThrows(OrderAlreadyClosedException.class, () -> sut.execute(request));
            assertEquals("Pedido já finalizado para o ID: " + orderId, exception.getMessage());
        }
    }

    @Tag("Structural")
    @Nested
    @DisplayName("Testes estruturais")
    class StructuralTests{
        @Test
        @DisplayName("Deve processar o item com sucesso quando os adicionais informados existirem")
        void shouldProcessItemSuccessfullyWhenAddOnsAreValid() {
            List<UUID> addOnIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            AddItemToOrderDTO request = new AddItemToOrderDTO(orderId, menuItemId, "Ponto da carne: Mal passado", addOnIds, waiterId);

            Table table = new Table(10);
            User user = new User("Garçom", "Sobrenome", "garcom@email.com", "123", UserRole.WAITER);
            br.edu.ifsp.foodflow.app.domain.order.Order order = new br.edu.ifsp.foodflow.app.domain.order.Order(table, user);

            MenuItem menuItem = new MenuItem(menuItemId, "X-Bacon", "Ingredientes", 30.0, 5);
            AddOn addOn1 = mock(AddOn.class);
            AddOn addOn2 = mock(AddOn.class);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
            when(userRepository.findById(waiterId)).thenReturn(Optional.of(user));
            when(addOnRepository.findAllById(addOnIds)).thenReturn(List.of(addOn1, addOn2));

            OrderResultDTO response = sut.execute(request);

            assertThat(response).isNotNull();
            verify(addOnRepository, times(1)).findAllById(addOnIds);

            OrderItem addedItem = order.getOrderItems().getFirst();
            assertThat(addedItem.getAdditions().size()).isEqualTo(2);
            verify(orderRepository, times(1)).save(order);
        }

        @Test
        @DisplayName("Deve lançar exceção quando a lista de adicionais contiver IDs inválidos ou removidos")
        void shouldThrowExceptionWhenSomeAddOnIsMissing() {
            List<UUID> addOnIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            AddItemToOrderDTO request = new AddItemToOrderDTO(orderId, menuItemId, "Sem molho", addOnIds, waiterId);

            br.edu.ifsp.foodflow.app.domain.order.Order mockOrder = mock(br.edu.ifsp.foodflow.app.domain.order.Order.class);
            when(mockOrder.getActive()).thenReturn(true);

            MenuItem menuItem = new MenuItem(menuItemId, "X-Bacon", "Ingredientes", 30.0, 5);
            User user = new User("Garçom", "Sobrenome", "garcom@email.com", "123", UserRole.WAITER);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
            when(userRepository.findById(waiterId)).thenReturn(Optional.of(user));

            AddOn addOn1 = mock(AddOn.class);
            when(addOnRepository.findAllById(addOnIds)).thenReturn(List.of(addOn1));

            NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> sut.execute(request));

            assertEquals("Um ou mais adicionais informados são inválidos ou foram removidos.", exception.getMessage());
            verify(mockOrder, never()).addOrderItem(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve processar o item com sucesso quando a lista de adicionais estiver vazia")
        void shouldProcessItemSuccessfullyWhenAddOnsAreEmpty() {
            AddItemToOrderDTO request = new AddItemToOrderDTO(orderId, menuItemId, "Ponto da carne: Mal passado", List.of(), waiterId);

            Table table = new Table(10);
            User user = new User("Garçom", "Sobrenome", "garcom@email.com", "123", UserRole.WAITER);
            br.edu.ifsp.foodflow.app.domain.order.Order order = new Order(table, user);

            MenuItem menuItem = new MenuItem(menuItemId, "X-Bacon", "Ingredientes", 30.0, 5);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
            when(userRepository.findById(waiterId)).thenReturn(Optional.of(user));

            OrderResultDTO response = sut.execute(request);

            assertThat(response).isNotNull();

            OrderItem addedItem = order.getOrderItems().getFirst();
            assertThat(addedItem.getAdditions().size()).isEqualTo(0);
            verify(orderRepository, times(1)).save(order);
        }
    }

    @Tag("Mutation")
    @Nested
    @DisplayName("Mutation Test")
    class MutationTest {
        @Test
        @DisplayName("Deve lançar exceção quando o item do menu não existir")
        void shouldThrowExceptionWhenMenuItemNotFound() {
            AddItemToOrderDTO request =
                    new AddItemToOrderDTO(orderId, menuItemId, "Sem milho", null, waiterId);

            Order mockOrder = mock(Order.class);
            when(mockOrder.getActive()).thenReturn(true);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.empty());

            NoSuchElementException exception =
                    assertThrows(NoSuchElementException.class, () -> sut.execute(request));

            assertEquals(
                    "Item do menu não encontrado para o ID: " + menuItemId,
                    exception.getMessage()
            );

            verify(mockOrder, never()).addOrderItem(any());
            verify(orderRepository, never()).save(any());
        }
    }
}
