package baguni.api.service.user.service.strategy;

import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import baguni.api.fixture.FolderFixture;
import baguni.api.fixture.UserFixture;
import baguni.api.service.link.service.LinkService;
import baguni.api.service.pick.service.PickService;
import baguni.api.service.ranking.service.RankingApi;
import baguni.common.dto.UrlWithCount;
import baguni.infra.exception.link.ApiLinkException;
import baguni.infra.infrastructure.link.dto.LinkInfo;
import baguni.infra.infrastructure.user.dto.UserInfo;
import baguni.infra.model.folder.Folder;
import baguni.infra.model.user.User;
import baguni.infra.model.util.IDToken;

@DisplayName("랭킹 초기 전략 - 단위 테스트")
@ExtendWith(MockitoExtension.class)
class RankingInitStrategyTest {

	@Mock
	private RankingApi rankingApi;

	@Mock
	private PickService pickService;

	@Mock
	private LinkService linkService;

	@InjectMocks
	private RankingInitStrategy strategy;

	@Test
	@DisplayName("정상 초기화")
	void init_test() {
		// given
		var currentDay = LocalDate.now();
		var before1Day = currentDay.minusDays(1);
		var before30Days = currentDay.minusDays(30);

		List<UrlWithCount> monlyRanking = List.of(
			new UrlWithCount("http://example.com/1", 100L),
			new UrlWithCount("http://example.com/2", 90L)
		);

		User user = UserFixture
			.builder().id(1L).nickname("name").idToken(IDToken.makeNew()).email("email").build().get();
		Folder folder = FolderFixture
			.builder().id(1L).user(user).build().get();

		UserInfo userInfo = UserInfo.from(user);

		LinkInfo linkInfo1 = new LinkInfo("http://example.com/1", "Title 1", "", "");
		LinkInfo linkInfo2 = new LinkInfo("http://example.com/2", "", "", "");

		given(rankingApi.getUrlRankingByViewCount(before30Days, before1Day, 5)).willReturn(ResponseEntity.ok(monlyRanking));
		given(linkService.getLinkInfo("http://example.com/1")).willReturn(linkInfo1);
		given(linkService.getLinkInfo("http://example.com/2")).willReturn(linkInfo2);

		// when
		strategy.initContent(userInfo, folder.getId());

		// then
		then(rankingApi).should(times(1)).getUrlRankingByViewCount(before30Days, before1Day, 5);
	}

	@Test
	@DisplayName("랭킹 리스트가 비어있는 경우")
	void initContent_with_empty_ranking_list() {
		// given
		var currentDay = LocalDate.now();
		var before1Day = currentDay.minusDays(1);
		var before30Days = currentDay.minusDays(30);

		User user = UserFixture
			.builder().id(1L).nickname("name").idToken(IDToken.makeNew()).email("email").build().get();
		Folder folder = FolderFixture
			.builder().id(1L).user(user).build().get();

		UserInfo userInfo = UserInfo.from(user);

		given(rankingApi.getUrlRankingByViewCount(before30Days, before1Day, 5)).willReturn(ResponseEntity.ok(null));

		// when
		strategy.initContent(userInfo, folder.getId());

		// then
		then(rankingApi).should(times(1)).getUrlRankingByViewCount(before30Days, before1Day, 5);
		then(linkService).shouldHaveNoInteractions();
		then(pickService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("랭킹 초기화 시 예외 발생")
	void initContent_exception() {
		// given
		var currentDay = LocalDate.now();
		var before1Day = currentDay.minusDays(1);
		var before30Days = currentDay.minusDays(30);

		User user = UserFixture
			.builder().id(1L).nickname("name").idToken(IDToken.makeNew()).email("email").build().get();
		Folder folder = FolderFixture
			.builder().id(1L).user(user).build().get();

		UserInfo userInfo = UserInfo.from(user);

		given(rankingApi.getUrlRankingByViewCount(before30Days, before1Day, 5))
			.willThrow(new RuntimeException("예외 발생"));

		// when
		strategy.initContent(userInfo, folder.getId());

		// then
		then(rankingApi).should(times(1)).getUrlRankingByViewCount(before30Days, before1Day, 5);
		then(linkService).shouldHaveNoInteractions();
		then(pickService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("링크 정보 조회 실패")
	void initContent_with_link_info_failure() {
		// given
		var currentDay = LocalDate.now();
		var before1Day = currentDay.minusDays(1);
		var before30Days = currentDay.minusDays(30);

		List<UrlWithCount> rankingList = List.of(
			new UrlWithCount("http://example.com/1", 100L),
			new UrlWithCount("http://example.com/2", 90L)
		);

		User user = UserFixture.builder()
			.id(1L).nickname("name").idToken(IDToken.makeNew())
			.email("email").build().get();
		Folder folder = FolderFixture.builder().id(1L).user(user).build().get();
		UserInfo userInfo = UserInfo.from(user);

		LinkInfo linkInfo2 = new LinkInfo("http://example.com/2", "Title 2", "", "");

		given(rankingApi.getUrlRankingByViewCount(before30Days, before1Day, 5))
			.willReturn(ResponseEntity.ok(rankingList));
		given(linkService.getLinkInfo("http://example.com/1"))
			.willThrow(ApiLinkException.LINK_NOT_FOUND());
		given(linkService.saveLink("http://example.com/1"))
			.willReturn(new LinkInfo("http://example.com/1", "Saved Title", "", ""));
		given(linkService.getLinkInfo("http://example.com/2"))
			.willReturn(linkInfo2);

		// when
		strategy.initContent(userInfo, folder.getId());

		// then
		then(rankingApi).should(times(1)).getUrlRankingByViewCount(before30Days, before1Day, 5);
		then(linkService).should(times(1)).getLinkInfo("http://example.com/1");
		then(linkService).should(times(1)).saveLink("http://example.com/1");
		then(linkService).should(times(1)).getLinkInfo("http://example.com/2");
	}
}