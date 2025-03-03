package baguni.batch.domain.feed.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import baguni.BaguniBatchApplication;
import baguni.infra.infrastructure.rss.BlogRepository;
import baguni.infra.model.rss.Blog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = BaguniBatchApplication.class)
@ActiveProfiles("local")
@DisplayName("RSS 서비스 - 통합 테스트")
class FeedServiceTest {

	@Autowired
	private FeedService feedService;

	@Autowired
	private BlogRepository blogRepository;

	@BeforeEach
	void setUp() {
		// Rss
		blogRepository.save(Blog.create("네이버 플레이스", "https://medium.com/feed/naver-place-dev"));
		blogRepository.save(Blog.create("요기요", "https://techblog.yogiyo.co.kr/feed"));
		blogRepository.save(Blog.create("당근", "https://medium.com/feed/daangn"));
		blogRepository.save(Blog.create("중고나라", "https://teamblog.joonggonara.co.kr/feed"));
		blogRepository.save(Blog.create("한컴", "https://tech.hancom.com/feed/"));
		// Atom
		blogRepository.save(Blog.create("네이버", "https://d2.naver.com/d2.atom"));
		blogRepository.save(Blog.create("하이퍼커넥트", "https://hyperconnect.github.io/feed"));
		blogRepository.save(Blog.create("엔씨소프트", "https://ncsoft.github.io/ncresearch/feed"));
	}

	@AfterEach
	void clear() {
		blogRepository.deleteAll();
	}

	@Test
	@DisplayName("RSS 테스트")
	void rssFeed() {
		// given

		// when
		feedService.saveBlogArticleLinks();

		// then
	}
}