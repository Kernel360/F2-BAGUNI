package baguni.domain.model.link;

import java.time.LocalDate;

import org.hibernate.annotations.ColumnDefault;

import baguni.common.exception.base.ServiceException;
import baguni.domain.exception.link.LinkErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
	name = "link_stats",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "UC_DATE_URL",
			columnNames = {"date", "url"} // index(date, url)
		)
	}
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinkStats {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "date", updatable = false, nullable = false)
	private LocalDate date; // 오직 날짜만 저장

	@Column(name = "url", updatable = false, nullable = false, columnDefinition = "VARCHAR(2048)")
	private String url;

	@Column(name = "view_count", nullable = false)
	@ColumnDefault("0")
	private Long viewCount; // 조회수

	@Column(name = "bookmarked_count", nullable = false)
	@ColumnDefault("0")
	private Long bookmarkedCount; // 북마크 횟수

	// Static Factory Method -------------

	public LinkStats(LocalDate date, String url) {
		if (2048 < url.length()) {
			throw new ServiceException(LinkErrorCode.LINK_URL_TOO_LONG);
		}
		this.date = date;
		this.url = url;
		this.viewCount = 0L;
		this.bookmarkedCount = 0L;
	}

	public void incrementViewCount() {
		this.viewCount++;
	}

	public void incrementBookmarkedCount() {
		this.bookmarkedCount++;
	}
}
