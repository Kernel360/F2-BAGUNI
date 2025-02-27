package baguni.domain.infrastructure.link;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import baguni.domain.model.link.LinkStats;

public interface LinkStatsRepository extends JpaRepository<LinkStats, Long> {

	Optional<LinkStats> findByDateAndUrl(LocalDate date, String url);

	List<LinkStats> findByDateBetweenOrderByViewCountDesc(LocalDate start, LocalDate end, Limit limit);

	List<LinkStats> findByDateBetweenOrderByBookmarkedCountDesc(LocalDate start, LocalDate end, Limit limit);
}
