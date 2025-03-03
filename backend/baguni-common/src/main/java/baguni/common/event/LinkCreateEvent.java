package baguni.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

/**
 * DB의 맄그 가 생성(Create) 됨을 의미하는 메시지.
 */
@Getter
public class LinkCreateEvent extends Event {

	private final String url;

	@JsonCreator
	public LinkCreateEvent(String url) {
		super(new Topic("link.create"));
		this.url = url;
	}
}
