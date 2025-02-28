package baguni.api.service.pick.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import baguni.common.exception.base.ServiceException;
import baguni.domain.exception.folder.FolderErrorCode;
import baguni.domain.exception.pick.PickErrorCode;
import baguni.domain.exception.tag.TagErrorCode;
import baguni.domain.infrastructure.pick.PickQuery;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import baguni.domain.annotation.LoginUserIdDistributedLock;
import baguni.domain.infrastructure.pick.dto.PickCommand;
import baguni.domain.infrastructure.pick.dto.PickMapper;
import baguni.domain.infrastructure.pick.dto.PickResult;
import baguni.api.service.ranking.service.RankingService;
import baguni.domain.infrastructure.folder.FolderDataHandler;
import baguni.domain.infrastructure.link.LinkDataHandler;
import baguni.domain.infrastructure.pick.PickDataHandler;
import baguni.domain.infrastructure.tag.TagDataHandler;
import baguni.domain.model.folder.Folder;
import baguni.domain.model.folder.FolderType;
import baguni.domain.model.pick.Pick;
import baguni.domain.model.tag.Tag;

@Slf4j
@Service
@RequiredArgsConstructor
public class PickService {

	private final TagDataHandler tagDataHandler;
	private final PickDataHandler pickDataHandler;
	private final PickQuery pickQuery;
	private final PickMapper pickMapper;
	private final FolderDataHandler folderDataHandler;
	private final LinkDataHandler linkDataHandler;

