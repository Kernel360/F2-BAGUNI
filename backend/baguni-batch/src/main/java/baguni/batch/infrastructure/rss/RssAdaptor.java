package baguni.batch.infrastructure.rss;

import java.time.LocalDate;
import java.util.List;

import baguni.batch.domain.rss.dto.RssBlogCommand;
import baguni.domain.model.rss.RssBlog;
import baguni.domain.model.rss.RssFeed;

public interface RssAdaptor {

	List<RssBlog> getAllRssBlog();

	RssBlog saveRssBlog(RssBlogCommand.Create command);

	// 특정 블로그 rssData 조회
	List<RssFeed> getAllRssFeedByRssBlog(RssBlog rssBlog);

	// 특정 날짜에 수집한 모든 rssData 조회
	List<RssFeed> getAllRssFeedByDate(LocalDate date);

	// 모든 rssData 조회
	List<RssFeed> getAllRssFeed();

	// DB에 없는 RssData 저장 - url 기준으로 중복 검사
	List<RssFeed> saveAllDistinctRssFeed(RssBlog rssBlog, List<RssFeed> feedList);
}
