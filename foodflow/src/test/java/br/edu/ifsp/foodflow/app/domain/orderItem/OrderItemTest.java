package br.edu.ifsp.foodflow.app.domain.orderItem;

import br.edu.ifsp.foodflow.app.domain.addOn.AddOn;
import br.edu.ifsp.foodflow.app.domain.menuItem.MenuItem;
import br.edu.ifsp.foodflow.app.domain.user.User;
import br.edu.ifsp.foodflow.app.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class OrderItemTest {

        @Test
        @Tag("Mutation")
        @DisplayName("Deve calcular o preço somando o item e os adicionais no segundo construtor")
        void shouldCalculatePriceWithAdditionsInSecondConstructor() {

            User user = new User(
                    UUID.randomUUID(),
                    "Ana Flávia",
                    "ana",
                    "ana@gmail.com",
                    "1234",
                    UserRole.WAITER
            );
            MenuItem menuItem = new MenuItem(
                    UUID.randomUUID(),
                    "Pizza",
                    "desc",
                    50.0,
                    1
            );
            AddOn addon = new AddOn(UUID.randomUUID(), "Queijo", 10.0);

            OrderItem item = new OrderItem(
                    UUID.randomUUID(), menuItem, List.of(addon),
                    user, "", OrderItemStatus.PENDING,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            assertThat(item.getPrice()).isEqualTo(60.0);
        }

}
