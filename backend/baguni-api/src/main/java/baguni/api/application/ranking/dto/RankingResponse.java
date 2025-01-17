package baguni.api.application.ranking.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record RankingResponse(
	@Schema(description = "오늘 하루 동안 인기 있는 링크 Top 10")
	List<LinkInfoWithCount> dailyViewRanking,

	@Schema(description = "지난 7일동안 링크 조회 수 Top 10")
	List<LinkInfoWithCount> weeklyViewRanking,

	@Schema(description = "지난 30일동안 링크가 픽된 횟수 Top 10")
	List<LinkInfoWithCount> monthlyPickRanking
) {
}
