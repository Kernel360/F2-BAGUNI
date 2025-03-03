package baguni.api.application.folder.controller;

import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import baguni.api.application.folder.dto.FolderApiMapper;
import baguni.api.application.folder.dto.FolderApiRequest;
import baguni.api.application.folder.dto.FolderApiResponse;
import baguni.api.service.folder.service.FolderService;
import baguni.api.service.sharedFolder.service.SharedFolderService;
import baguni.infra.infrastructure.folder.dto.FolderCommand;
import baguni.infra.infrastructure.folder.dto.FolderResult;
import baguni.infra.model.folder.FolderType;

@DisplayName("폴더 컨트롤러 - 단위 테스트")
@ExtendWith(MockitoExtension.class)
class FolderApiControllerUnitTest {

	@Mock
	private FolderService folderService;

	@Mock
	private SharedFolderService sharedFolderService;

	@Mock
	private FolderApiMapper folderApiMapper;

	@InjectMocks
	private FolderApiController folderApiController;

	Long userId, folderId, otherFolderId, rootId, unclassifiedId, recycleBinId, sharedFolderId;
	String uuidToken;

	@BeforeEach
	void setUp() {
		userId = 1L;
		rootId = 1L; unclassifiedId = 2L; recycleBinId = 3L; folderId = 4L; otherFolderId = 5L;
		sharedFolderId = 1L;
		uuidToken = UUID.randomUUID().toString();
	}

	@Test
	@DisplayName("루트 폴더와 하위 리스트 조회")
	void all_root_folder() {
		// given
		List<FolderResult> folderResultList = List.of(
			new FolderResult(rootId, "root", FolderType.ROOT, null, List.of(folderId, otherFolderId), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()),
			new FolderResult(folderId, "폴더1", FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()),
			new FolderResult(otherFolderId, "폴더2", FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now())
		);

		for (FolderResult folderResult : folderResultList) {
			String folderAccessToken = UUID.randomUUID().toString();
			Optional<String> optionalAccessToken = Optional.of(folderAccessToken);
			given(sharedFolderService.findFolderAccessTokenByFolderId(folderResult.id())).willReturn(optionalAccessToken);
			given(folderApiMapper.toApiResponse(folderResult, folderAccessToken)).willReturn(new FolderApiResponse(folderResult.id(), folderResult.name(), folderResult.folderType(), folderResult.parentFolderId(), folderResult.childFolderIdOrderedList(), folderResult.createdAt(), folderResult.updatedAt(), folderAccessToken));
		}

		given(folderService.getAllRootFolderList(userId)).willReturn(folderResultList);

		// when
		folderApiController.getAllRootFolderList(userId);

		// then
		then(sharedFolderService).should(times(3)).findFolderAccessTokenByFolderId(any());
		then(folderService).should(times(1)).getAllRootFolderList(userId);
		then(folderApiMapper).should(times(3)).toApiResponse(any(), any());
	}

	@Test
	@DisplayName("기본 폴더 리스트 조회")
	void basic_folder() {
		// given
		List<FolderResult> folderResultList = List.of(
			new FolderResult(rootId, "root", FolderType.ROOT, null, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()),
			new FolderResult(unclassifiedId, "unclassified", FolderType.UNCLASSIFIED, null, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()),
			new FolderResult(recycleBinId, "recycleBin", FolderType.RECYCLE_BIN, null, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now())
		);

		for (FolderResult folderResult : folderResultList) {
			String folderAccessToken = UUID.randomUUID().toString();
			var response = new FolderApiResponse(folderResult.id(), folderResult.name(), folderResult.folderType(), folderResult.parentFolderId(), folderResult.childFolderIdOrderedList(), folderResult.createdAt(), folderResult.updatedAt(), folderAccessToken);
			given(folderApiMapper.toApiResponse(folderResult)).willReturn(response);
		}

		given(folderService.getBasicFolderList(userId)).willReturn(folderResultList);

		// when
		folderApiController.getBasicFolderList(userId);

		// then
		then(folderApiMapper).should(times(3)).toApiResponse(any());
		then(folderService).should(times(1)).getBasicFolderList(userId);
	}

	@Test
	@DisplayName("폴더 생성")
	void create_folder() {
		// given
		var request = new FolderApiRequest.Create("새 폴더", rootId);
		var command = new FolderCommand.Create(userId, request.name(), request.parentFolderId());
		var result = new FolderResult(folderId, command.name(), FolderType.GENERAL, command.parentFolderId(), new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now());
		var response = new FolderApiResponse(folderId, command.name(), FolderType.GENERAL, command.parentFolderId(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now(), uuidToken);

		given(folderApiMapper.toCreateCommand(userId, request)).willReturn(command);
		given(folderService.saveFolder(command)).willReturn(result);
		given(folderApiMapper.toApiResponse(result)).willReturn(response);

		// when
		folderApiController.createFolder(userId, request);

		// then
		then(folderApiMapper).should(times(1)).toCreateCommand(userId, request);
		then(folderService).should(times(1)).saveFolder(command);
		then(folderApiMapper).should(times(1)).toApiResponse(result);
	}

	@Test
	@DisplayName("폴더 수정")
	void update_folder() {
		// given
		var request = new FolderApiRequest.Update(folderId, "변경");
		var command = new FolderCommand.Update(userId, request.id(), request.name());

		given(folderApiMapper.toUpdateCommand(userId, request)).willReturn(command);

		// when
		folderApiController.updateFolder(userId, request);

		// then
		then(folderApiMapper).should(times(1)).toUpdateCommand(userId, request);
		then(folderService).should(times(1)).updateFolder(command);
	}

	@Test
	@DisplayName("폴더 이동")
	void move_folder() {
		// given
		List<Long> folderIdList = List.of(folderId, otherFolderId);
		var request = new FolderApiRequest.Move(folderIdList, rootId, rootId, 0);
		var command = new FolderCommand.Move(userId, folderIdList, rootId, rootId, 0);

		given(folderApiMapper.toMoveCommand(userId, request)).willReturn(command);

		// when
		folderApiController.moveFolder(userId, request);

		// then
		then(folderApiMapper).should(times(1)).toMoveCommand(userId, request);
		then(folderService).should(times(1)).moveFolder(command);
	}

	@Test
	@DisplayName("폴더 삭제")
	void delete_folder() {
		// given
		List<Long> folderIdList = List.of(folderId);
		var request = new FolderApiRequest.Delete(folderIdList);
		var command = new FolderCommand.Delete(userId, folderIdList);

		given(folderApiMapper.toDeleteCommand(userId, request)).willReturn(command);

		// when
		folderApiController.deleteFolder(userId, request);

		// then
		then(folderApiMapper).should(times(1)).toDeleteCommand(userId, request);
		then(folderService).should(times(1)).deleteFolder(command);
	}
}