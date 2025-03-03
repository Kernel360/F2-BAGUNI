package baguni.api.service.pick.service;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import baguni.infra.model.user.SocialProvider;
import baguni.infra.model.util.IDToken;
import lombok.extern.slf4j.Slf4j;
import baguni.BaguniApiApplication;
import baguni.infra.exception.folder.ApiFolderException;
import baguni.infra.infrastructure.link.dto.LinkInfo;
import baguni.infra.infrastructure.pick.dto.PickCommand;
import baguni.infra.infrastructure.pick.dto.PickResult;
import baguni.infra.exception.tag.ApiTagException;
import baguni.infra.model.folder.Folder;
import baguni.infra.infrastructure.folder.FolderRepository;
import baguni.infra.model.tag.Tag;
import baguni.infra.infrastructure.tag.TagRepository;
import baguni.infra.model.user.Role;
import baguni.infra.model.user.User;
import baguni.infra.infrastructure.user.UserRepository;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = BaguniApiApplication.class)
@ActiveProfiles("local")
@DisplayName("픽 검색 서비스 - 통합 테스트")
class PickSearchTest {

	@Autowired
	PickSearchService pickSearchService;

	@Autowired
	PickService pickService;

	User user1, user2;
	Folder root1, recycleBin1, unclassified1, general1;
	Folder root2, recycleBin2, unclassified2, general2;
	Tag tag1, tag2, tag3, tag4, tag5, tag6;

	@BeforeAll
	void setUp(
		@Autowired UserRepository userRepository,
		@Autowired FolderRepository folderRepository,
		@Autowired TagRepository tagRepository
	) {
		saveTestUser(userRepository);
		saveTestFolder(folderRepository);
		saveTestTag(tagRepository);
		saveUser1TestPick();
		saveUser2TestPick();
	}

	@ParameterizedTest(name = "다중 검색 조건 테스트 : {index} - {0} ")
	@MethodSource("provideSearchTestCases")
	void parameterizedSearchTest(TestCase testCase) {
		// given
		PickCommand.SearchPagination search = new PickCommand.SearchPagination(
			user1.getId(),
			testCase.folderIdList,
			testCase.searchTokenList,
			testCase.tagIdList,
			0L,
			30
		);

		// when
		Slice<PickResult.Pick> pickList = pickSearchService.searchPickPagination(search);

		// then
		assertThat(pickList).isNotNull();
		assertThat(pickList.getNumberOfElements()).isEqualTo(testCase.expectedCount);

		if (testCase.searchTokenList != null && !testCase.searchTokenList.isEmpty()) {
			for (PickResult.Pick pick : pickList) {
				boolean containsAllTokens = testCase.searchTokenList.stream()
																	.allMatch(token -> pick
																		.title()
																		.toLowerCase()
																		.contains(token.toLowerCase()));
				assertThat(containsAllTokens).isTrue();
			}
		}

		if (testCase.tagIdList != null && !testCase.tagIdList.isEmpty()) {
			for (PickResult.Pick pick : pickList) {
				assertThat(pick.tagIdOrderedList()).containsAll(testCase.tagIdList);
			}
		}

		if (testCase.folderIdList != null && !testCase.folderIdList.isEmpty()) {
			for (PickResult.Pick pick : pickList) {
				assertThat(testCase.folderIdList).contains(pick.parentFolderId());
			}
		}
	}

