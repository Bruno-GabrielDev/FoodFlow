package br.edu.ifsp.foodflow.app.persistence;

import br.edu.ifsp.foodflow.app.annotation.PersistenceTest;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

@DisplayName("Testes de Persistência - Migrations")
class MigrationPersistenceTest extends BasePersistenceTest {

    private static final UUID ORDER_ITEM_ID =
            UUID.fromString("22220001-2222-2222-2222-222222222222");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceTest
    @DisplayName("Deve armazenar o preço do item com a soma dos adicionais")
    void shouldStoreOrderItemPriceWithAddOnSum() {
        Double storedPrice = jdbcTemplate.queryForObject(
                "SELECT price FROM order_items WHERE id = ?",
                Double.class,
                ORDER_ITEM_ID
        );
        Double menuItemPrice = jdbcTemplate.queryForObject(
                """
                SELECT mi.price
                FROM order_items oi
                JOIN menu_items mi ON mi.id = oi.menu_item_id
                WHERE oi.id = ?
                """,
                Double.class,
                ORDER_ITEM_ID
        );
        Double addOnTotal = jdbcTemplate.queryForObject(
                """
                SELECT COALESCE(SUM(ao.price), 0)
                FROM order_item_addons oia
                JOIN add_ons ao ON ao.id = oia.add_on_id
                WHERE oia.order_item_id = ?
                """,
                Double.class,
                ORDER_ITEM_ID
        );

        assertThat(storedPrice)
                .isCloseTo(menuItemPrice + addOnTotal, offset(0.001));
    }
}
