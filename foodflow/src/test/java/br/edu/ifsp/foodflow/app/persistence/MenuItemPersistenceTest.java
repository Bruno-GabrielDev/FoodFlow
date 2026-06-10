package br.edu.ifsp.foodflow.app.persistence;

import br.edu.ifsp.foodflow.app.annotation.PersistenceTest;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataMenuItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes de Persistência - Itens do Cardápio")
class MenuItemPersistenceTest extends BasePersistenceTest {

    private static final UUID REFERENCED_MENU_ITEM_ID =
            UUID.fromString("d4e5f6a7-b8c9-0123-defa-234567890123");

    @Autowired
    private SpringDataMenuItemRepository menuItemRepository;

    @PersistenceTest
    @Transactional
    @DisplayName("Deve impedir exclusão de item do cardápio referenciado por uma comanda")
    void shouldRejectDeletionOfReferencedMenuItem() {
        assertThatThrownBy(() -> {
            menuItemRepository.deleteById(REFERENCED_MENU_ITEM_ID);
            menuItemRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
