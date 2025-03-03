package baguni.api.application.sharedFolder.controller;

import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import baguni.api.application.sharedFolder.dto.SharedFolderApiMapper;
import baguni.api.application.sharedFolder.dto.SharedFolderApiResponse;
import baguni.api.service.sharedFolder.service.SharedFolderService;
import baguni.infra.infrastructure.folder.dto.FolderResult;
import baguni.infra.infrastructure.sharedFolder.dto.SharedFolderResult;
import baguni.infra.model.folder.FolderType;

@DisplayName("공유 폴더 컨트롤러 - 단위 테스트")
@ExtendWith(MockitoExtension.class)
class SharedFolderApiControllerUnitTest {

	@Mock
	private SharedFolderService sharedFolderService;

	@Mock
	private SharedFolderApiMapper sharedFolderApiMapper;

	@InjectMocks
	private SharedFolderApiController sharedFolderApiController;

	Long userId, folderId, otherFolderId, rootId;
	UUID uuid;
	String folderAccessToken;

	@BeforeEach
	void setUp() {
		userId = 1L;
		rootId = 1L;
		folderId = 2L;
		otherFolderId = 3L;
		uuid = UUID.randomUUID();
		folderAccessToken = UUID.randomUUID().toString();
	}

	@Test
	@DisplayName("공유 폴더 등록")
	void create_shared_folder() {
		// given
		var result = new SharedFolderResult.Create(folderAccessToken);
		var response = new SharedFolderApiResponse.Create(folderAccessToken);

		given(sharedFolderService.createSharedFolder(userId, folderId)).willReturn(result);
		given(sharedFolderApiMapper.toCreateResponse(result)).willReturn(response);

		// when
		sharedFolderApiController.createSharedFolder(userId, folderId);

		// then
		then(sharedFolderService).should(times(1)).createSharedFolder(userId, folderId);
		then(sharedFolderApiMapper).should(times(1)).toCreateResponse(result);
	}

	@Test
	@DisplayName("공유 폴더 리스트 조회")
	void get_shared_folder_list() {
		// given
		String otherAccessToken = UUID.randomUUID().toString();
		var result = List.of(
			new SharedFolderResult.Read(new FolderResult(folderId, "폴더1", FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()), folderAccessToken),
			new SharedFolderResult.Read(new FolderResult(otherFolderId, "폴더2", FolderType.GENERAL, rootId, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now()), otherAccessToken)
		);
		var response = List.of(
			new SharedFolderApiResponse.ReadFolderPartial(folderId, "폴더1", LocalDateTime.now(), LocalDateTime.now(), folderAccessToken),
			new SharedFolderApiResponse.ReadFolderPartial(otherFolderId, "폴더2", LocalDateTime.now(), LocalDateTime.now(), otherAccessToken)
		);

		given(sharedFolderService.getSharedFolderListByUserId(userId)).willReturn(result);
		given(sharedFolderApiMapper.toReadResponseList(result)).willReturn(response);

		// when
		sharedFolderApiController.getUserSharedFolderList(userId);

		// then
		then(sharedFolderService).should(times(1)).getSharedFolderListByUserId(userId);
		then(sharedFolderApiMapper).should(times(1)).toReadResponseList(result);
	}

	@Test
	@DisplayName("공유 폴더 취소")
	void delete_shared_folder() {
		// given

		// when
		sharedFolderApiController.deleteSharedFolder(userId, folderId);

		// then
		then(sharedFolderService).should(times(1)).deleteSharedFolder(userId, folderId);
	}

	@Test
	@DisplayName("공유 폴더 조회")
	void get_shared_folder_by_id() {
		// given
		var result = new SharedFolderResult.SharedFolderInfo("폴더", LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>(), new ArrayList<>());
		var response = new SharedFolderApiResponse.ReadFolderFull(result.folderName(), result.createdAt(), result.updatedAt(), result.pickList(), result.tagList());

		given(sharedFolderService.getSharedFolderInfo(uuid)).willReturn(result);
		given(sharedFolderApiMapper.toReadFolderFullResponse(result)).willReturn(response);

		// when
		sharedFolderApiController.getSharedFolderWithFullInfo(uuid);

		// then
		then(sharedFolderService).should(times(1)).getSharedFolderInfo(uuid);
		then(sharedFolderApiMapper).should(times(1)).toReadFolderFullResponse(result);
	}
}