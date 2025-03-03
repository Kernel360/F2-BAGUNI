package baguni.domain.model.pick;

import java.util.ArrayList;
import java.util.List;

import baguni.domain.model.util.OrderConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import baguni.domain.model.common.BaseEntity;
import baguni.domain.model.folder.Folder;
import baguni.domain.model.link.Link;
import baguni.domain.model.user.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pick extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	// 사용자
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	// 북마크 대상 링크
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "link_id", nullable = false)
	private Link link;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_folder_id", nullable = false)
	private Folder parentFolder;

	// 사용자가 수정 가능한 Pick 제목. 기본값은 원문 제목과 동일
	@Column(name = "title", nullable = false)
	private String title = "";

	// 픽에 속한 tag id들을 공백으로 분리된 String으로 변환하여 db에 저장. Ex) [6,3,2,23,1] -> "6 3 2 23 1"
	@Convert(converter = OrderConverter.class)
	@Column(name = "tag_order", columnDefinition = "longblob", nullable = false)
	private List<Long> tagIdOrderedList = new ArrayList<>();

	@Builder
	private Pick(User user, Link link, Folder parentFolder, String title, List<Long> tagIdOrderedList) {
		this.user = user;
		this.link = link;
		this.parentFolder = parentFolder;
		this.title = title;
		this.tagIdOrderedList = tagIdOrderedList;
	}

	public Pick updateTagOrderList(List<Long> tagOrderList) {
		if (tagOrderList == null)
			return this;
		this.tagIdOrderedList = tagOrderList;
		return this;
	}

	public Pick updateParentFolder(Folder parentFolder) {
		if (parentFolder == null)
			return this;
		this.parentFolder = parentFolder;
		return this;
	}

	public Pick updateTitle(String title) {
		if (title == null)
			return this;
		this.title = title;
		return this;
	}
}
