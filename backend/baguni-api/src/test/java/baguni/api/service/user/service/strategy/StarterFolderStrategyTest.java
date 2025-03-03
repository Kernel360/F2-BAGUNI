package baguni.api.service.user.service.strategy;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import baguni.api.fixture.FolderFixture;
import baguni.api.fixture.UserFixture;
import baguni.infra.infrastructure.folder.FolderDataHandler;
import baguni.infra.infrastructure.folder.dto.FolderCommand;
import baguni.infra.infrastructure.user.dto.UserInfo;
import baguni.infra.model.folder.Folder;
import baguni.infra.model.folder.FolderType;
import baguni.infra.model.user.User;
import baguni.infra.model.util.IDToken;

@DisplayName("시작하기 폴더 - 단위 테스트")
@ExtendWith(MockitoExtension.class)
class StarterFolderStrategyTest {

	@Mock
	private FolderDataHandler folderDataHandler;

	@Mock
	private RankingInitStrategy rankingInitStrategy;

	@Mock
	private ManualInitStrategy manualInitStrategy;

	@InjectMocks
	private StarterFolderStrategy strategy;

	String folderName;

	@BeforeEach
	void setUp() {
		folderName = "시작하기";
	}

	@Test
	@DisplayName("루트 폴더 내에 시작하기 폴더 생성")
	void init_folder() {
		// given
		User user = UserFixture
			.builder().id(1L).nickname("name").idToken(IDToken.makeNew()).email("email").build().get();
		Folder rootFolder = FolderFixture
			.builder().id(1L).user(user).folderType(FolderType.ROOT).build().get();

		UserInfo userInfo = UserInfo.from(user);

		var create = new FolderCommand.Create(user.getId(), folderName, rootFolder.getId());
		Folder startFolder = FolderFixture
			.builder().id(2L).user(user).name(folderName).folderType(FolderType.GENERAL).build().get();

		given(folderDataHandler.getRootFolder(userInfo.id())).willReturn(rootFolder);
		given(folderDataHandler.saveFolder(create)).willReturn(startFolder);

		// when
		strategy.initRootFolder(userInfo);

		// then
		then(folderDataHandler).should(times(1)).getRootFolder(userInfo.id());
		then(folderDataHandler).should(times(1)).saveFolder(create);
	}
}