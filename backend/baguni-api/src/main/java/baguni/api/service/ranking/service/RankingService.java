package baguni.api.service.ranking.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import baguni.common.dto.UrlWithCount;
import baguni.common.lib.cache.CacheType;
import baguni.domain.infrastructure.link.LinkDataHandler;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingService {

	private final LinkDataHandler linkDataHandler;

	@WithSpan
	@Cacheable(cacheNames = CacheType.CACHE_NAME.WEEKLY_LINK_RANK)
	public List<UrlWithCount> getWeeklyViewRank(int limit) {
		var today = LocalDate.now();
		return linkDataHandler
			.getViewRank(today.minusDays(7), today.minusDays(1), limit)
			.stream().map(r -> new UrlWithCount(r.getUrl(), r.getBookmarkedCount()))
			.toList();
	}

	@WithSpan
	@Cacheable(cacheNames = CacheType.CACHE_NAME.MONTHLY_PICK_RANK)
	public List<UrlWithCount> getMonthlyBookmarkedRank(int limit) {
		var today = LocalDate.now();
		return linkDataHandler
			.getBookmarkedRank(today.minusDays(30), today, limit)
			.stream().map(r -> new UrlWithCount(r.getUrl(), r.getViewCount()))
			.toList();
	}
}
