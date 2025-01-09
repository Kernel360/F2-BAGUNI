package baguni.common.event.events;

import java.time.LocalDateTime;

import lombok.Getter;

/**
 * @author minkyeu kim
 * 메시지큐에 보내는 기본 이벤트 형식입니다.
 */
@Getter
public abstract class Event {

	/** 이벤트가 발생한 시각 */
	private final LocalDateTime time = LocalDateTime.now();
}
