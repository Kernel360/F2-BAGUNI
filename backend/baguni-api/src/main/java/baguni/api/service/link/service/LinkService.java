package baguni.api.service.link.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import baguni.common.lib.cache.CacheType;
import baguni.infra.infrastructure.link.dto.BlogLinkInfo;
import baguni.infra.model.link.Link;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import baguni.infra.infrastructure.link.dto.LinkInfo;
import baguni.infra.infrastructure.link.dto.LinkMapper;
import baguni.infra.infrastructure.link.LinkDataHandler;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

	private final LinkDataHandler linkDataHandler;
	private final LinkMapper linkMapper;

	@WithSpan
	@Transactional(readOnly = true)
	public LinkInfo getLinkInfo(String url) {
		Link link = linkDataHandler.getLink(url);
		return linkMapper.of(link);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<LinkInfo> getLinkInfoList(List<String> urlList) {
		return linkDataHandler.getLinkList(urlList).stream().map(linkMapper::of).toList();
	}

	@WithSpan
	@Transactional
	public LinkInfo saveLink(String url) {
		Link link = linkDataHandler.getOptionalLink(url).orElseGet(() -> Link.createLink(url));
		return linkMapper.of(linkDataHandler.saveLink(link));
	}

	@WithSpan
	@Transactional(readOnly = true)
	@Cacheable(cacheNames = CacheType.CACHE_NAME.DAILY_RSS_BLOG_ARTICLE)
	public List<BlogLinkInfo> getRssLinkList(int limit) {
		return linkDataHandler.getRssLinkList(limit)
							  .stream()
							  .map(linkMapper::toBlogLinkInfo)
							  .toList();
	}
}
