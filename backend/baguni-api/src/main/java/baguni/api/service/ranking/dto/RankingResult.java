package baguni.api.service.ranking.dto;

import java.util.List;

import baguni.common.dto.UrlWithCount;

public record RankingResult(
	List<UrlWithCount> weeklyUrlViewRank,
	List<UrlWithCount> monthlyUrlBookmarkedRank
) {
}