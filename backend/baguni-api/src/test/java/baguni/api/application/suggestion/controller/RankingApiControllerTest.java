package baguni.api.application.suggestion.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import baguni.BaguniApiApplication;
import baguni.api.application.suggestion.dto.RankingApiMapper;
import baguni.api.application.suggestion.dto.RankingApiMapperImpl;
import baguni.api.service.link.service.LinkService;
import baguni.api.service.ranking.dto.RankingResult;
import baguni.api.service.ranking.service.RankingService;
import baguni.api.service.user.service.UserService;
import baguni.common.dto.UrlWithCount;
import baguni.infra.infrastructure.link.dto.LinkInfo;
import baguni.infra.infrastructure.user.dto.UserInfo;
import baguni.infra.model.util.IDToken;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = BaguniApiApplication.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc // MockMvc 사용을 위한 설정
@Import(RankingApiMapperImpl.class) // @SpyBean 구현체 인식을 못하여 추가
@DisplayName("랭킹 컨트롤러 - 슬라이스 테스트")
class RankingApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SecurityFilterChain securityFilterChain; // 실제 시큐리티 구현체 사용하지 않기 위함.

	@MockBean
	private UserService userService;

	@SpyBean
	private RankingApiMapper rankingApiMapper;

	@MockBean
	private LinkService linkService;

	@MockBean
	private RankingService rankingService;

	Long userId;
	IDToken idToken;
	UserInfo userInfo;

	@BeforeEach
	void setUp() {
		userId = 1L;
		idToken = IDToken.fromString(UUID.randomUUID().toString());
		var authentication = new UsernamePasswordAuthenticationToken(idToken, null,
			List.of(new SimpleGrantedAuthority("ROLE_USER")));

		// @LoginUserId에서 사용하므로 임의의 데이터 세팅
		SecurityContextHolder.getContext().setAuthentication(authentication);

		userInfo = new UserInfo(userId, "user", idToken, "email@example.com");

		// 반드시 있어야 합니다. userInfo 객체를 인식합니다.
		given(userService.getUserInfoByToken(idToken)).willReturn(userInfo);
	}

	@AfterEach
	void cleanUp() {
		// 설정했던 시큐리티 데이터 제거
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("인기 픽 랭킹 조회")
	void get_ranking() throws Exception {
		// given
		List<UrlWithCount> dailyViewRanking = List.of(
			new UrlWithCount("http://example.com/1", 100L),
			new UrlWithCount("http://example.com/2", 90L)
		);

		List<UrlWithCount> past7DaysViewRanking = List.of(
			new UrlWithCount("http://example.com/3", 300L),
			new UrlWithCount("http://example.com/4", 250L)
		);

		List<UrlWithCount> past30DaysPickRanking = List.of(
			new UrlWithCount("http://example.com/5", 50L),
			new UrlWithCount("http://example.com/6", 45L)
		);

		var result = new RankingResult(
			dailyViewRanking, past7DaysViewRanking, past30DaysPickRanking
		);

		given(rankingService.getUrlRanking(10)).willReturn(result);

		for (UrlWithCount urlWithCount : dailyViewRanking) {
			given(linkService.getLinkInfo(urlWithCount.url()))
				.willReturn(new LinkInfo(urlWithCount.url(), "Title - " + urlWithCount.url(), "desc", "image.jpg"));
		}
		for (UrlWithCount urlWithCount : past7DaysViewRanking) {
			given(linkService.getLinkInfo(urlWithCount.url()))
				.willReturn(new LinkInfo(urlWithCount.url(), "Title - " + urlWithCount.url(), "desc", "image.jpg"));
		}
		for (UrlWithCount urlWithCount : past30DaysPickRanking) {
			given(linkService.getLinkInfo(urlWithCount.url()))
				.willReturn(new LinkInfo(urlWithCount.url(), "Title - " + urlWithCount.url(), "desc", "image.jpg"));
		}

		// when
		mockMvc.perform(get("/api/suggestion/ranking")
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isOk());

		// then
		then(rankingService).should().getUrlRanking(10);

		rankingApiMapper.toRankingWithLinkInfo(null, null);
		rankingApiMapper.toRankingWithLinkInfo(new UrlWithCount("http://example.com/5", 50L), null);
		rankingApiMapper.toRankingWithLinkInfo(null, new LinkInfo("", "Title", "desc", "image.jpg"));
	}

	@Test
	@DisplayName("인기 픽 랭킹 조회 - 예외 테스트")
	void get_ranking_exception() throws Exception {
		// given
		List<UrlWithCount> dailyViewRanking = List.of(
			new UrlWithCount("http://example.com/1", 100L),
			new UrlWithCount("http://example.com/2", 90L)
		);

		List<UrlWithCount> past30DaysPickRanking = List.of(
			new UrlWithCount("http://example.com/5", 50L),
			new UrlWithCount("http://example.com/6", 45L)
		);

		var result = new RankingResult(
			dailyViewRanking, null, past30DaysPickRanking
		);

		given(rankingService.getUrlRanking(10)).willReturn(result);

		for (UrlWithCount urlWithCount : dailyViewRanking) {
			given(linkService.getLinkInfo(urlWithCount.url()))
				.willReturn(new LinkInfo(urlWithCount.url(), "", "desc", "image.jpg"));
		}

		for (UrlWithCount urlWithCount : past30DaysPickRanking) {
			given(linkService.getLinkInfo(urlWithCount.url()))
				.willReturn(new LinkInfo(urlWithCount.url(), "Title - " + urlWithCount.url(), "desc", "image.jpg"));
		}

		// when
		mockMvc.perform(get("/api/suggestion/ranking")
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isOk());

		// then
		then(rankingService).should().getUrlRanking(10);
	}

}