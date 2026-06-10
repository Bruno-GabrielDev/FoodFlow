package br.edu.ifsp.foodflow.app.persistence;

import br.edu.ifsp.foodflow.app.annotation.PersistenceTest;
import br.edu.ifsp.foodflow.app.domain.order.OrderRepository;
import br.edu.ifsp.foodflow.app.domain.orderItem.OrderItemStatus;
import br.edu.ifsp.foodflow.app.infra.persistence.entity.AddOnJpaEntity;
import br.edu.ifsp.foodflow.app.infra.persistence.entity.OrderItemJpaEntity;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataAddOnRepository;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataMenuItemRepository;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataOrderItemRepository;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataOrderRepository;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataUserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

@DisplayName("Testes de Persistência - Itens da Comanda")
class OrderItemPersistenceTest extends BasePersistenceTest {

    private static final UUID ORDER_ID =
            UUID.fromString("b4c5d6e7-f8a9-0123-bcde-234567890123");
    private static final UUID MENU_ITEM_ID =
            UUID.fromString("d4e5f6a7-b8c9-0123-defa-234567890123");
    private static final UUID WAITER_ID =
            UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID BACON_ADD_ON_ID =
            UUID.fromString("c9d0e1f2-a3b4-5678-cdef-789012345678");
    private static final UUID CHEDDAR_ADD_ON_ID =
            UUID.fromString("a3b4c5d6-e7f8-9012-abcd-123456789012");
    private static final UUID ORDER_WITH_PRICED_ITEM_ID =
            UUID.fromString("c5d6e7f8-a9b0-1234-cdef-345678901234");
    private static final UUID PRICED_ORDER_ITEM_ID =
            UUID.fromString("22220001-2222-2222-2222-222222222222");

    @Autowired
    private SpringDataOrderItemRepository orderItemRepository;

    @Autowired
    private SpringDataAddOnRepository addOnRepository;

    @Autowired
    private SpringDataOrderRepository orderRepository;

    @Autowired
    private SpringDataMenuItemRepository menuItemRepository;

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OrderRepository domainOrderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceTest
    @Transactional
    @DisplayName("Deve persistir observação com exatamente 255 caracteres")
    void shouldPersistObservationWithExactly255Characters() {
        String observation = "a".repeat(255);
        LocalDateTime now = LocalDateTime.now();
        OrderItemJpaEntity orderItem = new OrderItemJpaEntity(
                null,
                orderRepository.getReferenceById(ORDER_ID),
                menuItemRepository.getReferenceById(MENU_ITEM_ID),
                userRepository.getReferenceById(WAITER_ID),
                new ArrayList<>(),
                observation,
                OrderItemStatus.PENDING,
                35.90,
                now,
                now
        );

        UUID orderItemId = orderItemRepository.saveAndFlush(orderItem).getId();
        entityManager.clear();

        OrderItemJpaEntity persistedOrderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow();

        assertThat(persistedOrderItem.getObservations())
                .hasSize(255)
                .isEqualTo(observation);
    }

    @PersistenceTest
    @Transactional
    @DisplayName("Deve rejeitar observação com 256 caracteres")
    void shouldRejectObservationWith256Characters() {
        String observation = "a".repeat(256);
        LocalDateTime now = LocalDateTime.now();
        OrderItemJpaEntity orderItem = new OrderItemJpaEntity(
                null,
                orderRepository.getReferenceById(ORDER_ID),
                menuItemRepository.getReferenceById(MENU_ITEM_ID),
                userRepository.getReferenceById(WAITER_ID),
                new ArrayList<>(),
                observation,
                OrderItemStatus.PENDING,
                35.90,
                now,
                now
        );

        assertThatThrownBy(() -> orderItemRepository.saveAndFlush(orderItem))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @PersistenceTest
    @Transactional
    @DisplayName("Deve filtrar itens da comanda pelo status")
    void shouldFindOrderItemsByStatus() {
        var pendingItems = orderItemRepository.findByStatus(OrderItemStatus.PENDING);

        assertThat(pendingItems)
                .isNotEmpty()
                .allMatch(item -> item.getStatus() == OrderItemStatus.PENDING);
    }

    @PersistenceTest
    @Transactional
    @DisplayName("Deve persistir os adicionais do item da comanda")
    void shouldPersistOrderItemAddOns() {
        LocalDateTime now = LocalDateTime.now();
        List<AddOnJpaEntity> addOns = addOnRepository.findAllById(
                List.of(BACON_ADD_ON_ID, CHEDDAR_ADD_ON_ID)
        );
        OrderItemJpaEntity orderItem = new OrderItemJpaEntity(
                null,
                orderRepository.getReferenceById(ORDER_ID),
                menuItemRepository.getReferenceById(MENU_ITEM_ID),
                userRepository.getReferenceById(WAITER_ID),
                addOns,
                "Com adicionais",
                OrderItemStatus.PENDING,
                44.90,
                now,
                now
        );

        UUID orderItemId = orderItemRepository.saveAndFlush(orderItem).getId();
        entityManager.clear();

        OrderItemJpaEntity persistedOrderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow();

        assertThat(persistedOrderItem.getAdditions())
                .extracting(AddOnJpaEntity::getId)
                .containsExactlyInAnyOrder(BACON_ADD_ON_ID, CHEDDAR_ADD_ON_ID);
    }

    @PersistenceTest
    @Transactional
    @DisplayName("Deve impedir exclusao de adicional associado a item")
    void shouldRejectDeletionOfAddOnReferencedByOrderItem() {
        assertThatThrownBy(() -> {
            addOnRepository.deleteById(BACON_ADD_ON_ID);
            addOnRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @PersistenceTest
    @Transactional
    @DisplayName("Deve preservar o preco historico do item apos alteracao no cardapio")
    void shouldPreserveStoredOrderItemPriceAfterMenuPriceChanges() {
        double historicalPrice = 44.90;
        double updatedMenuItemPrice = 50.00;

        jdbcTemplate.update(
                "UPDATE menu_items SET price = ? WHERE id = ?",
                updatedMenuItemPrice,
                MENU_ITEM_ID
        );
        entityManager.clear();

        Double storedPrice = jdbcTemplate.queryForObject(
                "SELECT price FROM order_items WHERE id = ?",
                Double.class,
                PRICED_ORDER_ITEM_ID
        );
        var reloadedOrder = domainOrderRepository.findById(ORDER_WITH_PRICED_ITEM_ID)
                .orElseThrow();
        var reloadedOrderItem = reloadedOrder.getOrderItems().stream()
                .filter(item -> PRICED_ORDER_ITEM_ID.equals(item.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(storedPrice)
                .isCloseTo(historicalPrice, offset(0.001));
        assertThat(reloadedOrderItem.getPrice())
                .isCloseTo(historicalPrice, offset(0.001));
    }
}
