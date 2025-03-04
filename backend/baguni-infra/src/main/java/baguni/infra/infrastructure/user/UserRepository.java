package baguni.infra.infrastructure.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import baguni.infra.model.user.Role;
import baguni.infra.model.user.SocialProvider;
import baguni.infra.model.user.User;
import baguni.infra.model.util.IDToken;

public interface UserRepository extends JpaRepository<User, Long> {

	Long countByRole(Role role);

	Optional<User> findBySocialProviderAndSocialProviderId(SocialProvider socialProvider, String socialProviderId);

	Optional<User> findByIdToken(IDToken idToken);
}
