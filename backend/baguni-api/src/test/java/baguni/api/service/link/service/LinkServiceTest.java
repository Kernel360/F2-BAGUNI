package baguni.api.service.link.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import baguni.BaguniApiApplication;
import baguni.infra.infrastructure.folder.FolderRepository;
import baguni.infra.infrastructure.link.LinkDataHandler;
import baguni.infra.infrastructure.link.LinkRepository;
import baguni.infra.infrastructure.link.dto.BlogLinkInfo;
import baguni.infra.infrastructure.link.dto.LinkInfo;
import baguni.infra.infrastructure.link.dto.LinkMapper;
import baguni.infra.infrastructure.pick.PickRepository;
import baguni.infra.infrastructure.pick.PickTagRepository;
import baguni.infra.infrastructure.tag.TagRepository;
import baguni.infra.model.link.Link;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = BaguniApiApplication.class)
@ActiveProfiles("local")
@DisplayName("링크 서비스 - 통합 테스트")
class LinkServiceTest {

	@Autowired
	LinkDataHandler linkDataHandler;

	@Autowired
	LinkMapper linkMapper;

	@Autowired
	LinkService linkService;

	@AfterEach
		// TODO: change to Adaptor (repository 말고!)
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
	@DisplayName("링크 저장 후 조회")
	void get_link() {
		// given
		Link link = Link.createLink("url", "title");
		LinkInfo saveLink = linkService.saveLink(link.getUrl());

		// when
		LinkInfo getLink = linkService.getLinkInfo(saveLink.url());

		// then
		assertThat(saveLink.url()).isEqualTo(getLink.url());
		assertThat(saveLink.title()).isEqualTo(getLink.title());
	}

	@Test
	@DisplayName("RSS 링크 리스트 조회")
	void rss_list_link() {
		// given
		Link link1 = Link
			.builder().url("url1").isRss(true).title("title1").imageUrl("imageUrl").publishedAt(LocalDateTime.now()).build();
		Link link2 = Link
			.builder().url("url2").isRss(true).title("title2").imageUrl("imageUrl").publishedAt(LocalDateTime.now()).build();
		Link link3 = Link
			.builder().url("url3").isRss(true).title("title3").imageUrl("imageUrl").publishedAt(LocalDateTime.now()).build();

		linkDataHandler.saveLink(link1);
		linkDataHandler.saveLink(link2);
		linkDataHandler.saveLink(link3);

		// when
		List<BlogLinkInfo> rssLinkList = linkService.getRssLinkList(10);

		// then
		assertThat(rssLinkList.size()).isEqualTo(3);
	}

	@Test
	@DisplayName("Link Mapper 테스트")
	void link_mapper() {
		// given
		linkMapper.of((LinkInfo) null);
		linkMapper.of((Link) null);

		LinkInfo nullLinkInfo = new LinkInfo("", null, null, null);
		linkMapper.of(nullLinkInfo);

		LinkInfo linkInfo = new LinkInfo("url", "title", "description", "imageUrl");
		linkMapper.of(linkInfo);

		Link link = Link
			.builder().url("url1").title("title1").imageUrl("imageUrl").publishedAt(LocalDateTime.now()).description("test").build();
		linkMapper.toLinkResult(link);
		linkMapper.toLinkResult(null);
		linkMapper.toBlogLinkInfo(null);
	}
}