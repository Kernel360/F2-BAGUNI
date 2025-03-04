package baguni.api.service.tag.service;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import baguni.BaguniApiApplication;
import baguni.infra.infrastructure.folder.FolderRepository;
import baguni.infra.infrastructure.link.LinkRepository;
import baguni.infra.infrastructure.pick.PickRepository;
import baguni.infra.infrastructure.pick.PickTagRepository;
import baguni.infra.infrastructure.tag.TagRepository;
import baguni.infra.infrastructure.tag.dto.TagCommand;
import baguni.infra.infrastructure.user.UserRepository;
import baguni.infra.model.user.Role;
import baguni.infra.model.user.SocialProvider;
import baguni.infra.model.user.User;
import baguni.infra.model.util.IDToken;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = BaguniApiApplication.class)
@ActiveProfiles("local")
@DisplayName("태그 동시성 - 통합 테스트")
class TagServiceConcurrencyTest {

	@Autowired
	TagService tagService;

	@Autowired
	UserRepository userRepository;

	User user;

	@BeforeEach
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
		userRepository.deleteAll();
	}


	@Test
	@DisplayName("태그 저장 동시성 테스트")
	void createTagConcurrencyTest() throws InterruptedException {
		// given
		int threadCount = 20;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch countDownLatch = new CountDownLatch(threadCount);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		Long userId = user.getId();

		// when
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					TagCommand.Create command = new TagCommand.Create(userId, "태그12341", 2);
					tagService.saveTag(command);
					successCount.incrementAndGet(); // 성공 카운트
				} catch (Exception e) {
					log.info(e.getMessage());
					failCount.incrementAndGet(); // 실패 카운트
				} finally {
					countDownLatch.countDown();
				}
			});
		}

		countDownLatch.await(); // 모든 스레드가 완료될 때까지 대기
		executorService.shutdown();

		// then
		log.info("success : {} ", successCount.get());
		log.info("fail : {} ", failCount.get());

		assertThat(successCount.get()).isEqualTo(1);
		assertThat(failCount.get()).isEqualTo(threadCount - 1);
	}
}