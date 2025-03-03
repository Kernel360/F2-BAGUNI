package baguni.domain.infrastructure.sharedFolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import baguni.common.exception.base.ServiceException;
import baguni.common.exception.error_code.FolderErrorCode;
import baguni.common.exception.error_code.SharedFolderErrorCode;
import baguni.common.exception.error_code.UserErrorCode;
import baguni.domain.model.folder.Folder;
import baguni.domain.infrastructure.folder.FolderRepository;
import baguni.domain.model.sharedFolder.SharedFolder;
import baguni.domain.model.user.User;
import baguni.domain.infrastructure.user.UserRepository;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SharedFolderDataHandler {

	private final SharedFolderRepository sharedFolderRepository;
	private final UserRepository userRepository;
	private final FolderRepository folderRepository;

	@WithSpan
	@Transactional
	public SharedFolder save(Long userId, Long folderId) {
		User user = userRepository.findById(userId)
								  .orElseThrow(() -> new ServiceException(UserErrorCode.USER_NOT_FOUND));
		Folder folder = folderRepository.findById(folderId)
										.orElseThrow(() -> new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND));
		return sharedFolderRepository.save(SharedFolder.createSharedFolder(user, folder));
	}

	@WithSpan
	@Transactional(readOnly = true)
	public SharedFolder getByUUID(UUID uuid) {
		return sharedFolderRepository
			.findById(uuid)
			.orElseThrow(() -> new ServiceException(SharedFolderErrorCode.SHARED_FOLDER_NOT_FOUND));
	}

	@WithSpan
	@Transactional(readOnly = true)
	public SharedFolder getByFolderId(Long folderId) {
		return sharedFolderRepository
			.findByFolderId(folderId)
			.orElseThrow(() -> new ServiceException(SharedFolderErrorCode.SHARED_FOLDER_NOT_FOUND));
	}

	@WithSpan
	@Transactional(readOnly = true)
	public boolean isSharedFolder(Long folderId) {
		return sharedFolderRepository.findByFolderId(folderId).isPresent();
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<SharedFolder> getByUserId(Long userId) {
		return sharedFolderRepository.findByUserId(userId);
	}

	@WithSpan
	@Transactional
	public void deleteBySourceFolderId(Long sourceFolderId) {
		sharedFolderRepository.deleteByFolderId(sourceFolderId);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public Optional<UUID> findUUIDBySourceFolderId(Long sourceFolderId) {
		return sharedFolderRepository.findByFolderId(sourceFolderId).map(SharedFolder::getId);
	}
}
