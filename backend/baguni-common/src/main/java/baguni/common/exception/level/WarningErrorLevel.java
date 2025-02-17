package baguni.common.exception.level;

import lombok.extern.slf4j.Slf4j;
import baguni.common.util.CachedHttpServletRequest;

@Slf4j
public class WarningErrorLevel extends ErrorLevel {

	@Override
	public void logByLevel(Exception exception, CachedHttpServletRequest request) {
		log.warn("{}{}", exception.getMessage(), request); // stack trace 미출력
	}

	@Override
	public void logByLevel(Exception exception, String requestURI, String requestMethod) {
		log.warn(
			"message={} requestURI={} requestMethod={}",
			exception.getMessage(), requestURI, requestMethod,
			exception // stack trace 출력
		);
	}

	@Override
	public void logByLevel(Exception exception) {
		log.warn(
			"message={}",
			exception.getMessage(),
			exception // stack trace 출력
		);
	}
}
