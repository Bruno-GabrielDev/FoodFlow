package br.edu.ifsp.foodflow.app.application.useCases.user;

import br.edu.ifsp.foodflow.app.domain.user.User;
import br.edu.ifsp.foodflow.app.domain.user.UserRepository;
import br.edu.ifsp.foodflow.app.domain.user.UserRole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {
    private User user;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

    @BeforeEach
    void setUp(){
        user = new User(
                UUID.randomUUID(),
                "Bia alves",
                "bia",
                "bia@email.com",
                "123",
                UserRole.WAITER
        );
    }

    @Tag("Structural")
    @Nested
    @DisplayName("Testes Estruturais")
    class StructuralTests{
        @Test
        @DisplayName("Deve lançar exceção quando username já existe")
        void shouldThrowExceptionWhenUsernameAlreadyExists() {

            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

            assertThatThrownBy(() ->
                    registerUserUseCase.execute(
                            user.getName(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getPassword(),
                            user.getRole()
                    )
            ).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Deve salvar usuário com senha criptografada quando username não existe")
        void shouldSaveUserWithEncryptedPassword() {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(user.getPassword())).thenReturn("$2a$hash");

            registerUserUseCase.execute(
                    user.getName(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole()
            );

            verify(passwordEncoder).encode(user.getPassword());
            verify(userRepository).save(any(User.class));
        }
    }

}
