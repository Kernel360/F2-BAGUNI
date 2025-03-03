package baguni.api.service.sharedFolder.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import baguni.api.fixture.FolderFixture;
import baguni.api.fixture.PickFixture;
import baguni.api.fixture.SharedFolderFixture;
import baguni.api.fixture.TagFixture;
import baguni.api.fixture.UserFixture;
import baguni.infra.exception.folder.ApiFolderException;
import baguni.infra.exception.sharedFolder.ApiSharedFolderException;
import baguni.infra.infrastructure.folder.FolderDataHandler;
import baguni.infra.infrastructure.link.dto.LinkMapper;
import baguni.infra.infrastructure.pick.PickDataHandler;
import baguni.infra.infrastructure.sharedFolder.SharedFolderDataHandler;
import baguni.infra.infrastructure.sharedFolder.dto.SharedFolderMapper;
import baguni.infra.infrastructure.sharedFolder.dto.SharedFolderResult;
import baguni.infra.infrastructure.tag.TagDataHandler;
import baguni.infra.model.folder.Folder;
import baguni.infra.model.folder.FolderType;
import baguni.infra.model.pick.Pick;
import baguni.infra.model.sharedFolder.SharedFolder;
import baguni.infra.model.tag.Tag;
import baguni.infra.model.user.User;

@DisplayName("공유 폴더 서비스 - 단위 테스트")
@ExtendWith(MockitoExtension.class)
class SharedFolderServiceUnitTest {

	@Mock
	private SharedFolderDataHandler sharedFolderDataHandler;

	@Mock
	private SharedFolderMapper sharedFolderMapper;

	@Mock
	private FolderDataHandler folderDataHandler;

	@Mock
	private TagDataHandler tagDataHandler;

	@Mock
	private PickDataHandler pickDataHandler;

	@Mock
	private LinkMapper linkMapper;

	@InjectMocks
	private SharedFolderService sharedFolderService;

	Long userId, folderId;
	UUID uuid;
	User user;
	Folder folder;
	SharedFolder sharedFolder;
	@BeforeEach
	void setUp() {
		userId = 1L;
		folderId = 1L;
		uuid = UUID.randomUUID();
		user = UserFixture
			.builder().id(userId).build().get();
		folder = FolderFixture
			.builder().id(folderId).user(user).name("폴더").folderType(FolderType.GENERAL).build().get();
		sharedFolder = SharedFolderFixture
			.builder().id(uuid).user(user).folder(folder).build().get();
	}

	@Nested
	@DisplayName("공유 폴더 생성 관련")
	class CreateSharedFolder {

		@Test
		@DisplayName("공유 폴더 생성")
		void create_shared_folder() {
			// given
			given(folderDataHandler.getFolder(any())).willReturn(folder);
			given(sharedFolderDataHandler.save(userId, folderId)).willReturn(sharedFolder);

			// when
			sharedFolderService.createSharedFolder(userId, folderId);

			// then
			then(folderDataHandler).should(times(1)).getFolder(folderId);
			then(sharedFolderDataHandler).should(times(1)).save(userId, folderId);
			then(sharedFolderMapper).should(times(1)).toCreateResult(sharedFolder);
		}

		@Test
		@DisplayName("본인 폴더가 아닌 폴더 공유")
		void owner_check_shared_folder() {
			// given
			Long otherUserId = 2L;
			given(folderDataHandler.getFolder(any())).willReturn(folder);

			// when
			assertThatThrownBy(() -> sharedFolderService.createSharedFolder(otherUserId, folderId))
				.isInstanceOf(ApiFolderException.class)
				.hasMessageStartingWith(ApiFolderException.FOLDER_ACCESS_DENIED().getMessage());

			// then
			then(folderDataHandler).should(times(1)).getFolder(folderId);
			then(sharedFolderDataHandler).should(times(0)).save(userId, folderId);
			then(sharedFolderMapper).should(times(0)).toCreateResult(sharedFolder);
		}

		@Test
		@DisplayName("생성 시, 공유할 수 없는 폴더")
		void not_create_shared_folder() {
			// given
			given(folderDataHandler.getFolder(any())).willReturn(folder);
			given(sharedFolderDataHandler.isSharedFolder(folderId)).willReturn(true);

			// when
			assertThatThrownBy(() -> sharedFolderService.createSharedFolder(userId, folderId))
				.isInstanceOf(ApiSharedFolderException.class)
				.hasMessageStartingWith(ApiSharedFolderException.FOLDER_ALREADY_SHARED().getMessage());

			// then
			then(folderDataHandler).should(times(1)).getFolder(folderId);
			then(sharedFolderDataHandler).should(times(0)).save(userId, folderId);
			then(sharedFolderMapper).should(times(0)).toCreateResult(sharedFolder);
		}

		@Test
		@DisplayName("일반 폴더 외에 공유 금지")
		void not_general_shared_folder() {
			// given
			folder = FolderFixture
				.builder().id(folderId).user(user).folderType(FolderType.ROOT).build().get();
			given(folderDataHandler.getFolder(any())).willReturn(folder);

			// when
			assertThatThrownBy(() -> sharedFolderService.createSharedFolder(userId, folderId))
				.isInstanceOf(ApiSharedFolderException.class)
				.hasMessageStartingWith(ApiSharedFolderException.FOLDER_CANNOT_BE_SHARED().getMessage());

			// then
			then(folderDataHandler).should(times(1)).getFolder(folderId);
			then(sharedFolderDataHandler).should(times(0)).save(userId, folderId);
			then(sharedFolderMapper).should(times(0)).toCreateResult(sharedFolder);
		}
	}

