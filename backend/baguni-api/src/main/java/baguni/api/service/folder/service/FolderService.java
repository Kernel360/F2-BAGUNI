package baguni.api.service.folder.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import baguni.common.exception.base.ServiceException;
import baguni.common.exception.error_code.FolderErrorCode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import baguni.infra.annotation.LoginUserIdDistributedLock;
import baguni.infra.infrastructure.folder.dto.FolderCommand;
import baguni.infra.infrastructure.folder.dto.FolderMapper;
import baguni.infra.infrastructure.folder.dto.FolderResult;
import baguni.infra.infrastructure.folder.FolderDataHandler;
import baguni.infra.infrastructure.pick.PickDataHandler;
import baguni.infra.infrastructure.sharedFolder.SharedFolderDataHandler;
import baguni.infra.model.folder.Folder;
import baguni.infra.model.folder.FolderType;

@Service
@RequiredArgsConstructor
public class FolderService {

	private final FolderDataHandler folderDataHandler;
	private final SharedFolderDataHandler sharedFolderDataHandler;
	private final FolderMapper folderMapper;
	private final PickDataHandler pickDataHandler;

	// TODO: 현재는 1depth만 반환하고 있지만, 추후 ~3depth까지 반환할 예정
	@WithSpan
	@Transactional(readOnly = true)
	public List<FolderResult> getAllRootFolderList(Long userId) {
		Folder rootFolder = folderDataHandler.getRootFolder(userId);

		List<FolderResult> folderList = new ArrayList<>();
		folderList.add(folderMapper.toResult(rootFolder));

		rootFolder.getChildFolderIdOrderedList().stream()
				  .map(folderDataHandler::getFolder)
				  .map(folderMapper::toResult)
				  .forEach(folderList::add);

		return folderList;
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<FolderResult> getBasicFolderList(Long userId) {
		var basicFolders = List.of(
			folderDataHandler.getRootFolder(userId),
			folderDataHandler.getUnclassifiedFolder(userId),
			folderDataHandler.getRecycleBin(userId)
		);

		return basicFolders.stream()
						   .map(folderMapper::toResult)
						   .toList();
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<FolderResult> getMobileFolderList(Long userId) {
		List<FolderResult> folderList = new ArrayList<>(
			List.of(
				folderMapper.toResult(folderDataHandler.getUnclassifiedFolder(userId)),
				folderMapper.toResult(folderDataHandler.getRecycleBin(userId))
			)
		);

		Folder rootFolder = folderDataHandler.getRootFolder(userId);
		rootFolder.getChildFolderIdOrderedList().stream()
				  .map(folderDataHandler::getFolder)
				  .map(folderMapper::toResult)
				  .forEach(folderList::add);
		return folderList;
	}

	/**
	 * 생성하려는 폴더가 미분류폴더, 휴지통이 아닌지 검증합니다.
	 * */
	@WithSpan
	@Transactional
	@LoginUserIdDistributedLock
	public FolderResult saveFolder(FolderCommand.Create command) {
		Folder parentFolder = folderDataHandler.getFolder(command.parentFolderId());
		assertUserIsFolderOwner(command.userId(), parentFolder);
		assertDestinationIsRootFolder(parentFolder);

		return folderMapper.toResult(folderDataHandler.saveFolder(command));
	}

	@WithSpan
	@Transactional
	public FolderResult updateFolder(FolderCommand.Update command) {

		Folder folder = folderDataHandler.getFolder(command.id());

		assertUserIsFolderOwner(command.userId(), folder);
		assertFolderIsGeneralFolder(folder);

		return folderMapper.toResult(folderDataHandler.updateFolder(command));
	}

	/**
	 * 현재 폴더들의 부모가 같은지 검증합니다.
	 * 이동하려는 폴더가 미분류폴더, 휴지통이 아닌지 검증합니다.
	 * */
	@WithSpan
	@Transactional
	@LoginUserIdDistributedLock
	public void moveFolder(FolderCommand.Move command) {
		Folder destinationFolder = folderDataHandler.getFolder(command.destinationFolderId());
		assertUserIsFolderOwner(command.userId(), destinationFolder);
		assertDestinationIsRootFolder(destinationFolder);

		List<Folder> folderList = folderDataHandler.getFolderList(command.idList());
		for (Folder folder : folderList) {
			assertUserIsFolderOwner(command.userId(), folder);
			assertFolderIsGeneralFolder(folder);
		}

		assertParentFolderIsNotChanged(folderList, command.parentFolderId());
		if (Objects.equals(command.parentFolderId(), command.destinationFolderId())) {
			folderDataHandler.moveFolderWithinParent(command);
		} else {
			folderDataHandler.moveFolderToDifferentParent(command);
		}
	}

	@WithSpan
	@Transactional
	@LoginUserIdDistributedLock
	public void deleteFolder(FolderCommand.Delete command) {
		// 먼저 공유 부터 해제
		command.idList().forEach(sharedFolderDataHandler::deleteBySourceFolderId);
		// 휴지통으로 이동되어야할 픽 리스트
		List<Long> targetPickIdList = new ArrayList<>();
		// 삭제할 폴더 리스트
		List<Folder> targetFolderList = folderDataHandler.getFolderList(command.idList());
		for (Folder folder : targetFolderList) {
			assertUserIsFolderOwner(command.userId(), folder);
			assertFolderIsGeneralFolder(folder);
		}
		// db 재귀조회를 하지 않기위해 본인 폴더를 모두 가져와서 Map 형태로 들고있음.
		// TODO: queryDSL을 사용해서 최적화 할 수 있으면 좋을거같음.
		Map<Long, Folder> folderMap = folderDataHandler.getFolderListByUserId(command.userId())
													   .stream()
													   .collect(Collectors.toMap(Folder::getId, folder -> folder));

		// BFS로 자식폴더들을 순회하며 삭제할 폴더 리스트와 휴지통으로 보낼 픽 리스트를 얻음
		Queue<Folder> queue = new ArrayDeque<>(targetFolderList);
		while (!queue.isEmpty()) {
			Folder folder = queue.poll();
			for (Long childFolderId : folder.getChildFolderIdOrderedList()) {
				Folder childFolder = folderMap.get(childFolderId);
				targetFolderList.add(childFolder);
				queue.add(childFolder);
			}
			targetPickIdList.addAll(folder.getChildPickIdOrderedList());
		}

		pickDataHandler.movePickListToRecycleBin(command.userId(), targetPickIdList);
		folderDataHandler.deleteFolderList(command);
	}

	private void assertUserIsFolderOwner(Long userId, Folder folder) {
		if (!folder.getUser().getId().equals(userId)) {
			throw new ServiceException(FolderErrorCode.FOLDER_ACCESS_DENIED);
		}
	}

	private void assertFolderIsGeneralFolder(Folder folder) {
		if (FolderType.GENERAL != folder.getFolderType()) {
			throw new ServiceException(FolderErrorCode.BASIC_FOLDER_CANNOT_CHANGED);
		}
	}

	/**
	 * 같은 폴더 내에서 순서를 변경하는 경우에 검증로직입니다.
	 * 이동하려는 폴더들의 부모가 실제 부모폴더와 일치하는지 검증합니다.
	 * */
	private void assertParentFolderIsNotChanged(List<Folder> folderList, Long parentFolderId) {
		for (Folder folder : folderList) {
			Folder parentFolder = folder.getParentFolder();
			if (ObjectUtils.notEqual(parentFolder.getId(), parentFolderId)) {
				throw new ServiceException(FolderErrorCode.INVALID_PARENT_FOLDER);
			}
		}
	}

	/**
	 * 폴더 생성, 이동시 미분류폴더, 휴지통으로는 이동할 수 없습니다.
	 * */
	private void assertDestinationIsRootFolder(Folder destinationFolder) {
		if (Objects.equals(destinationFolder.getFolderType(), FolderType.UNCLASSIFIED)) {
			throw new ServiceException(FolderErrorCode.INVALID_TARGET);
		}
		if (Objects.equals(destinationFolder.getFolderType(), FolderType.RECYCLE_BIN)) {
			throw new ServiceException(FolderErrorCode.INVALID_TARGET);
		}
	}
}
