package baguni.infra.model.pick;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import baguni.infra.model.tag.Tag;

@Table(
	name = "pick_tag",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "UC_PICK_TAG",
			columnNames = {"pick_id", "tag_id"}
		)
	}
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PickTag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	// 사용자 FK
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pick_id", nullable = false)
	private Pick pick;

	// 사용자 정의 태그 FK
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tag_id", nullable = false)
	private Tag tag;

	// TODO: 엔티티 사용자가 정적 팩토리 메소드로 필요한 함수를 구현 하세요
	private PickTag(Pick pick, Tag tag) {
		this.pick = pick;
		this.tag = tag;
	}

	public static PickTag of(Pick pick, Tag tag) {
		return new PickTag(pick, tag);
	}
}
