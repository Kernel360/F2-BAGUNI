package baguni.domain.infrastructure.sharedFolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

import baguni.domain.model.sharedFolder.SharedFolder;

@RestController
public interface SharedFolderRepository extends JpaRepository<SharedFolder, UUID> {

	List<SharedFolder> findByUserId(Long userId);

	Optional<SharedFolder> findByFolderId(Long folderId);

	void deleteByFolderId(Long folderId);

	void deleteByUserId(Long userId);
}
