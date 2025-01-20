package baguni.common.event.events;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 	필드가 1개인 경우 cannot deserialize from Object value (no delegate- or property-based Creator) 발생
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinkCrawlingEvent extends Event {

	private String url;

	public LinkCrawlingEvent(String url) {
		this.url = url;
	}
}
