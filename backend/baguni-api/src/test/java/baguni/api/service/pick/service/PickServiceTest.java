package baguni.api.service.pick.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import baguni.api.service.ranking.dto.RankingResult;
import baguni.api.service.ranking.service.RankingService;
import baguni.common.dto.UrlWithCount;
import baguni.infra.exception.folder.ApiFolderException;
import baguni.infra.exception.tag.ApiTagException;
import baguni.infra.model.user.SocialProvider;
import baguni.infra.model.util.IDToken;
import lombok.extern.slf4j.Slf4j;
import baguni.BaguniApiApplication;
import baguni.api.application.pick.dto.PickApiMapper;
import baguni.infra.infrastructure.link.dto.LinkInfo;
import baguni.infra.infrastructure.pick.dto.PickCommand;
import baguni.infra.infrastructure.pick.dto.PickResult;
import baguni.infra.exception.pick.ApiPickException;
import baguni.infra.infrastructure.tag.dto.TagCommand;
import baguni.api.service.tag.service.TagService;
import baguni.infra.infrastructure.pick.PickDataHandler;
import baguni.infra.model.folder.Folder;
import baguni.infra.infrastructure.folder.FolderRepository;
import baguni.infra.infrastructure.link.LinkRepository;
import baguni.infra.infrastructure.pick.PickRepository;
import baguni.infra.model.pick.PickTag;
import baguni.infra.infrastructure.pick.PickTagRepository;
import baguni.infra.model.tag.Tag;
import baguni.infra.infrastructure.tag.TagRepository;
import baguni.infra.model.user.Role;
import baguni.infra.model.user.User;
import baguni.infra.infrastructure.user.UserRepository;

@Slf4j
@SpringBootTest(classes = BaguniApiApplication.class)
@ActiveProfiles("local")
@DisplayName("픽 서비스 - 통합 테스트")
class PickServiceTest {

	@Autowired
	PickService pickService;
	@Autowired
	PickDataHandler pickDataHandler;
	@Autowired
	TagService tagService;
	@Autowired
	PickApiMapper pickApiMapper;
	@Autowired UserRepository userRepository;
	@Autowired FolderRepository folderRepository;
	@Autowired TagRepository tagRepository;

	@MockBean
	private RankingService rankingService;

	User user, user2;
	Folder root, recycleBin, unclassified, general;
	Tag tag1, tag2, tag3, tag4;

	@BeforeEach
		// TODO: change to Adaptor
	void setUp() {
		// save test user
		user = userRepository.save(
			User
				.builder()
				.email("test@test.com")
				.nickname("test")
				.password("test")
				.role(Role.ROLE_USER)
				.socialProvider(SocialProvider.KAKAO)
				.socialProviderId("1")
				.tagOrderList(new ArrayList<>())
				.idToken(IDToken.makeNew())
				.build()
		);

		// save test folder
		root = folderRepository.save(Folder.createEmptyRootFolder(user));
		recycleBin = folderRepository.save(Folder.createEmptyRecycleBinFolder(user));
		unclassified = folderRepository.save(Folder.createEmptyUnclassifiedFolder(user));
		general = folderRepository.save(Folder.createEmptyGeneralFolder(user, root, "React.js"));

		// save tag
		tag1 = tagRepository.save(Tag
			.builder().user(user).name("tag1").colorNumber(1).build());
		tag2 = tagRepository.save(Tag
			.builder().user(user).name("tag2").colorNumber(1).build());
		tag3 = tagRepository.save(Tag
			.builder().user(user).name("tag3").colorNumber(1).build());
	}

	@AfterEach
		// TODO: change to Adaptor (repository 말고!)
	void cleanUp(
		@Autowired FolderRepository folderRepository,
		@Autowired TagRepository tagRepository,
		@Autowired PickRepository pickRepository,
		@Autowired PickTagRepository pickTagRepository,
		@Autowired LinkRepository linkRepository
	) {
		// NOTE: 제거 순서 역시 FK 제약 조건을 신경써야 한다.
		pickTagRepository.deleteAll();
		pickRepository.deleteAll();
		folderRepository.deleteAll();
		tagRepository.deleteAll();
		linkRepository.deleteAll();
	}

