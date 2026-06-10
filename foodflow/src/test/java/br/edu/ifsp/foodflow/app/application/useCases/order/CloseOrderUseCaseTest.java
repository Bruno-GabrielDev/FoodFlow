package br.edu.ifsp.foodflow.app.application.useCases.order;

import br.edu.ifsp.foodflow.app.domain.exceptions.EmptyOrderException;
import br.edu.ifsp.foodflow.app.domain.menuItem.MenuItem;
import br.edu.ifsp.foodflow.app.domain.order.Order;
import br.edu.ifsp.foodflow.app.domain.order.OrderRepository;
import br.edu.ifsp.foodflow.app.domain.order.dto.CloseOrderResultDTO;
import br.edu.ifsp.foodflow.app.domain.orderItem.OrderItem;
import br.edu.ifsp.foodflow.app.domain.table.Table;
import br.edu.ifsp.foodflow.app.domain.table.TableRepository;
import br.edu.ifsp.foodflow.app.domain.table.TableStatus;
import br.edu.ifsp.foodflow.app.domain.user.User;
import br.edu.ifsp.foodflow.app.domain.exceptions.OrderAlreadyClosedException;
import br.edu.ifsp.foodflow.app.domain.exceptions.OrderNotFoundException;
import br.edu.ifsp.foodflow.app.domain.user.UserRole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
public class CloseOrderUseCaseTest {

    @InjectMocks
    private CloseOrderUseCase closeOrderUseCase;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TableRepository tableRepository;

    private User user;
    private Table table;
    private br.edu.ifsp.foodflow.app.domain.order.Order order;
    private UUID randomUUID;

    private MenuItem menuItem;
    private OrderItem item;

    @BeforeEach
    void setup(){
        randomUUID = UUID.randomUUID();
        table = new Table(1);
        user = new User("João Silva","João","joao@gmail.com","1234", UserRole.WAITER);

        order = new br.edu.ifsp.foodflow.app.domain.order.Order(table,user);
        menuItem = new MenuItem(UUID.randomUUID(), "Prato", "desc", 80.0,1);
        item = new OrderItem(UUID.randomUUID(), menuItem, List.of(), user, "");

    }

    @Nested
    @Tag("TDD")
    @DisplayName("Testes criados com TDD")
    class TDDTests {