	@WithSpan
	@Transactional(readOnly = true)
	public boolean existPickByUrl(Long userId, String url) {
		return linkDataHandler.getOptionalLink(url)
							  .map(link -> pickDataHandler.existsByUserIdAndLink(userId, link))
							  .orElse(false);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public PickResult.Pick getPick(PickCommand.Read command) {
		assertUserIsPickOwner(command.userId(), command.id());
		var pick = pickDataHandler.getPick(command.id());
		return pickMapper.toPickResult(pick);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public Optional<PickResult.Pick> findPickUrl(Long userId, String url) {
		return pickDataHandler.findPickUrl(userId, url).map(pickMapper::toPickResult);
	}

	// 폴더 내에 있는 픽 리스트 조회
	// 구현은 해두었지만, 추후 사용되지 않을 때 삭제 예정
	@WithSpan
	@Transactional(readOnly = true)
	public List<PickResult.Pick> getFolderChildPickList(Long userId, Long folderId) {
		assertUserIsFolderOwner(userId, folderId);
		Folder folder = folderDataHandler.getFolder(folderId);
		List<Pick> pickList = pickDataHandler.getPickListPreservingOrder(folder.getChildPickIdOrderedList());

		return pickList.stream()
					   .map(pickMapper::toPickResult)
					   .toList();
	}

	// 폴더 리스트가 넘어오면, 각 폴더 내부에 있는 픽 리스트 조회
	@WithSpan
	@Transactional(readOnly = true)
	public List<PickResult.FolderPickList> getFolderListChildPickList(PickCommand.ReadList command) {
		return command.folderIdList().stream()
					  .peek(folderId -> assertUserIsFolderOwner(command.userId(), folderId))  // 폴더 접근 유효성 검사
					  .map(this::getFolderChildPickResultList)
					  .toList();
	}

	@WithSpan
	@Transactional(readOnly = true)
	public Slice<PickResult.Pick> getFolderListChildPickPagination(PickCommand.ReadPagination command) {
		assertUserIsFolderOwner(command.userId(), command.folderId());
		return pickQuery.getFolderChildPickPagination(command.userId(),
			command.folderId(), command.cursor(), command.size());
	}

	@WithSpan
	@Transactional
	@LoginUserIdDistributedLock
	public PickResult.Pick saveNewPick(PickCommand.Create command) {
		assertParentFolderIsNotRoot(command.parentFolderId());
		assertUserIsFolderOwner(command.userId(), command.parentFolderId());
		assertUserIsTagOwner(command.userId(), command.tagIdOrderedList());
		return pickMapper.toPickResult(pickDataHandler.savePick(command));
	}

	@WithSpan
	@Transactional
	@LoginUserIdDistributedLock
	public PickResult.Extension savePickFromExtension(PickCommand.CreateFromExtension command) {
		assertParentFolderIsNotRoot(command.parentFolderId());
		assertUserIsFolderOwner(command.userId(), command.parentFolderId());
		assertUserIsTagOwner(command.userId(), command.tagIdOrderedList());
		return pickMapper.toExtensionResult(pickDataHandler.savePickFromExtension(command));
	}

	/**
	 * 익스텐션에서 사용하지 않게 되는 경우, 제거 예정
	 */
	@WithSpan
	@Transactional
	@LoginUserIdDistributedLock
	public PickResult.Extension savePickToUnclassified(PickCommand.Extension command) {
		return pickMapper.toExtensionResult(pickDataHandler.savePickToUnclassified(command));
	}

	@WithSpan
	@Transactional
	@LoginUserIdDistributedLock
	public PickResult.Pick updatePick(PickCommand.Update command) {
		if (Objects.nonNull(command.parentFolderId())) {
			assertUserIsFolderOwner(command.userId(), command.parentFolderId());
			assertParentFolderIsNotRoot(command.parentFolderId());
		}
		assertUserIsPickOwner(command.userId(), command.id());
		assertUserIsTagOwner(command.userId(), command.tagIdOrderedList());

		var pick = pickDataHandler.updatePick(command);
		return pickMapper.toPickResult(pick);
	}

	@WithSpan
	@Transactional
	@LoginUserIdDistributedLock
	public void movePick(PickCommand.Move command) {
		assertParentFolderIsNotRoot(command.destinationFolderId());
		List<Pick> pickList = pickDataHandler.getPickListPreservingOrder(command.idList());
		for (Pick pick : pickList) {
			assertUserIsPickOwner(command.userId(), pick.getId());
		}

		if (isParentFolderChanged(pickList.get(0).getParentFolder().getId(), command.destinationFolderId())) {
			pickDataHandler.movePickToOtherFolder(command);
			return;
		}
		pickDataHandler.movePickToCurrentFolder(command);
	}

	@WithSpan
	@Transactional
	@LoginUserIdDistributedLock
	public void deletePick(PickCommand.Delete command) {
		List<Pick> pickList = pickDataHandler.getPickList(command.idList());
		for (Pick pick : pickList) {
			assertUserIsPickOwner(command.userId(), pick.getId());
			if (pick.getParentFolder().getFolderType() != FolderType.RECYCLE_BIN) {
				throw new ServiceException(PickErrorCode.PICK_DELETE_NOT_ALLOWED);
			}
		}

		pickDataHandler.deletePickList(command);
	}

	@WithSpan
	@Transactional
	@LoginUserIdDistributedLock
	public void deletePickFromRecycleBin(Long userId) {
		pickDataHandler.deletePickFromRecycleBin(userId);
	}

	/**
	 * Internal Helper Functions
	 **/
	private PickResult.FolderPickList getFolderChildPickResultList(Long folderId) {
		Folder folder = folderDataHandler.getFolder(folderId);
		List<Pick> pickList = pickDataHandler.getPickListPreservingOrder(folder.getChildPickIdOrderedList());

		List<PickResult.Pick> pickResultList
			= pickList.stream()
					  .map(pickMapper::toPickResult)
					  .toList();
		return pickMapper.toPickResultList(folderId, pickResultList);
	}

	private boolean isParentFolderChanged(Long originalFolderId, Long destinationFolderId) {
		return ObjectUtils.notEqual(originalFolderId, destinationFolderId);
	}

	private void assertUserIsPickOwner(Long userId, Long pickId) {
		var pick = pickDataHandler.getPick(pickId);
		if (ObjectUtils.notEqual(userId, pick.getUser().getId())) {
			throw new ServiceException(PickErrorCode.PICK_UNAUTHORIZED_USER_ACCESS);
		}
	}

	private void assertUserIsFolderOwner(Long userId, Long parentFolderId) {
		if (Objects.isNull(parentFolderId)) {
			throw new ServiceException(FolderErrorCode.INVALID_PARENT_FOLDER);
		}

		Folder parentFolder = folderDataHandler.getFolder(parentFolderId);
		if (ObjectUtils.notEqual(userId, parentFolder.getUser().getId())) {
			throw new ServiceException(FolderErrorCode.FOLDER_ACCESS_DENIED);
		}
	}

	private void assertParentFolderIsNotRoot(Long parentFolderId) {
		if (Objects.isNull(parentFolderId)) {
			throw new ServiceException(FolderErrorCode.INVALID_PARENT_FOLDER);
		}

		Folder parentFolder = folderDataHandler.getFolder(parentFolderId);
		if (parentFolder.getFolderType() == FolderType.ROOT) {
			throw new ServiceException(PickErrorCode.PICK_UNAUTHORIZED_ROOT_ACCESS);
		}
	}

	private void assertUserIsTagOwner(Long userId, List<Long> tagIdList) {
		for (Tag tag : tagDataHandler.getTagList(tagIdList)) {
			if (ObjectUtils.notEqual(userId, tag.getUser().getId())) {
				throw new ServiceException(TagErrorCode.UNAUTHORIZED_TAG_ACCESS);
			}
		}
	}
}
