package baguni.api.service.link.service;

import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import baguni.api.fixture.LinkFixture;
import baguni.infra.infrastructure.link.LinkDataHandler;
import baguni.infra.infrastructure.link.dto.LinkMapper;
import baguni.infra.model.link.Link;

@DisplayName("링크 서비스 - 단위 테스트")
@ExtendWith(MockitoExtension.class)
class LinkServiceUnitTest {

	@Mock
	private LinkDataHandler linkDataHandler;

	@Mock
	private LinkMapper linkMapper;

	@InjectMocks
	private LinkService linkService;

	String url;
	Link link;
	@BeforeEach
	void setUp() {
		url = "https://example.com";
		link = LinkFixture
			.builder().id(1L).url(url).build().get();
	}

	@Test
	@DisplayName("링크 정보 조회")
	void get_link() {
		// given
		given(linkDataHandler.getLink(url)).willReturn(link);

		// when
		linkService.getLinkInfo(url);

		// then
		then(linkDataHandler).should(times(1)).getLink(url);
		then(linkMapper).should(times(1)).of(link);
	}

	@Test
	@DisplayName("링크 저장")
	void save_link() {
		// given
		given(linkDataHandler.getOptionalLink(url)).willReturn(Optional.of(link));
		given(linkDataHandler.saveLink(link)).willReturn(link);

		// when
		linkService.saveLink(url);

		// then
		then(linkDataHandler).should(times(1)).getOptionalLink(url);
		then(linkDataHandler).should(times(1)).saveLink(link);
		then(linkMapper).should(times(1)).of(link);
	}

	@Test
	@DisplayName("RSS 리스트 조회")
	void rss_link_list() {
		// given
		List<Link> list = List.of(link);
		given(linkDataHandler.getRssLinkList(10)).willReturn(list);

		// when
		linkService.getRssLinkList(10);

		// then
		then(linkDataHandler).should(times(1)).getRssLinkList(10);
		then(linkMapper).should(times(1)).toBlogLinkInfo(link);
	}
}