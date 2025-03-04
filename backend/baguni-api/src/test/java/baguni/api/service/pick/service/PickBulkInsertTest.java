package baguni.api.service.pick.service;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import baguni.infra.model.util.IDToken;
import lombok.extern.slf4j.Slf4j;
import baguni.BaguniApiApplication;
import baguni.infra.infrastructure.link.dto.LinkInfo;
import baguni.infra.infrastructure.pick.dto.PickCommand;
import baguni.infra.model.folder.Folder;
import baguni.infra.infrastructure.folder.FolderRepository;
import baguni.infra.model.user.Role;
import baguni.infra.model.user.SocialProvider;
import baguni.infra.model.user.User;
import baguni.infra.infrastructure.user.UserRepository;

@Slf4j
@SpringBootTest(classes = BaguniApiApplication.class)
@ActiveProfiles("local")
@DisplayName("픽 Bulk Insert - 통합 테스트")
class PickBulkInsertTest {

	@Autowired
	PickService pickService;

	@Autowired
	PickBulkService pickBulkService;

	User user;
	Folder root, recycleBin, unclassified, general;

	@BeforeEach
		// TODO: change to Adaptor
	void setUp(
		@Autowired UserRepository userRepository,
		@Autowired FolderRepository folderRepository
	) {
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
	}

	@Test
	@DisplayName("픽 100개 bulk insert test")
	void pickBulkInsertTest() {
		long start = System.currentTimeMillis();
		pickBulkService.saveBulkPick(user.getId(), unclassified.getId());
		long end = System.currentTimeMillis();

		log.info("bulk insert time : {}", (end - start));
	}

	@Test
	@DisplayName("픽 100개 normal insert test")
	void pickInsertTest() {
		long start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			LinkInfo linkInfo = new LinkInfo("test" + i, "링크 제목", "링크 설명", "링크 이미지 url");
			PickCommand.Create command = new PickCommand.Create(user.getId(), "테스트 제목", new ArrayList<>(),
				unclassified.getId(), linkInfo);
			pickService.saveNewPick(command);
		}
		long end = System.currentTimeMillis();

		log.info("normal insert time : {}", (end - start));
	}
}
