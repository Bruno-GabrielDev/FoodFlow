package br.edu.ifsp.foodflow.app.persistence;

import br.edu.ifsp.foodflow.app.annotation.PersistenceTest;
import br.edu.ifsp.foodflow.app.domain.orderItem.OrderItemStatus;
import br.edu.ifsp.foodflow.app.infra.persistence.entity.OrderItemJpaEntity;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataMenuItemRepository;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataOrderItemRepository;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataOrderRepository;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataUserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de Persistência - Itens da Comanda")
class OrderItemPersistenceTest extends BasePersistenceTest {

    private static final UUID ORDER_ID =
            UUID.fromString("b4c5d6e7-f8a9-0123-bcde-234567890123");
    private static final UUID MENU_ITEM_ID =
            UUID.fromString("d4e5f6a7-b8c9-0123-defa-234567890123");
    private static final UUID WAITER_ID =
            UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    @Autowired
    private SpringDataOrderItemRepository orderItemRepository;

    @Autowired
    private SpringDataOrderRepository orderRepository;

    @Autowired
    private SpringDataMenuItemRepository menuItemRepository;

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

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
}
