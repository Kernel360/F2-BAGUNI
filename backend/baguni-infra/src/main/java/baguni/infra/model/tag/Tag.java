package baguni.infra.model.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import baguni.infra.model.user.User;

@Table(
	name = "tag",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "UC_TAG_NAME_PER_USER",
			columnNames = {"user_id", "name"}
		)
	}
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	// 태그명
	@Column(name = "name", nullable = false)
	private String name;

	// 프론트가 쓸 컬러 넘버 (숫자 - 색상 매핑은 프론트에서 처리, 무조건 처리)
	@Column(name = "color_number", nullable = false)
	private Integer colorNumber;

	// 사용자 FK
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Builder
	private Tag(String name, Integer colorNumber, User user) {
		this.name = name;
		this.colorNumber = colorNumber;
		this.user = user;
	}

	// TODO: 엔티티 사용자가 정적 팩토리 메소드로 필요한 함수를 구현 하세요

	public void updateTagName(String name) {
		if (name != null) {
			this.name = name;
		}
	}

	public void updateColorNumber(Integer colorNumber) {
		if (colorNumber != null) {
			this.colorNumber = colorNumber;
		}
	}
}
