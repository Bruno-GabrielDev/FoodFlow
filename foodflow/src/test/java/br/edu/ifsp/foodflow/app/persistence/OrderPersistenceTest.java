package br.edu.ifsp.foodflow.app.persistence;

import br.edu.ifsp.foodflow.app.annotation.PersistenceTest;
import br.edu.ifsp.foodflow.app.infra.persistence.entity.OrderJpaEntity;
import br.edu.ifsp.foodflow.app.infra.persistence.entity.TableJpaEntity;
import br.edu.ifsp.foodflow.app.infra.persistence.entity.UserJpaEntity;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataOrderRepository;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataTableRepository;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataUserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes de Persistência - Comandas")
class OrderPersistenceTest extends BasePersistenceTest {

    private static final int TABLE_NUMBER = 10;
    private static final int REFERENCED_TABLE_NUMBER = 1;
    private static final UUID USER_ID =
            UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    @Autowired
    private SpringDataOrderRepository orderRepository;

    @Autowired
    private SpringDataTableRepository tableRepository;

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @PersistenceTest
    @Transactional
    @DisplayName("Deve buscar somente a comanda ativa de uma mesa")
    void shouldFindOnlyActiveOrderByTable() {
        TableJpaEntity table = tableRepository.getReferenceById(TABLE_NUMBER);
        UserJpaEntity user = userRepository.getReferenceById(USER_ID);
        OrderJpaEntity closedOrder = createOrder(table, user, false);
        OrderJpaEntity activeOrder = createOrder(table, user, true);

        orderRepository.saveAndFlush(closedOrder);
        UUID activeOrderId = orderRepository.saveAndFlush(activeOrder).getId();
        entityManager.clear();

        OrderJpaEntity persistedOrder = orderRepository
                .findByTableAndActiveTrue(tableRepository.getReferenceById(TABLE_NUMBER))
                .orElseThrow();

        assertThat(persistedOrder.getId()).isEqualTo(activeOrderId);
        assertThat(persistedOrder.getActive()).isTrue();
    }

    @PersistenceTest
    @Transactional
    @DisplayName("Deve listar somente comandas ativas")
    void shouldListOnlyActiveOrders() {
        UserJpaEntity user = userRepository.getReferenceById(USER_ID);
        OrderJpaEntity closedOrder = createOrder(
                tableRepository.getReferenceById(9),
                user,
                false
        );
        OrderJpaEntity activeOrder = createOrder(
                tableRepository.getReferenceById(10),
                user,
                true
        );

        UUID closedOrderId = orderRepository.saveAndFlush(closedOrder).getId();
        UUID activeOrderId = orderRepository.saveAndFlush(activeOrder).getId();
        entityManager.clear();

        var activeOrders = orderRepository.findByActiveTrue();

        assertThat(activeOrders)
                .extracting(OrderJpaEntity::getId)
                .contains(activeOrderId)
                .doesNotContain(closedOrderId);
        assertThat(activeOrders)
                .allMatch(OrderJpaEntity::getActive);
    }

    @PersistenceTest
    @Transactional
    @DisplayName("Deve impedir exclusao de mesa associada a comanda")
    void shouldRejectDeletionOfTableReferencedByOrder() {
        assertThatThrownBy(() -> {
            tableRepository.deleteById(REFERENCED_TABLE_NUMBER);
            tableRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    private OrderJpaEntity createOrder(
            TableJpaEntity table,
            UserJpaEntity user,
            boolean active
    ) {
        return new OrderJpaEntity(
                null,
                table,
                user,
                new ArrayList<>(),
                LocalDateTime.now(),
                active
        );
    }
}
