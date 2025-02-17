package baguni.common.exception.level;

import lombok.extern.slf4j.Slf4j;
import baguni.common.util.CachedHttpServletRequest;

@Slf4j
public class NormalErrorLevel extends ErrorLevel {

	@Override
	public void logByLevel(Exception exception, CachedHttpServletRequest request) {
		// TODO: 에러 알림 작업 끝나면 복구
		// log.info("{}{}", exception.getMessage(), request); // stack trace 미출력
		log.error("{}{}", exception.getMessage(), request, exception); // stack trace 출력
	}

	@Override
	public void logByLevel(Exception exception) {
		// TODO: 에러 알림 작업 끝나면 복구
		// log.error(exception.getMessage(), exception);
		log.info(exception.getMessage(), exception);
	}
}