	@MethodSource("provideSearchTestCases")
	Stream<TestCase> provideSearchTestCases() {
		return Stream.of(
			new TestCase(new ArrayList<>(List.of(unclassified1.getId(), recycleBin1.getId())), new ArrayList<>(List.of("리액트", "서버", "스프링")),
				new ArrayList<>(List.of(tag1.getId(), tag2.getId())), 10, "모든 조건 검색"),
			new TestCase(null, new ArrayList<>(List.of("리액트", "서버", "스프링")), new ArrayList<>(List.of(tag1.getId(), tag2.getId())), 10, "폴더 null"),
			new TestCase(new ArrayList<>(List.of(unclassified1.getId(), recycleBin1.getId())), null, new ArrayList<>(List.of(tag1.getId(),
				tag2.getId())),
				20, "제목 검색 null"),
			new TestCase(new ArrayList<>(List.of(unclassified1.getId(), recycleBin1.getId())), new ArrayList<>(List.of("리액트", "서버", "스프링")), null, 10,
				"태그 null"),
			new TestCase(null, null, new ArrayList<>(List.of(tag1.getId(), tag2.getId())), 20, "폴더, 검색 null"),
			new TestCase(null, null, null, 30, "전체 null : 전체 검색"),
			new TestCase(new ArrayList<>(List.of(general1.getId())), new ArrayList<>(List.of("s")), null, 10, "폴더 : 일반, 제목 : s"),
			new TestCase(new ArrayList<>(List.of(general1.getId())), new ArrayList<>(List.of("T")), null, 5, "폴더 : 일반, 제목 : T"),
			new TestCase(new ArrayList<>(List.of(general1.getId())), null, new ArrayList<>(List.of(tag3.getId())), 10, "폴더 : 일반, 태그 : 3"),
			new TestCase(new ArrayList<>(List.of(recycleBin1.getId())), null, new ArrayList<>(List.of(tag3.getId())), 0, "폴더 : 휴지통, 태그 : 3")
		);
	}


	@Nested
	@DisplayName("제목 검색 테스트")
	@Transactional
	class TitleSearchConditionTest {

		static Stream<TestCase> provideTestCases() {
			return Stream.of(
				new TestCase(null, new ArrayList<>(List.of("리액트")), null, 10, "리액트"),
				new TestCase(null, new ArrayList<>(List.of("스프링")), null, 20, "스프링"),
				new TestCase(null, new ArrayList<>(List.of("Spring")), null, 5, "Spring"),
				new TestCase(null, new ArrayList<>(List.of("S", "g")), null, 5, "S g"),
				new TestCase(null, new ArrayList<>(List.of("S")), null, 10, "S"),
				new TestCase(null, new ArrayList<>(List.of("g")), null, 5, "g"),
				new TestCase(null, new ArrayList<>(List.of("스", "링")), null, 20, "스 링"),
				new TestCase(null, new ArrayList<>(List.of("프링")), null, 20, "프링"),
				new TestCase(null, new ArrayList<>(List.of("서버")), null, 10, "서버"),
				new TestCase(null, new ArrayList<>(List.of("트")), null, 10, "트"),
				new TestCase(null, new ArrayList<>(List.of("a".repeat(500))), null, 0, "긴 검색어") // 매우 긴 검색어
			);
		}

		@ParameterizedTest(name = "제목 검색 테스트 : {index} - {0}")
		@MethodSource("provideTestCases")
		void parameterizedTitleSearchTest(TestCase testCase) {
			// given
			PickCommand.SearchPagination search = new PickCommand.SearchPagination(
				user1.getId(),
				testCase.folderIdList,
				testCase.searchTokenList,
				testCase.tagIdList,
				0L,
				30
			);

			// when
			Slice<PickResult.Pick> pickList = pickSearchService.searchPickPagination(search);

			// then
			assertThat(pickList).isNotNull();
			assertThat(pickList.getNumberOfElements()).isEqualTo(testCase.expectedCount);

			if (testCase.expectedCount > 0) {
				for (PickResult.Pick pick : pickList) {
					boolean containsAllTokens = testCase.searchTokenList.stream()
																		.allMatch(token -> pick
																			.title()
																			.toLowerCase()
																			.contains(token.toLowerCase()));
					assertThat(containsAllTokens).isTrue();
				}
			}
		}
	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	@DisplayName("폴더 검색 테스트")
	@Transactional
	class FolderSearchConditionTest {

		@Test
		@DisplayName("루트 폴더 - 검색 불가")
		void rootFolderSearchTest() {
			// given
			List<Long> folderIdList = new ArrayList<>(List.of(root1.getId()));
			List<String> searchTokenList = null;
			List<Long> tagIdList = null;

			PickCommand.SearchPagination search = new PickCommand.SearchPagination(user1.getId(), folderIdList,
				searchTokenList, tagIdList,
				0L, 30);

			// when, then
			assertThatThrownBy(() -> pickSearchService.searchPickPagination(search))
				.isInstanceOf(ApiFolderException.class)
				.hasMessageStartingWith(ApiFolderException.ROOT_FOLDER_SEARCH_NOT_ALLOWED().getMessage());
		}

