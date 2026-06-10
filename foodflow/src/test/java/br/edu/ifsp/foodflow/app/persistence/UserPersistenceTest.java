package br.edu.ifsp.foodflow.app.persistence;

import br.edu.ifsp.foodflow.app.annotation.PersistenceTest;
import br.edu.ifsp.foodflow.app.domain.user.UserRole;
import br.edu.ifsp.foodflow.app.infra.persistence.entity.UserJpaEntity;
import br.edu.ifsp.foodflow.app.infra.persistence.repository.springdata.SpringDataUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes de Persistência - Usuários")
class UserPersistenceTest extends BasePersistenceTest {

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
