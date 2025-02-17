package baguni.common.exception.level;

import lombok.extern.slf4j.Slf4j;
import baguni.common.util.CachedHttpServletRequest;

@Slf4j
public class WarningErrorLevel extends ErrorLevel {

	@Override
	public void logByLevel(Exception exception, CachedHttpServletRequest request) {
		log.warn(
			"{}{}",
			exception.getMessage(), request
		);
	}

	@Override
	public void logByLevel(Exception exception, String requestURI, String requestMethod) {
		log.warn(
			"{} requestURI={} requestMethod={}",
			exception.getMessage(), requestURI, requestMethod
		);
	}

	@Override
	public void logByLevel(Exception exception) {
		log.warn(
			"{}",
			exception.getMessage()
		);
	}
}
