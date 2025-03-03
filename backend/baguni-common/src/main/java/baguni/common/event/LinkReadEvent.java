package baguni.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

/**
 * DB의 맄그 가 조회(Read) 됨을 의미하는 메시지.
 */
@Getter
public class LinkReadEvent extends Event {

	private final String url;

	@JsonCreator
	public LinkReadEvent(String url) {
		super(new Topic("link.read"));
		this.url = url;
	}
}
