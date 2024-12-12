package baguni.batch.infrastructure.rss;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import baguni.batch.domain.rss.dto.RssBlogCommand;
import baguni.batch.domain.rss.dto.RssMapper;
import baguni.core.model.rss.RssBlog;
import baguni.core.model.rss.RssBlogRepository;
import baguni.core.model.rss.RssFeed;
import baguni.core.model.rss.RssFeedRepository;

@Component
@RequiredArgsConstructor
public class RssAdaptorImpl implements RssAdaptor {

	private final RssBlogRepository rssBlogRepository;
	private final RssFeedRepository rssFeedRepository;
	private final RssMapper rssMapper;

	@Override
	@Transactional(readOnly = true)
	public List<RssBlog> getAllRssBlog() {
		return rssBlogRepository.findAll();
	}

	@Override
	@Transactional
	public RssBlog saveRssBlog(RssBlogCommand.Create command) {
		return rssBlogRepository.save(rssMapper.toRssBlog(command));
	}

	@Override
	@Transactional(readOnly = true)
	public List<RssFeed> getAllRssFeedByRssBlog(RssBlog rssBlog) {
		return rssFeedRepository.findByRssBlogId(rssBlog.getId());
	}

	@Override
	@Transactional(readOnly = true)
	public List<RssFeed> getAllRssFeedByDate(LocalDate date) {
		LocalDateTime from = date.atStartOfDay();
		LocalDateTime to = from.plusDays(1);
		return rssFeedRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, to);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RssFeed> getAllRssFeed() {
		return rssFeedRepository.findAll();
	}

	@Override
	@Transactional
	public List<RssFeed> saveAllDistinctRssFeed(RssBlog rssBlog, List<RssFeed> feedList) {
		Set<String> urlSet = new HashSet<>(rssFeedRepository.findAllUrlByBlogId(rssBlog.getId()));
		feedList.removeIf(feed -> urlSet.contains(feed.getUrl()));
		return rssFeedRepository.saveAll(feedList);
	}
}
