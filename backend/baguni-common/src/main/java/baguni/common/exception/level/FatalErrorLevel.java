package baguni.common.exception.level;

import lombok.extern.slf4j.Slf4j;
import baguni.common.util.CachedHttpServletRequest;

@Slf4j
public class FatalErrorLevel extends ErrorLevel {

	@Override
	public void logByLevel(Exception exception, CachedHttpServletRequest request) {
		log.error("{}{}", exception.getMessage(), request, exception); // stack trace 출력
	}

	@Override
	public void logByLevel(Exception exception, String requestURI, String requestMethod) {
		log.error(
			"message={} requestURI={} requestMethod={}",
			exception.getMessage(), requestURI, requestMethod,
			exception // stack trace 출력
		);
	}

	@Override
	public void logByLevel(Exception exception) {
		log.error(
			"message={}",
			exception.getMessage(),
			exception // stack trace 출력
		);
	}
}
