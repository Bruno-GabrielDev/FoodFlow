package br.edu.ifsp.foodflow.app.application.useCases.addOn;

import br.edu.ifsp.foodflow.app.domain.addOn.AddOn;
import br.edu.ifsp.foodflow.app.domain.addOn.AddOnRepository;
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
class ListAddOnsUseCaseTest {

    @InjectMocks
    private ListAddOnsUseCase listAddOnsUseCase;

    @Mock
    private AddOnRepository addOnRepository;

    @Tag("Mutation")
    @Test
    @DisplayName("deve testar lista com vários adicionais")
    void shouldReturnMultipleAddOnsInList(){
        List<AddOn> addOns = List.of(
                new AddOn("milho",3.5),
                new AddOn("tomate",2.0),
                new AddOn("alface",1.5)
        );
        when(addOnRepository.findAll()).thenReturn(addOns);
        assertThat(listAddOnsUseCase.execute()).isEqualTo(addOns);

    }

}