        @Test
        @DisplayName("Dado que a comanda informada é nula, quando o cliente tentar fechá-la, então deve ser lançado " +
                "um erro de comanda nula")
        void shouldThrowsNullPointerExceptionWhenOrderIdIsNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> closeOrderUseCase.closeOrder(null, 2))
                    .withMessage("O ID do pedido não pode ser nulo");
        }

        @Test
        @DisplayName("Dado que a comanda não existe, quando o cliente tentar fechá-la, então deve ser lançado um erro" +
                " de comanda inexistente")
        void shouldThrowsNoSuchElementExceptionWhenOrderNotExists() {
            when(orderRepository.findById(randomUUID)).thenReturn(Optional.empty());
            assertThatExceptionOfType(OrderNotFoundException.class)
                    .isThrownBy(() -> closeOrderUseCase.closeOrder(randomUUID, 2));
        }

        @Test
        @DisplayName("Dado que a comanda já está fechada, quando o cliente tentar fechá-la novamente, então um erro deve " +
                "ser lançado informando que a comanda já está fechada.")
        void shouldThrowsIllegalStateExceptionWhenOrderNotExists() {
            order.markAsClosed();
            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            assertThatExceptionOfType(OrderAlreadyClosedException.class).isThrownBy(() -> closeOrderUseCase.closeOrder(randomUUID, 2));
        }

        @Test
        @DisplayName("Dado que a comanda está aberta e não possui itens, quando o cliente tentar fechar a comanda, " +
                "então deve ser lançado um erro informando que a comanda não possui itens.")
        void shouldReturnIllegalStateExceptionWhenOrderNotHaveItems() {
            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            assertThatExceptionOfType(EmptyOrderException.class).isThrownBy(() -> closeOrderUseCase.closeOrder(randomUUID, 2));
        }

        @ParameterizedTest(name = "[{index}]: número de pessoas igual a {0} should throws IllegalArgumentException")
        @ValueSource(ints = {-1, 0})
        @DisplayName("Dado que a comanda está aberta e o número de pessoas informado para divisão da conta é zero" +
                " ou negativo, quando o cliente tentar fechar a comanda, deve ser lançado um erro de argumento inválido")
        void shouldThrowsIllegalArgumentExceptionWhenNumberOfPeopleIsLowerThan1(int numberOfPeople) {
            order.addOrderItem(item);
            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> closeOrderUseCase.closeOrder(randomUUID, numberOfPeople));
        }

        @Test
        @DisplayName("Dado que a comanda está aberta e o total está abaixo de R$ 100,00, quando o cliente fechar a comanda," +
                "deve ser disponibilizado um resumo de pagamento sem desconto aplicado")
        void shouldReturnCloseOrderResponseWithoutDiscount() {
            order.addOrderItem(item);
            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            CloseOrderResultDTO closeOrderResponse = closeOrderUseCase.closeOrder(randomUUID, 1);
            assertThat(closeOrderResponse.totalWithoutDiscount()).isEqualTo(80);
            assertThat(closeOrderResponse.discountPercentage()).isEqualTo(0);
            assertThat(closeOrderResponse.totalWithDiscount()).isEqualTo(80.00);

        }

        @Test
        @DisplayName("Dado que a comanda está aberta e o total está entre R$ 100,00 e R$ 199,99, quando o cliente fechar " +
                "a comanda, deve ser disponibilizado um resumo de pagamento com 5% de desconto aplicado.")
        void shouldReturnCloseOrderResponseWithDiscountOfFivePercent() {

            MenuItem menuItem2 = new MenuItem(UUID.randomUUID(), "Macarrão", "desc", 50.0, 1);
            OrderItem item2 = new OrderItem(UUID.randomUUID(), menuItem2, List.of(), user, "");

            order.addOrderItem(item);
            order.addOrderItem(item2);

            double totalWithDiscount = 130 * 0.95;

            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            CloseOrderResultDTO closeOrderResponse = closeOrderUseCase.closeOrder(randomUUID, 1);
            assertThat(closeOrderResponse.totalWithoutDiscount()).isEqualTo(130);
            assertThat(closeOrderResponse.discountPercentage()).isEqualTo(0.05);
            assertThat(closeOrderResponse.totalWithDiscount()).isEqualTo(totalWithDiscount);

        }

        @Test
        @DisplayName("Dado que a comanda está aberta e o total está entre R$ 200,00 e R$ 249,99, quando o cliente fechar " +
                "a comanda, deve ser disponibilizado um resumo de pagamento com 10% de desconto aplicado")
        void shouldReturnCloseOrderResponseWithDiscountTenPercent() {

            MenuItem menuItem2 = new MenuItem(UUID.randomUUID(), "Macarrão", "desc", 140.0, 1);
            OrderItem item2 = new OrderItem(UUID.randomUUID(), menuItem2, List.of(), user, "");

            order.addOrderItem(item);
            order.addOrderItem(item2);

            double totalWithDiscount = 220 * 0.90;

            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            CloseOrderResultDTO closeOrderResponse = closeOrderUseCase.closeOrder(randomUUID, 1);
            assertThat(closeOrderResponse.totalWithoutDiscount()).isEqualTo(220);
            assertThat(closeOrderResponse.discountPercentage()).isEqualTo(0.10);
            assertThat(closeOrderResponse.totalWithDiscount()).isEqualTo(totalWithDiscount);

        }

        @Test
        @DisplayName("Dado que a comanda está aberta e o total é igual ou superior a R$ 250,00, quando o cliente fechar a " +
                "comanda, deve ser disponibilizado um resumo de pagamento com 20% de desconto aplicado.")
        void shouldReturnCloseOrderResponseWithDiscountTwentyPercent() {
            MenuItem menuItem2 = new MenuItem(UUID.randomUUID(), "Macarrão", "desc", 200.0, 1);
            OrderItem item2 = new OrderItem(UUID.randomUUID(), menuItem2, List.of(), user, "");
            order.addOrderItem(item);
            order.addOrderItem(item2);

            double totalWithDiscount = 280 * 0.80;

            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            CloseOrderResultDTO closeOrderResponse = closeOrderUseCase.closeOrder(randomUUID, 1);
            assertThat(closeOrderResponse.totalWithoutDiscount()).isEqualTo(280);
            assertThat(closeOrderResponse.discountPercentage()).isEqualTo(0.20);
            assertThat(closeOrderResponse.totalWithDiscount()).isEqualTo(totalWithDiscount);

        }

        @Test
        @DisplayName("Dado que a comanda está aberta, quando o cliente fechar a comanda informando N pessoas " +
                "para divisão da conta, então deve ser disponibilizado um resumo de pagamento com o valor por " +
                "pessoa igual ao total com desconto dividido por N.")
        void shouldReturnCloseOrderResponseWithTotalPerPerson() {
            int numberOfPeople = 4;
            order.addOrderItem(item);
            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            CloseOrderResultDTO closeOrderResponse = closeOrderUseCase.closeOrder(randomUUID, numberOfPeople);
            assertThat(closeOrderResponse.totalPerPerson()).isEqualTo(80.0 / numberOfPeople);

        }

        @Test
        @DisplayName("Dado que a comanda está aberta, quando o cliente fechar a comanda, então a comanda " +
                "deve ter seu status alterado para fechada.")
        void shouldMarkOrderAsClosedWhenOrderIsClosed() {
            order.addOrderItem(item);
            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            closeOrderUseCase.closeOrder(randomUUID, 1);
            assertThat(order.getActive()).isFalse();

        }

        @Test
        @DisplayName("Dado que a comanda está aberta, quando o cliente fechar a comanda, então a mesa " +
                "deve ter seu status alterado para disponível.")
        void shouldMarkTableAsAvailableWhenOrderIsClosed() {
            order.addOrderItem(item);
            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            closeOrderUseCase.closeOrder(randomUUID, 1);
            assertThat(order.getTable().getStatus()).isEqualTo(TableStatus.AVAILABLE);

        }
        @Test
        @DisplayName("Dado que a comanda está aberta, quando o cliente fechar a comanda, então a comanda deve ser salva")                                                                   void shouldSaveOrderWhenOrderIsClosed() {
            order.addOrderItem(item);
            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            closeOrderUseCase.closeOrder(randomUUID, 1);
            verify(orderRepository).save(order);
        }

    }

    @Nested
    @Tag("Functional")
    @DisplayName("Testes criados com a técnica funcional")
    class FunctionalTests{
        @ParameterizedTest
        @CsvSource(value = {
                "99.99,0.0,99.99",
                "100.00,0.05,95.00",
                "199.99,0.05,189.9905",
                "200.00,0.1,180.00",
                "249.99,0.1,224.991",
                "250.00,0.2,200.00"
        })
        @DisplayName("Deve testar os valores limites para aplicação de disconto em finalização de comanda")
        void shouldTestLimitsValuesForDiscountInTotalOfOrder(double total,double discount,double totalWithDiscount){
            MenuItem menuItem2 = new MenuItem(
                    UUID.randomUUID(),
                    "Macarrão",
                    "desc",
                    total,
                    1
            );
            OrderItem item2 = new OrderItem(UUID.randomUUID(), menuItem2, List.of(), user, "");
            order.addOrderItem(item2);

            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            CloseOrderResultDTO closeOrderResponse = closeOrderUseCase.closeOrder(randomUUID, 1);
            assertThat(closeOrderResponse.totalWithoutDiscount()).isEqualTo(total);
            assertThat(closeOrderResponse.discountPercentage()).isEqualTo(discount);
            assertThat(closeOrderResponse.totalWithDiscount()).isEqualTo(totalWithDiscount);

        }
        @Test
        @DisplayName("Deve retornar o valor correto por pessoa ao dividir a comanda com número" +
                "de pessoas acima do limite mínimo válido")
        void shouldReturnCorrectTotalPerPersonWhenNumberOfPeopleIsAboveMinimumBoundary(){
            order.addOrderItem(item);
            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            CloseOrderResultDTO closeOrderResponse = closeOrderUseCase.closeOrder(randomUUID, 2);
            assertThat(closeOrderResponse.totalPerPerson()).isEqualTo(80.0 / 2);

        }

    }

    @Nested
    @Tag("Mutation")
    @DisplayName("Testes de Mutação")
    class MutationTests{
        @Test
        @DisplayName("Deve testar se mesa mudou status para disponível")
        void shouldSetTableStatusToAvailable(){
            Table table = new Table(1, TableStatus.OCCUPIED);
            br.edu.ifsp.foodflow.app.domain.order.Order order = new Order(table, user);
            order.addOrderItem(item);
            when(orderRepository.findById(randomUUID)).thenReturn(Optional.of(order));
            closeOrderUseCase.closeOrder(randomUUID, 1);
            assertThat(order.getTable().getStatus()).isEqualTo(TableStatus.AVAILABLE);

        }
    }

}
