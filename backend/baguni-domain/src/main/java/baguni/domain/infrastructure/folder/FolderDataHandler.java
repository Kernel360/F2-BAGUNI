package baguni.domain.infrastructure.folder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import baguni.common.exception.base.ServiceException;
import baguni.common.exception.error_code.FolderErrorCode;
import baguni.common.exception.error_code.UserErrorCode;
import baguni.domain.model.folder.Folder;
import baguni.domain.model.user.User;
import baguni.domain.infrastructure.user.UserRepository;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import baguni.domain.infrastructure.folder.dto.FolderCommand;

@Component
@RequiredArgsConstructor
public class FolderDataHandler {

	private final FolderRepository folderRepository;
	private final FolderQuery folderQuery;
	private final UserRepository userRepository;

	@WithSpan
	@Transactional
	public void createMandatoryFolder(User user) {
		folderRepository.save(Folder.createEmptyUnclassifiedFolder(user));
		folderRepository.save(Folder.createEmptyRecycleBinFolder(user));
		folderRepository.save(Folder.createEmptyRootFolder(user));
	}

	@WithSpan
	@Transactional(readOnly = true)
	public Folder getFolder(Long folderId) {
		return folderRepository
			.findById(folderId)
			.orElseThrow(() -> new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND));
	}

	// idList에 포함된 모든 ID에 해당하는 폴더 리스트 조회, 순서를 보장하지 않음
	@WithSpan
	@Transactional(readOnly = true)
	public List<Folder> getFolderList(List<Long> folderIdList) {
		List<Folder> folderList = folderRepository.findAllById(folderIdList);
		// 조회리스트에 존재하지 않는 태그id가 존재하면 예외 발생
		if (folderList.size() != folderIdList.size()) {
			throw new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND);
		}
		return folderList;
	}

	// idList에 포함된 모든 ID에 해당하는 폴더 리스트 조회, 순서는 idList의 순서를 따름
	@WithSpan
	@Transactional(readOnly = true)
	public List<Folder> getFolderListPreservingOrder(List<Long> folderIdList) {
		List<Folder> folderList = folderRepository.findAllById(folderIdList);
		// 조회리스트에 존재하지 않는 태그id가 존재하면 예외 발생
		if (folderList.size() != folderIdList.size()) {
			throw new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND);
		}
		folderList.sort(Comparator.comparing(folder -> folderIdList.indexOf(folder.getId())));
		return folderList;
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<Folder> getFolderListByUserId(Long userId) {
		return folderRepository.findByUserId(userId);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public Folder getRootFolder(Long userId) {
		return folderQuery.findRoot(userId);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public Folder getRecycleBin(Long userId) {
		return folderQuery.findRecycleBin(userId);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public Folder getUnclassifiedFolder(Long userId) {
		return folderQuery.findUnclassified(userId);
	}

	@WithSpan
	@Transactional
	public Folder saveFolder(FolderCommand.Create command) {
		User user = userRepository
			.findById(command.userId())
			.orElseThrow(() -> new ServiceException(UserErrorCode.USER_NOT_FOUND));
		Folder parentFolder = folderRepository.findById(command.parentFolderId())
											  .orElseThrow(
												  () -> new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND));

		Folder folder = folderRepository.save(Folder.createEmptyGeneralFolder(user, parentFolder, command.name()));
		folder.getParentFolder().addChildFolderIdOrdered(folder.getId());
		return folder;
	}

	@WithSpan
	@Transactional
	public Folder updateFolder(FolderCommand.Update command) {
		Folder folder = folderRepository.findById(command.id())
										.orElseThrow(() -> new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND));
		folder.updateFolderName(command.name());

		return folder;
	}

	@WithSpan
	@Transactional
	public List<Long> moveFolderWithinParent(FolderCommand.Move command) {
		Folder parentFolder = folderRepository.findById(command.parentFolderId())
											  .orElseThrow(
												  () -> new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND));

		parentFolder.updateChildFolderIdOrderedList(command.idList(), command.orderIdx());
		return parentFolder.getChildFolderIdOrderedList();
	}

	@WithSpan
	@Transactional
	public List<Long> moveFolderToDifferentParent(FolderCommand.Move command) {
		Folder folder = folderRepository.findById(command.idList().get(0))
										.orElseThrow(() -> new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND));

		Folder oldParent = folder.getParentFolder();
		oldParent.getChildFolderIdOrderedList().removeAll(command.idList());

		Folder newParent = folderRepository.findById(command.destinationFolderId())
										   .orElseThrow(() -> new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND));
		newParent.addChildFolderIdOrderedList(command.idList(), command.orderIdx());

		List<Folder> folderList = getFolderList(command.idList());
		for (Folder moveFolder : folderList) {
			moveFolder.updateParentFolder(newParent);
		}

		return newParent.getChildFolderIdOrderedList();
	}

	@WithSpan
	@Transactional
	public void deleteFolderList(FolderCommand.Delete command) {

		List<Folder> deleteList = new ArrayList<>();

		for (Long id : command.idList()) {
			Folder folder = folderRepository.findById(id)
											.orElseThrow(() -> new ServiceException(FolderErrorCode.FOLDER_NOT_FOUND));

			Folder parentFolder = folder.getParentFolder();
			parentFolder.removeChildFolderIdOrdered(folder.getId());

			deleteList.add(folder);
		}

		folderRepository.deleteAllInBatch(deleteList);
	}
}
