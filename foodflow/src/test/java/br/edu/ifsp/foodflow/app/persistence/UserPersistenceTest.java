package br.edu.ifsp.foodflow.app.persistence;

import br.edu.ifsp.foodflow.app.annotation.PersistenceTest;
import br.edu.ifsp.foodflow.app.domain.user.UserRole;
import br.edu.ifsp.foodflow.app.infra.persistence.entity.UserJpaEntity;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes de Persistência - Usuários")
class UserPersistenceTest extends BasePersistenceTest {

    private static final UUID EXISTING_USER_ID =
            UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    @Autowired
    private SpringDataUserRepository userRepository;

    @PersistenceTest
    @Transactional
    @DisplayName("Deve rejeitar dois usuários com o mesmo username")
    void shouldRejectUsersWithSameUsername() {
        UserJpaEntity firstUser = createUser("username_repetido", "primeiro@test.com");
        UserJpaEntity secondUser = createUser("username_repetido", "segundo@test.com");

        userRepository.saveAndFlush(firstUser);

        assertThatThrownBy(() -> userRepository.saveAndFlush(secondUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @PersistenceTest
    @Transactional
    @DisplayName("Deve rejeitar dois usuários com o mesmo email")
    void shouldRejectUsersWithSameEmail() {
        UserJpaEntity firstUser = createUser("primeiro_usuario", "email_repetido@test.com");
        UserJpaEntity secondUser = createUser("segundo_usuario", "email_repetido@test.com");

        userRepository.saveAndFlush(firstUser);

        assertThatThrownBy(() -> userRepository.saveAndFlush(secondUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @PersistenceTest
    @Transactional
    @DisplayName("Deve buscar usuario por username existente")
    void shouldFindUserByExistingUsername() {
        UserJpaEntity user = userRepository.findByUsername("joao")
                .orElseThrow();

        assertThat(user.getId()).isEqualTo(EXISTING_USER_ID);
        assertThat(user.getUsername()).isEqualTo("joao");
        assertThat(user.getEmail()).isEqualTo("joao@gmail.com");
    }

    @PersistenceTest
    @Transactional
    @DisplayName("Deve impedir exclusao de usuario associado a comanda")
    void shouldRejectDeletionOfUserReferencedByOrder() {
        assertThatThrownBy(() -> {
            userRepository.deleteById(EXISTING_USER_ID);
            userRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    private UserJpaEntity createUser(String username, String email) {
        return new UserJpaEntity(
                null,
                "Usuário de Teste",
                username,
                email,
                "senha123",
                UserRole.WAITER
        );
    }
}
