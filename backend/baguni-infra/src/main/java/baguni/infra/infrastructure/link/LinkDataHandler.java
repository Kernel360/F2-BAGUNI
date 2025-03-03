package baguni.infra.infrastructure.link;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import baguni.common.exception.base.ServiceException;
import baguni.common.exception.error_code.LinkErrorCode;
import baguni.infra.model.link.Link;
import baguni.infra.model.link.LinkStats;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LinkDataHandler {
	private final LinkRepository linkRepository;
	private final LinkStatsRepository linkStatsRepository;

	@WithSpan
	@Transactional(readOnly = true)
	public Link getLink(String url) {
		return linkRepository.findByUrl(url)
							 .orElseThrow(() -> new ServiceException(LinkErrorCode.LINK_NOT_FOUND));
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<Link> getLinkList(List<String> urlList) {
		return linkRepository.findByUrlIn(urlList);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public Optional<Link> getOptionalLink(String url) {
		return linkRepository.findByUrl(url);
	}

	@WithSpan
	@Transactional
	public Link saveLink(Link link) {
		return linkRepository.save(link);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public boolean existsByUrl(String url) {
		return linkRepository.existsByUrl(url);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<Link> getRssLinkList(int limit) {
		return linkRepository.findAllRssBlogArticlesOrderByPublishedDate(
			PageRequest.of(0, limit)
		);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public Optional<LinkStats> findLinkStats(LocalDate date, String url) {
		return linkStatsRepository.findByDateAndUrl(date, url);
	}

	@WithSpan
	@Transactional
	public LinkStats saveLinkStats(LinkStats linkStats) {
		return linkStatsRepository.save(linkStats);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<LinkStats> getViewRank(LocalDate startDate, LocalDate endDate, Integer limit) {
		return linkStatsRepository.findByDateBetweenAndViewCountGreaterThanOrderByViewCountDesc(
			startDate, endDate, 0, Limit.of(limit)
		);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<LinkStats> getBookmarkedRank(LocalDate startDate, LocalDate endDate, Integer limit) {
		return linkStatsRepository.findByDateBetweenAndBookmarkedCountGreaterThanOrderByBookmarkedCountDesc(
			startDate, endDate, 0, Limit.of(limit)
		);
	}
}
