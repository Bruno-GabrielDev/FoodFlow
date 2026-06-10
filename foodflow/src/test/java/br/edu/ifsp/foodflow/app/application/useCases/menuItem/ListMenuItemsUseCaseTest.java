package br.edu.ifsp.foodflow.app.application.useCases.menuItem;

import br.edu.ifsp.foodflow.app.domain.menuItem.MenuItem;
import br.edu.ifsp.foodflow.app.domain.menuItem.MenuItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class ListMenuItemsUseCaseTest {
    @InjectMocks
    private ListMenuItemsUseCase listMenuItemsUseCase;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Tag("Mutation")
    @Test
    @DisplayName("deve testar lista com vários itens")
    void shouldReturnMultipleMenuItemsInList() {
        List<MenuItem> menuItems = List.of(
                new MenuItem(UUID.randomUUID(),"Pizza","pequena",20.0,4),
                new MenuItem(UUID.randomUUID(),"Salada","pequena",10.0,3),
                new MenuItem(UUID.randomUUID(),"Macarrão","pequena",40.0,1)
        );
        when(menuItemRepository.findAll()).thenReturn(menuItems);
        assertThat(listMenuItemsUseCase.execute()).isEqualTo(menuItems);

    }

}
