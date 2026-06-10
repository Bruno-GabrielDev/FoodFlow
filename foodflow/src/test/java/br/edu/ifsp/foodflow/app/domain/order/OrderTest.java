package br.edu.ifsp.foodflow.app.domain.order;

import br.edu.ifsp.foodflow.app.domain.table.Table;
import br.edu.ifsp.foodflow.app.domain.user.User;
import br.edu.ifsp.foodflow.app.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Tag("UnitTest")
class OrderTest {

    @Tag("Structural")
    @Nested
    @DisplayName("Testes Estruturais")
    class StructuralTests{

        @DisplayName("Deve lançar IllegalArgumentException se mesa for nula")
        @Test
        void shouldThrowIllegalArgumentExceptionIfTableIsNull(){
            User user = new User(
                    UUID.randomUUID(),
                    "ana lívia",
                    "ana",
                    "ana@gmail",
                    "1234",
                    UserRole.WAITER);
            assertThatIllegalArgumentException().isThrownBy(()->new Order(null,user));
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException se usuário for nulo")
        void shouldThrowIllegalArgumentExceptionIfUserIsNull() {
            Table table = new Table(1);
            assertThatIllegalArgumentException().isThrownBy(()->new Order(table,null));
        }

    }

}
