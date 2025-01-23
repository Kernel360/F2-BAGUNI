package baguni.common.event.events;

import lombok.Getter;

/**
 * 	필드가 1개인 경우 cannot deserialize from Object value (no delegate- or property-based Creator) 발생
 */
@Getter
public class PickCreateEvent extends Event {

	private static final String TOPIC = "pick.create";
	private final String url;

	public PickCreateEvent(String url) {
		super(TOPIC);
		this.url = url;
	}
}
