package br.edu.ifsp.foodflow.app.application.useCases.table;

import br.edu.ifsp.foodflow.app.domain.table.Table;
import br.edu.ifsp.foodflow.app.domain.table.TableRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class ListTablesUseCaseTest {
    @InjectMocks
    private ListTablesUseCase listTablesUseCase;

    @Mock
    private TableRepository tableRepository;

    @Tag("Mutation")
    @Test
    @DisplayName("deve testar lista com várias mesas")
    void shouldReturnMultipleTablesInList(){
        List<Table> tables = List.of(
                new Table(1),
                new Table(2),
                new Table(3)
        );
        when(tableRepository.findAll()).thenReturn(tables);
        assertThat(listTablesUseCase.execute()).isEqualTo(tables);

    }

}
