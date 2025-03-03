package baguni.infra.model.link;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import org.hibernate.annotations.ColumnDefault;

import baguni.common.exception.base.ServiceException;
import baguni.common.exception.error_code.LinkErrorCode;
import baguni.infra.model.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "link")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Link extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	// url로 검색이 자주 되므로 text가 아닌 varchar를 사용 + unique
	@Column(name = "url", nullable = false, columnDefinition = "VARCHAR(2048)", unique = true)
	private String url;

	// title이 한글 200자 이상인 경우가 있어 text타입으로 변경
	@Column(name = "title", columnDefinition = "TEXT")
	private String title;

	// description이 한글 300자 이상인 경우가 있어 text타입으로 변경
	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	// image가 base64 로 인코딩되서 url에 담기는 경우가 있기 때문에 text로 변경
	@Column(name = "image_url", columnDefinition = "TEXT")
	private String imageUrl;

	@Column(name = "published_at")
	private LocalDateTime publishedAt;

	@Column(name = "is_rss", nullable = false)
	@ColumnDefault("false")
	private Boolean isRss;

	// Static Factory Method -------------

	public static Link createLink(String url) {
		return Link
			.builder()
			.url(url)
			.title("")
			.description("")
			.isRss(false)
			.build();
	}

	public static Link createLink(String url, String title) {
		return Link
			.builder()
			.url(url)
			.title(title)
			.description("")
			.isRss(false)
			.build();
	}

	public static Link createRssLink(String url, String title, String pubDate) {
		LocalDateTime publishedAt = null;
		if (Objects.nonNull(pubDate)) {
			try {
				publishedAt = LocalDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME);
			} catch (DateTimeParseException e) {
				publishedAt = LocalDateTime.parse(pubDate, DateTimeFormatter.ISO_DATE_TIME);
			}
		}
		return Link
			.builder()
			.url(url)
			.title(title)
			.description("")
			.publishedAt(publishedAt)
			.isRss(true)
			.build();
	}

	public void updateMetadata(String title, String description, String imageUrl) {
		this.title = title;
		this.description = description;
		this.imageUrl = imageUrl;
	}

	// Private Builder -------------

	@Builder
	private Link(
		String url, String title, String description,
		String imageUrl, Boolean isRss, LocalDateTime publishedAt
	) {
		if (2048 < url.length()) {
			throw new ServiceException(LinkErrorCode.LINK_URL_TOO_LONG);
		}
		this.url = url;
		this.title = title;
		this.description = description;
		this.imageUrl = imageUrl;
		this.isRss = isRss;
		this.publishedAt = publishedAt;
	}
}
