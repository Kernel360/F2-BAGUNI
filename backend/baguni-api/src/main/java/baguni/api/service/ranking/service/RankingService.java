package baguni.api.service.ranking.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import baguni.api.service.ranking.dto.RankingResult;
import baguni.common.dto.UrlWithCount;
import baguni.common.lib.cache.CacheType;
import baguni.domain.infrastructure.link.LinkDataHandler;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingService {

	/** @deprecated 삭제 예정 */
	private final RankingApi rankingApi;

	private final LinkDataHandler linkDataHandler;

	/**
	 * @deprecated 랭킹 서버를 없앰에 따라 삭제 예정
	 */
	@WithSpan
	public RankingResult getUrlRanking(int limit) {
		var currentDay = LocalDate.now();
		var before1Day = currentDay.minusDays(1);
		var before7Days = currentDay.minusDays(7);
		var before30Days = currentDay.minusDays(30);

		var past7DaysViewRanking = // 일주일 전 ~ 어제
			rankingApi.getUrlRankingByViewCount(before7Days, before1Day, limit).getBody();

		/**
		 * @author sangwon
		 * 어제가 아닌 오늘로 변경한 이유는 가장 많이 저장한 픽을 즉시 확인하기 위함.
		 * 집계 테이블이 추가되고, 캐싱 처리한다면 오늘 데이터는 즉시 보여줄 필요가 없기 때문에 어제부터로 하는 것은 어떨지? - psh
		 */
		var past30DaysPickRanking = // 한달 전 ~ 오늘
			rankingApi.getUrlRankingByPickedCount(before30Days, currentDay, limit).getBody();

		return new RankingResult(past7DaysViewRanking, past30DaysPickRanking);
	}

	@WithSpan
	@Cacheable(cacheNames = CacheType.CACHE_NAME.WEEKLY_LINK_RANK)
	public List<UrlWithCount> getWeeklyViewRank() {
		var limit = 10;
		var today = LocalDate.now();
		return linkDataHandler
			.getViewRank(today.minusDays(7), today.minusDays(1), limit)
			.stream().map(r -> new UrlWithCount(r.getUrl(), r.getBookmarkedCount()))
			.toList();
	}

	@WithSpan
	@Cacheable(cacheNames = CacheType.CACHE_NAME.MONTHLY_PICK_RANK)
	public List<UrlWithCount> getMonthlyBookmarkedRank() {
		var limit = 10;
		var today = LocalDate.now();
		return linkDataHandler
			.getBookmarkedRank(today.minusDays(30), today, limit)
			.stream().map(r -> new UrlWithCount(r.getUrl(), r.getViewCount()))
			.toList();
	}
}
