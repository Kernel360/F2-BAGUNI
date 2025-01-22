package baguni.api.service.link.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import baguni.common.annotation.MeasureTime;
import baguni.common.lib.cache.CacheType;
import baguni.domain.infrastructure.link.dto.RssLinkInfo;
import baguni.domain.model.link.Link;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import baguni.domain.infrastructure.link.dto.LinkInfo;
import baguni.domain.infrastructure.link.dto.LinkMapper;
import baguni.domain.infrastructure.link.LinkDataHandler;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

	private final LinkDataHandler linkDataHandler;
	private final LinkMapper linkMapper;

	@Transactional(readOnly = true)
	public LinkInfo getLinkInfo(String url) {
		Link link = linkDataHandler.getLink(url);
		return linkMapper.of(link);
	}

	@Transactional
	public LinkInfo saveLink(String url) {
		Link link = linkDataHandler.getOptionalLink(url).orElseGet(() -> Link.createLink(url));
		return linkMapper.of(linkDataHandler.saveLink(link));
	}

	@MeasureTime
	@Transactional(readOnly = true)
	@Cacheable(cacheNames = CacheType.CACHE_NAME.DAILY_RSS_BLOG_ARTICLE)
	public List<RssLinkInfo> getRssLinkList(int limit) {
		return linkDataHandler.getRssLinkList(limit)
							  .stream()
							  .map(linkMapper::toRssLinkInfo)
							  .toList();
	}
}