		@ParameterizedTest(name = "폴더 검색 테스트 : {index} - {0}")
		@MethodSource("provideFolderSearchTestCases")
		void parameterizedFolderSearchTest(TestCase testCase) {
			// given
			PickCommand.SearchPagination search = new PickCommand.SearchPagination(
				user1.getId(),
				testCase.folderIdList,
				testCase.searchTokenList,
				testCase.tagIdList,
				0L,
				30
			);

			// when
			Slice<PickResult.Pick> pickList = pickSearchService.searchPickPagination(search);

			// then
			assertThat(pickList).isNotNull();
			assertThat(pickList.getNumberOfElements()).isEqualTo(testCase.expectedCount);

			if (testCase.expectedCount > 0) {
				for (PickResult.Pick pick : pickList) {
					assertThat(pick.parentFolderId()).isEqualTo(testCase.folderIdList.get(0));
				}
			}
		}

		@MethodSource("provideFolderSearchTestCases")
		Stream<TestCase> provideFolderSearchTestCases() {
			return Stream.of(
				new TestCase(new ArrayList<>(List.of(unclassified1.getId())), null, null, 20, "미분류 폴더"),
				new TestCase(new ArrayList<>(List.of(general1.getId())), null, null, 10, "일반 폴더 - React.js"),
				new TestCase(new ArrayList<>(List.of(recycleBin1.getId())), null, null, 0, "휴지통 폴더")
			);
		}
	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	@DisplayName("태그 검색 테스트")
	@Transactional
	class TagSearchConditionTest {

		@ParameterizedTest(name = "태그 검색 테스트 : {index} - {0}")
		@MethodSource("provideTagSearchTestCases")
		void parameterizedTagSearchTest(TestCase testCase) {
			// given
			PickCommand.SearchPagination search = new PickCommand.SearchPagination(
				user1.getId(),
				testCase.folderIdList,
				testCase.searchTokenList,
				testCase.tagIdList,
				0L,
				30
			);

			// when
			Slice<PickResult.Pick> pickList = pickSearchService.searchPickPagination(search);

			// then
			assertThat(pickList).isNotNull();
			assertThat(pickList.getNumberOfElements()).isEqualTo(testCase.expectedCount);

			if (testCase.expectedCount > 0) {
				for (PickResult.Pick pick : pickList) {
					assertThat(pick.tagIdOrderedList()).containsAll(testCase.tagIdList);
				}
			}
		}

		@MethodSource("provideTagSearchTestCases")
		Stream<TestCase> provideTagSearchTestCases() {
			return Stream.of(
				new TestCase(null, null, new ArrayList<>(List.of(tag1.getId())), 30, "태그 1"),
				new TestCase(null, null, new ArrayList<>(List.of(tag2.getId())), 20, "태그 2"),
				new TestCase(null, null, new ArrayList<>(List.of(tag3.getId())), 20, "태그 3"),
				new TestCase(null, null, new ArrayList<>(List.of(tag1.getId(), tag2.getId())), 20, "태그 1, 2"),
				new TestCase(null, null, new ArrayList<>(List.of(tag1.getId(), tag3.getId())), 20, "태그 1, 3"),
				new TestCase(null, null, new ArrayList<>(List.of(tag2.getId(), tag3.getId())), 10, "태그 2, 3"),
				new TestCase(null, null, new ArrayList<>(List.of(tag1.getId(), tag2.getId(), tag3.getId())), 10, "태그 1, 2, 3")
			);
		}
	}

	@Nested
	@DisplayName("존재 여부 및 예외 테스트")
	class exceptionTest {

		@Test
		@DisplayName("존재하지 않는 폴더")
		void exceptionTest1() {
			// given
			List<Long> folderIdList = new ArrayList<>(List.of(999L));
			List<String> searchTokenList = null;
			List<Long> tagIdList = null;

			PickCommand.SearchPagination search = new PickCommand.SearchPagination(user1.getId(), folderIdList,
				searchTokenList, tagIdList,
				0L, 30);

			// when, then
			assertThatThrownBy(() -> pickSearchService.searchPickPagination(search))
				.isInstanceOf(ApiFolderException.class)
				.hasMessageStartingWith(ApiFolderException.FOLDER_NOT_FOUND().getMessage());
		}

