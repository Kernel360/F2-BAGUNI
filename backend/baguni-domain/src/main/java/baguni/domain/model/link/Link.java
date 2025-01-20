package baguni.domain.model.link;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

import baguni.domain.exception.link.ApiLinkException;
import baguni.domain.model.common.BaseEntity;
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

	@Column(name = "invalidated_at")
	private LocalDateTime invalidatedAt;

	public static Link createLinkByUrl(String url) {
		return new Link(url, "", "", "", null);
	}

	/**
	 * 익스텐션으로 픽 생성 시 항상 title을 받기 때문에 생성 메서드 추가
	 */
	public static Link createLinkByUrlAndTitle(String url, String title) {
		return new Link(url, title, "", "", null);
	}

	// 업데이트 조건은 서비스 로직에서 명시
	public void updateMetadata(String title, String description, String imageUrl) {
		this.title = title;
		this.description = description;
		this.imageUrl = imageUrl;
	}

	public Link markAsInvalid() {
		this.invalidatedAt = LocalDateTime.now();
		return this;
	}

	public boolean isValidLink() {
		return (this.invalidatedAt == null);
	}

	@Builder
	private Link(String url, String title, String description, String imageUrl, LocalDateTime invalidatedAt) {
		if (2048 < url.length()) {
			throw ApiLinkException.LINK_URL_TOO_LONG();
		}
		this.url = url;
		this.title = title;
		this.description = description;
		this.imageUrl = imageUrl;
		this.invalidatedAt = invalidatedAt;
	}
}
