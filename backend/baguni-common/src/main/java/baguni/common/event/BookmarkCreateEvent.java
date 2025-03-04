package baguni.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

/**
 * DB의 북마크(픽) 가 생성됨을 의미하는 메시지.
 */
@Getter
public class BookmarkCreateEvent extends Event {

	private final String url;

	@JsonCreator
	public BookmarkCreateEvent(String url) {
		super(new Topic("bookmark.create"));
		this.url = url;
	}
}
