package baguni.api.service.folder.service;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import baguni.infra.model.util.IDToken;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import baguni.BaguniApiApplication;
import baguni.infra.infrastructure.folder.dto.FolderCommand;
import baguni.infra.infrastructure.folder.dto.FolderResult;
import baguni.infra.infrastructure.folder.FolderDataHandler;
import baguni.infra.model.folder.Folder;
import baguni.infra.infrastructure.folder.FolderRepository;
import baguni.infra.model.user.Role;
import baguni.infra.model.user.SocialProvider;
import baguni.infra.model.user.User;
import baguni.infra.infrastructure.user.UserRepository;

@Slf4j
@SpringBootTest(classes = BaguniApiApplication.class)
@ActiveProfiles("local")
@DisplayName("폴더 서비스 - 통합 테스트")
class FolderServiceTest {

	@Autowired
	FolderService folderService;

	@Autowired
	FolderDataHandler folderDataHandler;

	@Autowired
	FolderRepository folderRepository;

	@Autowired
	UserRepository userRepository;

	User user;
	Folder root, recycleBin, unclassified;

	@BeforeEach
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
	}

	@Test
	@DisplayName("폴더 삭제 시 부모 폴더의 하위 폴더 리스트 업데이트 관련 데드락 테스트")
	void updateParentFolderChildListDeadlock() throws InterruptedException {
		// given
		int threadCount = 3;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch readyLatch = new CountDownLatch(threadCount); // 준비 상태를 대기
		CountDownLatch startLatch = new CountDownLatch(1);          // 동시에 시작
		CountDownLatch finishLatch = new CountDownLatch(threadCount); // 완료 상태를 대기

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		FolderResult react = folderService.saveFolder(new FolderCommand.Create(user.getId(), "React", root.getId()));
		FolderResult spring = folderService.saveFolder(new FolderCommand.Create(user.getId(), "Spring", root.getId()));
		FolderResult java = folderService.saveFolder(new FolderCommand.Create(user.getId(), "Java", root.getId()));

		List<Long> folderIds = List.of(react.id(), spring.id(), java.id());

		// when
		for (int i = 0; i < threadCount; i++) {
			int index = i; // 쓰레드마다 삭제할 폴더 ID를 다르게 설정
			executorService.submit(() -> {
				try {
					readyLatch.countDown(); // 준비 완료
					startLatch.await(); // 다른 쓰레드와 동시에 시작
					folderService.deleteFolder(new FolderCommand.Delete(user.getId(), List.of(folderIds.get(index))));
					successCount.incrementAndGet();
				} catch (OptimisticLockException e) {
					log.info("Optimistic lock exception : {}", e.getMessage());
					failCount.incrementAndGet();
				} catch (Exception e) {
					log.error("Unexpected exception : {}", e.getMessage(), e);
				} finally {
					finishLatch.countDown(); // 작업 완료
				}
			});
		}

		// 모든 쓰레드가 준비될 때까지 대기
		readyLatch.await();

		// 모든 쓰레드가 동시에 시작
		startLatch.countDown();

		// 모든 쓰레드가 작업을 마칠 때까지 대기
		finishLatch.await();

		// then
		log.info("Success count: {}", successCount.get());
		log.info("Fail count: {}", failCount.get());

		Folder rootFolder = folderDataHandler.getFolder(root.getId());
		assertThat(rootFolder.getChildFolderIdOrderedList().size()).isEqualTo(0);
	}

}
