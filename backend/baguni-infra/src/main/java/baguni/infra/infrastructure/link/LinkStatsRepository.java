package baguni.infra.infrastructure.link;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import baguni.infra.model.link.LinkStats;

public interface LinkStatsRepository extends JpaRepository<LinkStats, Long> {

	Optional<LinkStats> findByDateAndUrl(LocalDate date, String url);

	List<LinkStats> findByDateBetweenAndViewCountGreaterThanOrderByViewCountDesc(
		LocalDate start, LocalDate end,
		int minCount, // 최소 기준 조회 수
		Limit limit
	);

	List<LinkStats> findByDateBetweenAndBookmarkedCountGreaterThanOrderByBookmarkedCountDesc(
		LocalDate start, LocalDate end,
		int minCount, // 최소 기준 북마크 횟수
		Limit limit
	);
}
