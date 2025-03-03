package baguni.domain.infrastructure.tag;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import baguni.domain.model.tag.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

	boolean existsByUserIdAndName(Long userId, String name);

	List<Tag> findAllByUserId(Long userId);

	void deleteByIdAndUserId(Long id, Long userId);

	void deleteByUserId(Long userId);
}
