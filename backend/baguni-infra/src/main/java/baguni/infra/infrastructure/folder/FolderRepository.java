package baguni.infra.infrastructure.folder;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import baguni.infra.model.folder.Folder;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface FolderRepository extends JpaRepository<Folder, Long> {

	@Lock(value = LockModeType.PESSIMISTIC_WRITE)
	@QueryHints({
		@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")
	})
	@Query("SELECT f FROM Folder f WHERE f.id=:id")
	Optional<Folder> findByIdForUpdate(Long id);

	List<Folder> findByUserId(Long userId);

	List<Folder> findByParentFolderId(Long parentFolderId);

	void deleteByUserId(Long userId);
}
