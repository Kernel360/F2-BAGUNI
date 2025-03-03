package baguni.api.service.pick.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import baguni.common.exception.base.ServiceException;
import baguni.common.exception.error_code.FolderErrorCode;
import baguni.common.exception.error_code.TagErrorCode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import baguni.domain.infrastructure.pick.dto.PickCommand;
import baguni.domain.infrastructure.pick.dto.PickResult;
import baguni.domain.infrastructure.folder.FolderDataHandler;
import baguni.domain.infrastructure.pick.PickQuery;
import baguni.domain.infrastructure.tag.TagDataHandler;
import baguni.domain.model.folder.Folder;
import baguni.domain.model.folder.FolderType;
import baguni.domain.model.tag.Tag;

@Service
@RequiredArgsConstructor
public class PickSearchService {

	private final PickQuery pickQuery;
	private final FolderDataHandler folderDataHandler;
	private final TagDataHandler tagDataHandler;

	@WithSpan
	@Transactional(readOnly = true)
	public Slice<PickResult.Pick> searchPickPagination(PickCommand.SearchPagination command) {

		List<Long> folderIdList = command.folderIdList();
		if (ObjectUtils.isNotEmpty(folderIdList)) {
			folderIdList.removeAll(Collections.singletonList(null));
			for (Long folderId : folderIdList) {
				assertUserIsFolderOwner(command.userId(), folderId);
				assertSearchTargetIsNotRootFolder(folderId);
			}
		}

		List<Long> tagIdList = command.tagIdList();
		if (ObjectUtils.isNotEmpty(tagIdList)) {
			tagIdList.removeAll(Collections.singletonList(null));
			for (Long tagId : tagIdList) {
				assertUserIsTagOwner(command.userId(), tagId);
			}
		}

		List<String> searchTokenList = command.searchTokenList();
		if (ObjectUtils.isNotEmpty(searchTokenList)) {
			searchTokenList.removeAll(Arrays.asList("", null));
		}

		return pickQuery.searchPickPagination(
			command.userId(),
			folderIdList, searchTokenList, tagIdList,
			command.cursor(), command.size()
		);
	}

	private void assertUserIsFolderOwner(Long userId, Long folderId) {
		Folder parentFolder = folderDataHandler.getFolder(folderId); // 존재하지 않으면, FOLDER_NOT_FOUND
		if (ObjectUtils.notEqual(userId, parentFolder.getUser().getId())) {
			throw new ServiceException(FolderErrorCode.FOLDER_ACCESS_DENIED);
		}
	}

	private void assertSearchTargetIsNotRootFolder(Long folderId) {
		Folder parentFolder = folderDataHandler.getFolder(folderId); // 존재하지 않으면, FOLDER_NOT_FOUND
		if (Objects.equals(parentFolder.getFolderType(), FolderType.ROOT)) {
			throw new ServiceException(FolderErrorCode.ROOT_FOLDER_SEARCH_NOT_ALLOWED);
		}
	}

	private void assertUserIsTagOwner(Long userId, Long tagId) {
		Tag tag = tagDataHandler.getTag(tagId); // 존재하지 않으면, TAG_NOT_FOUND
		if (!userId.equals(tag.getUser().getId())) {
			throw new ServiceException(TagErrorCode.UNAUTHORIZED_TAG_ACCESS);
		}
	}
}