		@Test
		@DisplayName("존재하지 않는 제목")
		void exceptionTest2() {
			// given
			List<Long> folderIdList = null;
			List<String> searchTokenList = new ArrayList<>(List.of("검색결과가없음"));
			List<Long> tagIdList = null;

			PickCommand.SearchPagination search = new PickCommand.SearchPagination(user1.getId(), folderIdList,
				searchTokenList, tagIdList,
				0L, 30);

			// when
			Slice<PickResult.Pick> pickList = pickSearchService.searchPickPagination(search);

			// then
			assertThat(pickList).isNotNull();
			assertThat(pickList.getNumberOfElements()).isEqualTo(0); // 검색 결과 수
		}

		@Test
		@DisplayName("존재하지 않는 제목, 태그")
		void exceptionTest3() {
			// given
			List<Long> folderIdList = null;
			List<String> searchTokenList = new ArrayList<>(List.of("검색결과가없음"));
			List<Long> tagIdList = new ArrayList<>(List.of(999L));

			PickCommand.SearchPagination search = new PickCommand.SearchPagination(user1.getId(), folderIdList,
				searchTokenList, tagIdList,
				0L, 30);

			// when, then
			assertThatThrownBy(() -> pickSearchService.searchPickPagination(search))
				.isInstanceOf(ApiTagException.class)
				.hasMessageStartingWith(ApiTagException.TAG_NOT_FOUND().getMessage());
		}

		@Test
		@DisplayName("다른 유저의 폴더 리스트 검색")
		void exceptionTest4() {
			// given
			List<Long> folderIdList = new ArrayList<>(List.of(unclassified2.getId()));
			List<String> searchTokenList = null;
			List<Long> tagIdList = null;

			PickCommand.SearchPagination search = new PickCommand.SearchPagination(user1.getId(), folderIdList,
				searchTokenList, tagIdList,
				0L, 30);

			// when, then
			assertThatThrownBy(() -> pickSearchService.searchPickPagination(search))
				.isInstanceOf(ApiFolderException.class)
				.hasMessageStartingWith(ApiFolderException.FOLDER_ACCESS_DENIED().getMessage());
		}

		@Test
		@DisplayName("다른 유저의 태그 리스트 검색")
		void exceptionTest5() {
			// given
			List<Long> folderIdList = null;
			List<String> searchTokenList = null;
			List<Long> tagIdList = new ArrayList<>(List.of(tag4.getId()));

			PickCommand.SearchPagination search = new PickCommand.SearchPagination(user1.getId(), folderIdList,
				searchTokenList, tagIdList,
				0L, 30);

			// when, then
			assertThatThrownBy(() -> pickSearchService.searchPickPagination(search))
				.isInstanceOf(ApiTagException.class)
				.hasMessageStartingWith(ApiTagException.UNAUTHORIZED_TAG_ACCESS().getMessage());
		}
	}

	// Test Case Class
	static class TestCase {
		List<Long> folderIdList;
		List<String> searchTokenList;
		List<Long> tagIdList;
		int expectedCount;
		String description;

		TestCase(List<Long> folderIdList, List<String> searchTokenList, List<Long> tagIdList, int expectedCount,
			String description) {
			this.folderIdList = folderIdList;
			this.searchTokenList = searchTokenList;
			this.tagIdList = tagIdList;
			this.expectedCount = expectedCount;
			this.description = description;
		}

		@Override
		public String toString() {
			return description;
		}
	}