	@Nested
	@DisplayName("픽 조회")
	class getPick {
		@Test
		@DisplayName("""
			    저장한 픽이 정상적으로 조회되어야 한다.
			""")
		void pick_save_and_read_test() {
			// given
			LinkInfo linkInfo = new LinkInfo("linkUrl", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command = new PickCommand.Create(
				user.getId(), "PICK", tagOrder, unclassified.getId(), linkInfo
			);

			// when
			PickResult.Pick saveResult = pickService.saveNewPick(command);
			PickResult.Pick readResult = pickService.getPick(new PickCommand.Read(user.getId(), saveResult.id()));

			// then
			assertThat(readResult).isNotNull();
			assertThat(readResult).isEqualTo(saveResult);
		}

		@Test
		@DisplayName("폴더 리스트 id가 넘어오면, 각 폴더 내부에 있는 픽 리스트들을 조회한다.")
		void folder_list_in_pick_list_test() {
			// given
			LinkInfo linkInfo1 = new LinkInfo("linkUrl1", "linkTitle", "linkDescription", "imageUrl");
			LinkInfo linkInfo2 = new LinkInfo("linkUrl2", "linkTitle", "linkDescription", "imageUrl");
			LinkInfo linkInfo3 = new LinkInfo("linkUrl3", "linkTitle", "linkDescription", "imageUrl");
			LinkInfo linkInfo4 = new LinkInfo("linkUrl4", "linkTitle", "linkDescription", "imageUrl");
			LinkInfo linkInfo5 = new LinkInfo("linkUrl5", "linkTitle", "linkDescription", "imageUrl");

			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command1 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				recycleBin.getId(), linkInfo1);
			PickCommand.Create command2 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo2);
			PickCommand.Create command3 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo3);
			PickCommand.Create command4 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				general.getId(), linkInfo4);
			PickCommand.Create command5 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo5);

			PickResult.Pick pick1 = pickService.saveNewPick(command1);
			PickResult.Pick pick2 = pickService.saveNewPick(command2);
			PickResult.Pick pick3 = pickService.saveNewPick(command3);
			PickResult.Pick pick4 = pickService.saveNewPick(command4);
			PickResult.Pick pick5 = pickService.saveNewPick(command5);

			List<Long> folderIdList = List.of(unclassified.getId(), general.getId(), recycleBin.getId());
			PickCommand.ReadList readListCommand = pickApiMapper.toReadListCommand(user.getId(), folderIdList);

			// when
			List<PickResult.FolderPickWithViewCountList> folderPickList = pickService.getFolderListChildPickList(
				readListCommand);

			for (PickResult.FolderPickWithViewCountList list : folderPickList) {
				log.info("list: {} ", list.toString());
			}

			// then
			assertThat(folderPickList.get(0).pickList().size()).isEqualTo(3); // unclassified
			assertThat(folderPickList.get(1).pickList().size()).isEqualTo(1); // general
			assertThat(folderPickList.get(2).pickList().size()).isEqualTo(1); // recycleBin
		}

		@Test
		@DisplayName("픽 리스트에 조회수가 포함되어 반환된다.")
		void folder_list_in_folder_list_test() {
			// given
			LinkInfo linkInfo1 = new LinkInfo("linkUrl1", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command1 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				recycleBin.getId(), linkInfo1);
			pickService.saveNewPick(command1);
			List<Long> folderIdList = List.of(unclassified.getId(), general.getId(), recycleBin.getId());
			PickCommand.ReadList readListCommand = pickApiMapper.toReadListCommand(user.getId(), folderIdList);

			// when
			when(rankingService.getUrlRanking(10)).thenThrow(new RuntimeException("Ranking server is down"));
			pickService.getFolderListChildPickList(readListCommand);
		}

		@Test
		@DisplayName("조회수 null이 아닌 경우 테스트")
		void url_count_test() {
			// given
			LinkInfo linkInfo1 = new LinkInfo("http://example.com", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command1 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				recycleBin.getId(), linkInfo1);
			pickService.saveNewPick(command1);
			List<Long> folderIdList = List.of(unclassified.getId(), general.getId(), recycleBin.getId());
			PickCommand.ReadList readListCommand = pickApiMapper.toReadListCommand(user.getId(), folderIdList);

			UrlWithCount urlWithCount = new UrlWithCount("http://example.com", 100L);
			UrlWithCount pickCount = new UrlWithCount("http://example.com", 10L);

			// when
			when(rankingService.getUrlRanking(anyInt()))
				.thenReturn(new RankingResult(List.of(urlWithCount), List.of(urlWithCount), List.of(pickCount)));
			pickService.getFolderListChildPickList(readListCommand);
		}

		@Test
		@DisplayName("픽이 존재하는지 확인한다.")
		void pick_exist_test() {
			// given
			LinkInfo linkInfo = new LinkInfo("linkUrl", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command = new PickCommand.Create(
				user.getId(), "PICK", tagOrder, unclassified.getId(), linkInfo
			);

			// when
			pickService.saveNewPick(command);
			boolean existPickTrue = pickService.existPickByUrl(user.getId(), linkInfo.url());
			boolean existPickFalse = pickService.existPickByUrl(user.getId(), "");

			// then
			assertThat(existPickTrue).isTrue();
			assertThat(existPickFalse).isFalse();
		}

		@Test
		@DisplayName("조회 시, 본인 픽이 아닌 경우 예외 발생")
		void pick_owner_test() {
			// given
			LinkInfo linkInfo = new LinkInfo("linkUrl", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			createUser2AndTag4();
			PickCommand.Create command = new PickCommand.Create(
				user.getId(), "PICK", tagOrder, unclassified.getId(), linkInfo
			);
			PickResult.Pick pick = pickService.saveNewPick(command);

			PickCommand.Read read = new PickCommand.Read(user2.getId(), pick.id());

			// when, then
			assertThatThrownBy(() -> pickService.getPick(read))
				.isInstanceOf(ApiPickException.class)
				.hasMessageStartingWith(ApiPickException.PICK_UNAUTHORIZED_USER_ACCESS().getMessage());
		}
	}

	@Nested
	@DisplayName("픽 생성")
	class savePick {

		@Test
		@DisplayName("부모 폴더 아이디가 null인 경우 픽을 저장하는 경우, 실패해야 한다. - 루트는 폴더만 위치할 수 있다.")
		void create_root_pick_exception_test() {
			// given
			LinkInfo linkInfo = new LinkInfo("linkUrl", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command = new PickCommand.Create(
				user.getId(), "PICK", tagOrder, null, linkInfo
			);

			// when, then
			assertThatThrownBy(() -> pickService.saveNewPick(command))
				.isInstanceOf(ApiFolderException.class)
				.hasMessageStartingWith(ApiFolderException.INVALID_PARENT_FOLDER().getMessage());
		}

		@Test
		@DisplayName("루트에 픽을 저장하는 경우, 실패해야 한다. - 루트는 폴더만 위치할 수 있다.")
		void create_root_pick_test() {
			// given
			LinkInfo linkInfo = new LinkInfo("linkUrl", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command = new PickCommand.Create(
				user.getId(), "PICK", tagOrder, root.getId(), linkInfo
			);

			// when, then
			assertThatThrownBy(() -> pickService.saveNewPick(command))
				.isInstanceOf(ApiPickException.class)
				.hasMessageStartingWith(ApiPickException.PICK_UNAUTHORIZED_ROOT_ACCESS().getMessage());
		}

		@Test
		@DisplayName("픽 생성 시, 본인의 태그만 넣을 수 있어야 한다. 아니면 예외 발생")
		void create_pick_owner_tag_test() {
			// given
			LinkInfo linkInfo = new LinkInfo("linkUrl", "linkTitle", "linkDescription", "imageUrl");
			createUser2AndTag4();
			List<Long> tagOrder = List.of(tag4.getId());
			PickCommand.Create command = new PickCommand.Create(
				user.getId(), "PICK", tagOrder, unclassified.getId(), linkInfo
			);

			// when, then
			assertThatThrownBy(() -> pickService.saveNewPick(command))
				.isInstanceOf(ApiTagException.class)
				.hasMessageStartingWith(ApiTagException.UNAUTHORIZED_TAG_ACCESS().getMessage());
		}

		@Test
		@DisplayName("미분류 폴더에 픽이 생성 후 존재하는지 확인")
		void findPickUrl_test() {
			// given
			LinkInfo linkInfo = new LinkInfo("findPickUrl", "linkTitle", "linkDescription", "imageUrl");
			PickCommand.Extension command = new PickCommand.Extension(user.getId(), linkInfo.title(),
				linkInfo.url());

			// when
			pickService.savePickToUnclassified(command);

			// then
			assertThat(pickService.findPickUrl(user.getId(), linkInfo.url())).isNotEqualTo(Optional.empty());
			assertThat(pickService.findPickUrl(user.getId(), "")).isEqualTo(Optional.empty());
		}
	}

	@Nested
	@DisplayName("픽 수정")
	class updatePick {
		@Test
		@DisplayName("""
			   픽의 제목, 메모는 null 값이 들어오면 수정을 하지 않는다.
			   모두 null 값이 들어올 경우 아무 일도 발생하지 않는다.
			""")
		void update_data_with_null_test() {
			// given
			LinkInfo linkInfo = new LinkInfo("linkUrl", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command = new PickCommand.Create(
				user.getId(), "PICK", tagOrder, unclassified.getId(), linkInfo
			);
			PickResult.Pick savePick = pickService.saveNewPick(command);

			// when
			String newTitle = "NEW_PICK";
			List<Long> newTagOrder = List.of(tag3.getId(), tag2.getId(), tag1.getId());
			PickCommand.Update updateCommand = new PickCommand.Update(
				user.getId(), savePick.id(), newTitle, null, newTagOrder
			);
			PickResult.Pick updatePick = pickService.updatePick(updateCommand);

			// then
			assertThat(updatePick.title()).isNotEqualTo(savePick.title()).isEqualTo(newTitle); // changed
			assertThat(updatePick.tagIdOrderedList()).isNotEqualTo(savePick.tagIdOrderedList())
													 .isEqualTo(newTagOrder); // changed
		}

		@Test
		@DisplayName("본인 폴더에 있지 않은 픽 수정")
		void not_user_folder_pick_test() {
			// given
			LinkInfo linkInfo = new LinkInfo("linkUrl", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command = new PickCommand.Create(
				user.getId(), "PICK", tagOrder, unclassified.getId(), linkInfo
			);
			PickResult.Pick savePick = pickService.saveNewPick(command);

			String newTitle = "NEW_PICK";
			List<Long> newTagOrder = List.of(tag3.getId(), tag2.getId(), tag1.getId());
			PickCommand.Update updateCommand = new PickCommand.Update(
				100L, savePick.id(), newTitle, unclassified.getId(), newTagOrder
			);

			// when, then
			assertThatThrownBy(() -> pickService.updatePick(updateCommand))
				.isInstanceOf(ApiFolderException.class)
				.hasMessageStartingWith(ApiFolderException.FOLDER_ACCESS_DENIED().getMessage());
		}

		@Test
		@DisplayName("부모 폴더가 루트 폴더가 아닌 곳으로 수정")
		void parent_folder_is_not_root_pick_test() {
			// given
			LinkInfo linkInfo = new LinkInfo("linkUrl", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command = new PickCommand.Create(
				user.getId(), "PICK", tagOrder, unclassified.getId(), linkInfo
			);
			PickResult.Pick savePick = pickService.saveNewPick(command);

			String newTitle = "NEW_PICK";
			List<Long> newTagOrder = List.of(tag3.getId(), tag2.getId(), tag1.getId());
			PickCommand.Update updateCommand = new PickCommand.Update(
				user.getId(), savePick.id(), newTitle, root.getId(), newTagOrder
			);

			// when, then
			assertThatThrownBy(() -> pickService.updatePick(updateCommand))
				.isInstanceOf(ApiPickException.class)
				.hasMessageStartingWith(ApiPickException.PICK_UNAUTHORIZED_ROOT_ACCESS().getMessage());
		}
	}

	@Nested
	@DisplayName("픽 이동")
	class movePick {
		@Test
		@DisplayName("""
			    같은 폴더 내에서 픽의 순서를 이동한 후
			    그 이동된 부모 폴더의 자식 리스트 획득을 할 수 있어야 한다.
			    그리고 그 자식 리스트는 순서 정보가 올바르게 설정되어야 한다.
			""")
		void move_pick_to_same_folder_test() {
			// given
			LinkInfo linkInfo1 = new LinkInfo("linkUrl1", "linkTitle", "linkDescription", "imageUrl");
			LinkInfo linkInfo2 = new LinkInfo("linkUrl2", "linkTitle", "linkDescription", "imageUrl");
			LinkInfo linkInfo3 = new LinkInfo("linkUrl3", "linkTitle", "linkDescription", "imageUrl");
			LinkInfo linkInfo4 = new LinkInfo("linkUrl4", "linkTitle", "linkDescription", "imageUrl");
			LinkInfo linkInfo5 = new LinkInfo("linkUrl5", "linkTitle", "linkDescription", "imageUrl");

			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command1 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo1);
			PickCommand.Create command2 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo2);
			PickCommand.Create command3 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo3);
			PickCommand.Create command4 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo4);
			PickCommand.Create command5 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo5);

			PickResult.Pick pick1 = pickService.saveNewPick(command1);
			PickResult.Pick pick2 = pickService.saveNewPick(command2);
			PickResult.Pick pick3 = pickService.saveNewPick(command3);
			PickResult.Pick pick4 = pickService.saveNewPick(command4);
			PickResult.Pick pick5 = pickService.saveNewPick(command5);

			List<Long> originalPickIdList = List.of(pick1.id(), pick2.id(), pick3.id(),
				pick4.id(), pick5.id());
			List<Long> movePickIdList = List.of(pick2.id(), pick3.id(), pick1.id());

			PickCommand.Move command = new PickCommand.Move(user.getId(), movePickIdList, unclassified.getId(), 0);

			// when
			pickService.movePick(command);

			List<PickResult.Pick> movedPickList = pickService.getFolderChildPickList(user.getId(),
				unclassified.getId());

			assertThatThrownBy(() -> pickService.getFolderChildPickList(user.getId(), null))
				.isInstanceOf(ApiFolderException.class)
				.hasMessageStartingWith(ApiFolderException.INVALID_PARENT_FOLDER().getMessage());;

			// then
			// 결과값 : [1, 2, 3, 4, 5] -> [2, 3, 1, 4, 5]
			assertThat(originalPickIdList).isNotEqualTo(movedPickList);
			assertThat(originalPickIdList.get(0)).isEqualTo(movedPickList.get(2).id());
			assertThat(originalPickIdList.get(1)).isEqualTo(movedPickList.get(0).id());
			assertThat(originalPickIdList.get(2)).isEqualTo(movedPickList.get(1).id());
		}

		@Test
		@DisplayName("""
			    다른 폴더 내에서 픽의 순서를 이동한 후
			    그 이동된 부모 폴더의 자식 리스트 획득을 할 수 있어야 한다.
			    그리고 그 자식 리스트는 순서 정보가 올바르게 설정되어야 한다.
			""")
		void move_pick_to_other_folder_test() {
			// given
			LinkInfo linkInfo1 = new LinkInfo("linkUrl1", "linkTitle", "linkDescription", "imageUrl");
			LinkInfo linkInfo2 = new LinkInfo("linkUrl2", "linkTitle", "linkDescription", "imageUrl");
			LinkInfo linkInfo3 = new LinkInfo("linkUrl3", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command1 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo1);
			PickCommand.Create command2 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo2);
			PickCommand.Create command3 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo3);

			PickResult.Pick pick1 = pickService.saveNewPick(command1);
			PickResult.Pick pick2 = pickService.saveNewPick(command2);
			PickResult.Pick pick3 = pickService.saveNewPick(command3);

			List<Long> movePickIdList = List.of(pick3.id(), pick2.id());
			PickCommand.Move command = new PickCommand.Move(user.getId(), movePickIdList, general.getId(), 0);

			PickCommand.Read readCommand1 = new PickCommand.Read(user.getId(), pick1.id());
			PickCommand.Read readCommand2 = new PickCommand.Read(user.getId(), pick2.id());
			PickCommand.Read readCommand3 = new PickCommand.Read(user.getId(), pick3.id());

			// when
			pickService.movePick(command);

			PickResult.Pick readPick1 = pickService.getPick(readCommand1);
			PickResult.Pick readPick2 = pickService.getPick(readCommand2);
			PickResult.Pick readPick3 = pickService.getPick(readCommand3);

			List<PickResult.Pick> unclassifiedPickList = pickService.getFolderChildPickList(user.getId(),
				unclassified.getId());
			List<PickResult.Pick> generalPickList = pickService.getFolderChildPickList(user.getId(), general.getId());

			// then
			assertThat(readPick1.parentFolderId()).isNotEqualTo(command.destinationFolderId());
			assertThat(readPick2.parentFolderId()).isEqualTo(command.destinationFolderId());
			assertThat(readPick3.parentFolderId()).isEqualTo(command.destinationFolderId());
			assertThat(unclassifiedPickList).contains(readPick1);
			assertThat(generalPickList).contains(readPick2, readPick3);
		}

		@Test
		@DisplayName("루트로 픽을 이동하는 경우, 실패해야 한다. - 루트는 폴더만 위치할 수 있다.")
		void move_root_pick_test() {
			// given
			LinkInfo linkInfo1 = new LinkInfo("linkUrl1", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command1 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo1);
			pickService.saveNewPick(command1);

			List<Long> movePickIdList = List.of(1L, 2L);

			PickCommand.Move command = new PickCommand.Move(user.getId(), movePickIdList, root.getId(), 0);

			// when, then
			assertThatThrownBy(() -> pickService.movePick(command))
				.isInstanceOf(ApiPickException.class)
				.hasMessageStartingWith(ApiPickException.PICK_UNAUTHORIZED_ROOT_ACCESS().getMessage());
		}

		@Test
		@DisplayName("부모 폴더 id가 null인 폴더로 픽을 이동하는 경우, 실패해야 한다.")
		void move_null_parentId_pick_test() {
			// given
			LinkInfo linkInfo1 = new LinkInfo("linkUrl1", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command1 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo1);
			pickService.saveNewPick(command1);

			List<Long> movePickIdList = List.of(1L, 2L);

			PickCommand.Move command = new PickCommand.Move(user.getId(), movePickIdList, null, 0);

			// when, then
			assertThatThrownBy(() -> pickService.movePick(command))
				.isInstanceOf(ApiFolderException.class)
				.hasMessageStartingWith(ApiFolderException.INVALID_PARENT_FOLDER().getMessage());
		}

		@Test
		@DisplayName("""
			    순서 id 리스트가 존재하지 않는 픽 Id면 ApiPickException.PICK_NOT_FOUND() 예외를 발생시킨다.
			""")
		void move_pick_invalid_order_value_test() {
			// given
			LinkInfo linkInfo1 = new LinkInfo("linkUrl1", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command1 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo1);
			pickService.saveNewPick(command1);

			List<Long> movePickIdList = List.of(-10L, -20000L);

			PickCommand.Move command = new PickCommand.Move(user.getId(), movePickIdList, general.getId(), 0);

			// when, then
			assertThatThrownBy(() -> pickService.movePick(command))
				.isInstanceOf(ApiPickException.class);
		}
	}

	@Nested
	@DisplayName("픽 삭제")
	class deletePick {
		@Test
		@DisplayName("""
			    픽 삭제한 후 조회하여 삭제가 되었음을 확인한다.
			""")
		void remove_and_read_pick_test() {
			// given
			LinkInfo linkInfo1 = new LinkInfo("linkUrl1", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command1 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				recycleBin.getId(), linkInfo1);
			PickResult.Pick pickResult = pickService.saveNewPick(command1);

			List<Long> deletePickIdList = List.of(pickResult.id());

			// when
			pickService.deletePick(new PickCommand.Delete(user.getId(), deletePickIdList));

			// then
			assertThatThrownBy(() -> pickService.getPick(new PickCommand.Read(user.getId(), pickResult.id())))
				.isInstanceOf(ApiPickException.class)
				.hasMessageStartingWith(ApiPickException.PICK_NOT_FOUND().getMessage());
		}

		@Test
		@DisplayName("""
			    존재하지 않은 픽을 삭제할 순 없으며, 시도시 예외가 발생한다.
			""")
		void remove_not_existing_pick_exception_test() {
			// given
			LinkInfo linkInfo1 = new LinkInfo("linkUrl1", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command1 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				recycleBin.getId(), linkInfo1);
			PickResult.Pick pickResult = pickService.saveNewPick(command1);

			List<Long> deletePickIdList = List.of(pickResult.id());

			// when
			pickService.deletePick(new PickCommand.Delete(user.getId(), deletePickIdList));

			// then
			assertThatThrownBy(() -> pickService.deletePick(new PickCommand.Delete(user.getId(), deletePickIdList)))
				.isInstanceOf(ApiPickException.class)
				.hasMessageStartingWith(ApiPickException.PICK_NOT_FOUND().getMessage());
		}

		@Test
		@DisplayName("""
			    휴지통에 있지 않은 픽은 삭제할 수 없으며, 시도시 예외가 발생한다.
			""")
		void remove_not_in_recycle_bin_folder_exception_test() {
			// given
			LinkInfo linkInfo1 = new LinkInfo("linkUrl1", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command1 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo1);
			PickResult.Pick pickResult = pickService.saveNewPick(command1);

			List<Long> deletePickIdList = List.of(pickResult.id());

			// when, then
			assertThatThrownBy(() -> pickService.deletePick(new PickCommand.Delete(user.getId(), deletePickIdList)))
				.isInstanceOf(ApiPickException.class)
				.hasMessageStartingWith(ApiPickException.PICK_DELETE_NOT_ALLOWED().getMessage());
		}

		@Test
		@DisplayName("""
			    태그 삭제시, 픽에 설정된 태그 정보와 tagList도 변경되어야 한다.
			""")
		void update_tag_list_in_pick_when_tag_is_removed_test() {
			// given
			TagCommand.Delete delete = new TagCommand.Delete(user.getId(), tag1.getId());

			LinkInfo linkInfo1 = new LinkInfo("linkUrl1", "linkTitle", "linkDescription", "imageUrl");
			List<Long> tagOrder = List.of(tag1.getId(), tag2.getId(), tag3.getId());
			PickCommand.Create command1 = new PickCommand.Create(user.getId(), "PICK", tagOrder,
				unclassified.getId(), linkInfo1);
			PickResult.Pick savedPickResult = pickService.saveNewPick(command1);

			// when
			tagService.deleteTag(delete);
			PickResult.Pick pickResult = pickService.getPick(new PickCommand.Read(user.getId(), savedPickResult.id()));
			List<PickTag> pickTagList = pickDataHandler.getPickTagList(pickResult.id());

			// then
			assertThat(pickResult.tagIdOrderedList().size()).isEqualTo(tagOrder.size() - 1);
			assertThat(pickResult.tagIdOrderedList().size()).isEqualTo(pickTagList.size());
			assertThat(pickResult.tagIdOrderedList()).isEqualTo(List.of(tag2.getId(), tag3.getId()));
		}
	}

	public void createUser2AndTag4() {
		user2 = userRepository.save(
			User
				.builder()
				.email("test@test.com")
				.nickname("test")
				.password("test")
				.role(Role.ROLE_USER)
				.socialProvider(SocialProvider.KAKAO)
				.socialProviderId("1")
				.tagOrderList(new ArrayList<>())
				.idToken(IDToken.makeNew())
				.build()
		);

		tag4 = tagRepository.save(Tag
			.builder().user(user2).name("tag4").colorNumber(1).build());
	}
}