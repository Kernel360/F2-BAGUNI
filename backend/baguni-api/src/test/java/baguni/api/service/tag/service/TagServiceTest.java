package baguni.api.service.tag.service;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import baguni.domain.infrastructure.folder.FolderRepository;
import baguni.domain.infrastructure.link.LinkRepository;
import baguni.domain.infrastructure.pick.PickRepository;
import baguni.domain.infrastructure.pick.PickTagRepository;
import baguni.domain.infrastructure.tag.TagRepository;
import baguni.domain.model.util.IDToken;
import lombok.extern.slf4j.Slf4j;
import baguni.BaguniApiApplication;
import baguni.domain.infrastructure.tag.dto.TagCommand;
import baguni.domain.infrastructure.tag.dto.TagResult;
import baguni.domain.exception.tag.ApiTagException;
import baguni.domain.infrastructure.user.UserDataHandler;
import baguni.domain.model.user.Role;
import baguni.domain.model.user.SocialProvider;
import baguni.domain.model.user.User;
import baguni.domain.infrastructure.user.UserRepository;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = BaguniApiApplication.class)
@ActiveProfiles("local")
@Transactional
@DisplayName("태그 서비스 - 통합 테스트")
class TagServiceTest {

	@Autowired
	TagService tagService;

	@Autowired
	UserDataHandler userDataHandler;

	@Autowired
	UserRepository userRepository;

	User user;

	@BeforeAll
	void setUp() {
		user = User
			.builder()
			.email("test@test.com")
			.nickname("test")
			.password("test")
			.role(Role.ROLE_USER)
			.socialProvider(SocialProvider.KAKAO)
			.socialProviderId("1")
			.tagOrderList(new ArrayList<>())
			.idToken(IDToken.makeNew())
			.build();
		userRepository.save(user);
	}

	@AfterEach
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

	@Test
	@DisplayName("태그 저장 후 태그 상세 조회 테스트")
	void getTagTest() {
		// given
		TagResult tagCreateResult = getTagCreateResult(0);
		TagCommand.Read command = new TagCommand.Read(user.getId(), tagCreateResult.id());

		// when
		TagResult tagReadResult = tagService.getTag(command);

		// then
		assertThat(tagReadResult).isNotNull();
		assertThat(tagReadResult.name()).isEqualTo(tagCreateResult.name());
		assertThat(tagReadResult.colorNumber()).isEqualTo(tagCreateResult.colorNumber());
	}

	@Test
	@DisplayName("태그 저장 후 태그 리스트 조회 테스트")
	void getTagListTest() {
		// given
		for (int i = 0; i < 5; i++) {
			getTagCreateResult(i);
		}

		// when
		List<TagResult> tagList = tagService.getUserTagList(user.getId());

		// then
		assertThat(tagList).isNotNull();
		assertThat(tagList.size()).isEqualTo(5);
	}

	@Test
	@DisplayName("다른 사람 태그 조회")
	void other_user_tag_test() {
		// given
		getTagCreateResult(1);

		User user2 = User
			.builder()
			.email("test@test.com")
			.nickname("test")
			.password("test")
			.role(Role.ROLE_USER)
			.socialProvider(SocialProvider.KAKAO)
			.socialProviderId("1")
			.tagOrderList(new ArrayList<>())
			.idToken(IDToken.makeNew())
			.build();
		userRepository.save(user2);

		TagCommand.Create create = new TagCommand.Create(user2.getId(), "태그4", 1);
		TagResult tagResult = tagService.saveTag(create);

		TagCommand.Read command = new TagCommand.Read(user.getId(), tagResult.id());

		// when, then
		assertThatThrownBy(() -> tagService.getTag(command))
			.isInstanceOf(ApiTagException.class)
			.hasMessageStartingWith(ApiTagException.UNAUTHORIZED_TAG_ACCESS().getMessage());
	}

	@Test
	@DisplayName("태그 중복 저장 테스트")
	void duplicateTagTest() {
		// given
		getTagCreateResult(0);

		// when, then
		assertThatThrownBy(() -> getTagCreateResult(0))
			.isInstanceOf(ApiTagException.class)
			.hasMessageStartingWith(ApiTagException.TAG_ALREADY_EXIST().getMessage());
	}

	@Test
	@DisplayName("태그 수정 테스트")
	void updateTagTest() {
		// given
		TagResult tagCreateResult = getTagCreateResult(0);
		TagCommand.Update update = new TagCommand.Update(user.getId(), tagCreateResult.id(), "태그태그", 2);

		// when
		TagResult tagUpdateResult = tagService.updateTag(update);

		// then
		assertThat(tagUpdateResult).isNotNull();
		assertThat(tagUpdateResult.name()).isEqualTo("태그태그");
		assertThat(tagUpdateResult.colorNumber()).isEqualTo(2);
	}

	@Test
	@DisplayName("태그 이동 테스트")
	void moveTagTest() {
		// given
		List<Long> tagIdList = new ArrayList<>();
		List<Long> expectedOrderList = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			TagResult tagResult = getTagCreateResult(i);
			tagIdList.add(tagResult.id());
			expectedOrderList.add(tagResult.id());
		}

		User savedUser = userDataHandler.getUser(user.getId());
		for (Long tagId : tagIdList) {
			savedUser.updateTagOrderList(tagId, tagIdList.size());
		}

		Long targetId = expectedOrderList.get(0);
		int targetIdx = 3;
		expectedOrderList.remove(0);
		expectedOrderList.add(targetIdx, targetId);

		TagCommand.Move move = new TagCommand.Move(savedUser.getId(), targetId, targetIdx);

		// when
		tagService.moveUserTag(move);

		// then
		assertThat(savedUser.getTagOrderList().size()).isEqualTo(5);
		assertThat(savedUser.getTagOrderList()).isEqualTo(expectedOrderList);
	}

	@Test
	@DisplayName("태그 삭제 테스트")
	void deleteTagTest() {
		// given
		TagResult tagCreateResult = getTagCreateResult(0);
		TagCommand.Delete delete = new TagCommand.Delete(user.getId(), tagCreateResult.id());
		TagCommand.Read read = new TagCommand.Read(user.getId(), tagCreateResult.id());

		// when
		tagService.deleteTag(delete);

		// then
		assertThatThrownBy(() -> tagService.getTag(read))
			.isInstanceOf(ApiTagException.class)
			.hasMessageStartingWith(ApiTagException.TAG_NOT_FOUND().getMessage());
	}

	private TagResult getTagCreateResult(int n) {
		TagCommand.Create create = new TagCommand.Create(user.getId(), "태그" + n, 1);
		return tagService.saveTag(create);
	}

}