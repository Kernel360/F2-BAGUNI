package baguni.infra.model.blog;

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
import baguni.infra.model.common.BaseEntity;

@Table(name = "blog")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Blog extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "blog_name", nullable = false, unique = true)
	private String blogName;

	// Rss 피드 주소
	@Column(name = "url", nullable = false, unique = true)
	private String url;

	// TODO: 엔티티 사용자가 정적 팩토리 메소드로 필요한 함수를 구현 하세요
	@Builder
	private Blog(String blogName, String url) {
		this.blogName = blogName;
		this.url = url;
	}

	public static Blog create(String blogName, String url) {
		return new Blog(blogName, url);
	}
}
