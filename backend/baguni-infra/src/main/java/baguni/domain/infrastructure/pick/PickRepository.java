package baguni.domain.infrastructure.pick;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import baguni.domain.model.pick.Pick;
import baguni.domain.model.link.Link;

public interface PickRepository extends JpaRepository<Pick, Long> {

	Optional<Pick> findByUserIdAndLinkUrl(Long userId, String url);

	boolean existsByUserIdAndLink(Long userId, Link link);

	@Query("SELECT p from Pick p JOIN FETCH p.link WHERE p.id IN (:pickIdList)")
	List<Pick> findAllById_JoinLink(List<Long> pickIdList);

	@Query("SELECT p.id FROM Pick p WHERE p.user.id = :userId")
	List<Long> findIdAllByUserId(Long userId);
}