	@Nested
	@DisplayName("공유 폴더 조회 관련")
	class GetSharedFolder {

		@Test
		@DisplayName("공유 폴더 조회")
		void get_shared_folder() {
			// given
			Pick pick1 = PickFixture
				.builder().id(1L).parentFolder(folder).tagIdOrderedList(new ArrayList<>(List.of(1L, 2L))).build().get();
			Pick pick2 = PickFixture
				.builder().id(2L).parentFolder(folder).tagIdOrderedList(new ArrayList<>(List.of(2L))).build().get();
			Tag tag1 = TagFixture
				.builder().id(1L).user(user).name("tag1").build().get();
			Tag tag2 = TagFixture
				.builder().id(2L).user(user).name("tag1").build().get();
			var tagInfo = SharedFolderResult.SharedTagInfo
				.builder()
				.name("tag1")
				.build();

			List<Pick> pickList = List.of(pick1, pick2);
			List<Tag> tagList = List.of(tag1, tag2);

			given(sharedFolderDataHandler.getByUUID(any())).willReturn(sharedFolder);
			given(pickDataHandler.getPickListPreservingOrder(any())).willReturn(pickList);
			given(tagDataHandler.getTagList(anyList())).willReturn(tagList);
			given(sharedFolderMapper.toSharedTagInfo(any())).willReturn(tagInfo);

			// when
			sharedFolderService.getSharedFolderInfo(uuid);

			// then
			then(sharedFolderDataHandler).should(times(1)).getByUUID(uuid);
			then(pickDataHandler).should(times(1)).getPickListPreservingOrder(folder.getChildPickIdOrderedList());
			then(tagDataHandler).should(times(1)).getTagList(anyList());
			then(sharedFolderMapper).should(times(2)).toSharedTagInfo(any());
		}

		@Test
		@DisplayName("폴더 ID에 해당하는 접근 토큰이 존재하는 경우")
		void find_access_token_exists() {
			// given
			given(sharedFolderDataHandler.findUUIDBySourceFolderId(any())).willReturn(Optional.of(uuid));

			// when
			Optional<String> accessToken = sharedFolderService.findFolderAccessTokenByFolderId(
				folderId);

			// then
			then(sharedFolderDataHandler).should(times(1)).findUUIDBySourceFolderId(folderId);
			assertThat(accessToken.isPresent()).isTrue();
			assertThat(accessToken.get()).isEqualTo(uuid.toString());
		}

		@Test
		@DisplayName("폴더 ID에 해당하는 접근 토큰이 존재하지 않는 경우")
		void find_access_token_not_exists() {
			// given
			given(sharedFolderDataHandler.findUUIDBySourceFolderId(any())).willReturn(Optional.empty());

			// when
			Optional<String> accessToken = sharedFolderService.findFolderAccessTokenByFolderId(
				folderId);

			// then
			then(sharedFolderDataHandler).should(times(1)).findUUIDBySourceFolderId(folderId);
			assertThat(accessToken.isPresent()).isFalse();
		}

		@Test
		@DisplayName("유저가 가진 폴더 공유 폴더 리스트 조회")
		void get_user_shared_folder_list() {
			// given
			Long otherFolderId = 2L;
			UUID otherUuid = UUID.randomUUID();
			Folder otherFolder = FolderFixture
				.builder().id(otherFolderId).user(user).folderType(FolderType.GENERAL).build().get();
			SharedFolder otherSharedFolder = SharedFolderFixture
				.builder().id(otherUuid).user(user).folder(otherFolder).build().get();
			List<SharedFolder> list = new ArrayList<>(List.of(sharedFolder, otherSharedFolder));
			given(sharedFolderDataHandler.getByUserId(userId)).willReturn(list);

			// when
			sharedFolderService.getSharedFolderListByUserId(userId);

			// then
			then(sharedFolderDataHandler).should(times(1)).getByUserId(userId);
			then(sharedFolderMapper).should(times(1)).toReadResult(sharedFolder);
			assertThat(list).hasSize(2);
		}
	}

	@Nested
	@DisplayName("공유 폴더 삭제 관련")
	class DeleteSharedFolder {

		@Test
		@DisplayName("공유 폴더 삭제")
		void delete_shared_folder() {
			// given
			given(sharedFolderDataHandler.getByFolderId(folderId)).willReturn(sharedFolder);

			// when
			sharedFolderService.deleteSharedFolder(userId, folderId);

			// then
			then(sharedFolderDataHandler).should(times(1)).getByFolderId(folderId);
			then(sharedFolderDataHandler).should(times(1)).deleteBySourceFolderId(folderId);
		}

		@Test
		@DisplayName("다른 사람의 공유 폴더 삭제 시 예외 발생")
		void delete_owner_check_shared_folder() {
			// given
			Long otherUserId = 2L;
			given(sharedFolderDataHandler.getByFolderId(folderId)).willReturn(sharedFolder);

			// when
			assertThatThrownBy(() -> sharedFolderService.deleteSharedFolder(otherUserId, folderId))
				.isInstanceOf(ApiSharedFolderException.class)
				.hasMessageStartingWith(ApiSharedFolderException.SHARED_FOLDER_UNAUTHORIZED().getMessage());

			// then
			then(sharedFolderDataHandler).should(times(1)).getByFolderId(folderId);
		}
	}
}