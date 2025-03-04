package baguni.api.service.user.service.strategy;

import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import baguni.api.fixture.FolderFixture;
import baguni.api.fixture.UserFixture;
import baguni.api.service.link.service.LinkService;
import baguni.api.service.pick.service.PickService;
import baguni.infra.exception.link.ApiLinkException;
import baguni.infra.infrastructure.link.dto.LinkInfo;
import baguni.infra.infrastructure.user.dto.UserInfo;
import baguni.infra.model.folder.Folder;
import baguni.infra.model.user.User;
import baguni.infra.model.util.IDToken;

@DisplayName("메뉴얼 초기 전략 - 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ManualInitStrategyTest {

	@Mock
	private PickService pickService;

	@Mock
	private LinkService linkService;

	@InjectMocks
	private ManualInitStrategy strategy;

	List<String> urlList;

	@BeforeEach
	void setUp() {
		urlList = List.of(
			// 설문 조사
			"https://positive-airboat-4de.notion.site/15841a7fba6580f78caee50c069a1247?pvs=4",
			// 확장 프로그램
			"https://positive-airboat-4de.notion.site/15841a7fba65808b8636e15e6c6d9679?pvs=4",
			// 앱 사용법
			"https://positive-airboat-4de.notion.site/15841a7fba65809d89a6dceb89060f70?pvs=4"
		);
	}

	@Test
	@DisplayName("정상 초기화")
	void init_content_test() {
		// given
		User user = UserFixture
			.builder().id(1L).nickname("name").idToken(IDToken.makeNew()).email("email").build().get();
		Folder folder = FolderFixture
			.builder().id(1L).user(user).build().get();

		UserInfo userInfo = UserInfo.from(user);

		for (int i = 0; i < urlList.size(); i++) {
			String url = urlList.get(i);
			LinkInfo linkInfo = new LinkInfo(url, "Title " + (i + 1), "", "");
			given(linkService.getLinkInfo(url)).willReturn(linkInfo);
		}

		// when
		strategy.initContent(userInfo, folder.getId());

		// then
		then(linkService).should(times(1)).getLinkInfo(urlList.get(0));
		then(linkService).should(times(1)).getLinkInfo(urlList.get(1));
		then(linkService).should(times(1)).getLinkInfo(urlList.get(2));
	}

	@Test
	@DisplayName("예외 상황 테스트")
	void init_content_exception_test() {
		// given
		User user = UserFixture
			.builder().id(1L).nickname("name").idToken(IDToken.makeNew()).email("email").build().get();
		Folder folder = FolderFixture
			.builder().id(1L).user(user).build().get();

		UserInfo userInfo = UserInfo.from(user);

		for (int i = 0; i < urlList.size(); i++) {
			String url = urlList.get(i);
			if (i == 0) {
				given(linkService.getLinkInfo(urlList.get(i)))
					.willThrow(ApiLinkException.LINK_NOT_FOUND());
				given(linkService.saveLink(url)).willReturn(new LinkInfo(url, "Saved Title " + (i + 1), "", ""));
				continue;
			}
			LinkInfo linkInfo = new LinkInfo(url, "Title " + (i + 1), "", "");
			given(linkService.getLinkInfo(url)).willReturn(linkInfo);
		}

		// when
		strategy.initContent(userInfo, folder.getId());

		// then
		then(linkService).should(times(1)).getLinkInfo(urlList.get(0));
		then(linkService).should(times(1)).getLinkInfo(urlList.get(1));
		then(linkService).should(times(1)).getLinkInfo(urlList.get(2));
	}
}