	// Test Data Setting Method
	private void saveUser1TestPick() {
		for (int i = 0; i < 10; i++) {
			LinkInfo linkInfo = new LinkInfo("리액트" + i, "링크 제목", "링크 설명", "링크 이미지 url");
			PickCommand.Create command = new PickCommand.Create(user1.getId(), "리액트" + i + "서버" + i + "스프링",
				List.of(tag1.getId(), tag2.getId()), unclassified1.getId(), linkInfo);
			pickService.saveNewPick(command);
		}

		for (int i = 0; i < 10; i++) {
			LinkInfo linkInfo = new LinkInfo("스프링" + i, "링크 제목", "링크 설명", "링크 이미지 url");
			PickCommand.Create command = new PickCommand.Create(user1.getId(), "스프링" + i,
				List.of(tag1.getId(), tag2.getId(), tag3.getId()), unclassified1.getId(), linkInfo);
			pickService.saveNewPick(command);
		}

		for (int i = 0; i < 5; i++) {
			LinkInfo linkInfo = new LinkInfo("Spring" + i, "링크 제목", "링크 설명", "링크 이미지 url");
			PickCommand.Create command = new PickCommand.Create(user1.getId(), "Spring" + i,
				List.of(tag1.getId(), tag3.getId()), general1.getId(), linkInfo);
			pickService.saveNewPick(command);
		}

		for (int i = 0; i < 5; i++) {
			LinkInfo linkInfo = new LinkInfo("Test" + i, "링크 제목", "링크 설명", "링크 이미지 url");
			PickCommand.Create command = new PickCommand.Create(user1.getId(), "Test" + i,
				List.of(tag1.getId(), tag3.getId()), general1.getId(), linkInfo);
			pickService.saveNewPick(command);
		}
	}

	private void saveUser2TestPick() {
		for (int i = 0; i < 5; i++) {
			LinkInfo linkInfo = new LinkInfo("user2" + i, "링크 제목", "링크 설명", "링크 이미지 url");
			PickCommand.Create command = new PickCommand.Create(user2.getId(), "Backend",
				List.of(tag4.getId(), tag5.getId()), unclassified2.getId(), linkInfo);
			pickService.saveNewPick(command);
		}

		for (int i = 0; i < 5; i++) {
			LinkInfo linkInfo = new LinkInfo("user2s" + i, "링크 제목", "링크 설명", "링크 이미지 url");
			PickCommand.Create command = new PickCommand.Create(user2.getId(), "Frontend",
				List.of(tag6.getId()), unclassified2.getId(), linkInfo);
			pickService.saveNewPick(command);
		}
	}

	private void saveTestTag(TagRepository tagRepository) {
		// save test tag
		tag1 = tagRepository.save(Tag
			.builder().user(user1).name("tag1").colorNumber(1).build());
		tag2 = tagRepository.save(Tag
			.builder().user(user1).name("tag2").colorNumber(1).build());
		tag3 = tagRepository.save(Tag
			.builder().user(user1).name("tag3").colorNumber(1).build());

		tag4 = tagRepository.save(Tag
			.builder().user(user2).name("tag4").colorNumber(1).build());
		tag5 = tagRepository.save(Tag
			.builder().user(user2).name("tag5").colorNumber(1).build());
		tag6 = tagRepository.save(Tag
			.builder().user(user2).name("tag6").colorNumber(1).build());
	}

	private void saveTestFolder(FolderRepository folderRepository) {
		// save test folder
		root1 = folderRepository.save(Folder.createEmptyRootFolder(user1));
		recycleBin1 = folderRepository.save(Folder.createEmptyRecycleBinFolder(user1));
		unclassified1 = folderRepository.save(Folder.createEmptyUnclassifiedFolder(user1));
		general1 = folderRepository.save(Folder.createEmptyGeneralFolder(user1, root1, "React.js"));

		root2 = folderRepository.save(Folder.createEmptyRootFolder(user2));
		recycleBin2 = folderRepository.save(Folder.createEmptyRecycleBinFolder(user2));
		unclassified2 = folderRepository.save(Folder.createEmptyUnclassifiedFolder(user2));
		general2 = folderRepository.save(Folder.createEmptyGeneralFolder(user2, root2, "Backend"));
	}

	private void saveTestUser(UserRepository userRepository) {
		// save test user
		user1 = userRepository.save(
			User
				.builder()
				.email("user1@test.com")
				.nickname("user1")
				.password("user1")
				.role(Role.ROLE_USER)
				.socialProvider(SocialProvider.KAKAO)
				.socialProviderId("1")
				.tagOrderList(new ArrayList<>())
				.idToken(IDToken.makeNew())
				.build()
		);

		// save test user
		user2 = userRepository.save(
			User
				.builder()
				.email("user2@test.com")
				.nickname("user2")
				.password("user2")
				.role(Role.ROLE_USER)
				.socialProvider(SocialProvider.KAKAO)
				.socialProviderId("2")
				.tagOrderList(new ArrayList<>())
				.idToken(IDToken.makeNew())
				.build()
		);
	}
}