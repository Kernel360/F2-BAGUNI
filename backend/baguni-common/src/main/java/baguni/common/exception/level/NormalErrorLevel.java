package baguni.common.exception.level;

import lombok.extern.slf4j.Slf4j;
import baguni.common.util.CachedHttpServletRequest;

@Slf4j
public class NormalErrorLevel extends ErrorLevel {

	@Override
	public void logByLevel(Exception exception, CachedHttpServletRequest request) {
		log.info(
			"{}{}",
			exception.getMessage(), request
		);
	}

	@Override
	public void logByLevel(Exception exception, String requestURI, String requestMethod) {
		log.info(
			"{} requestURI={} requestMethod={}",
			exception.getMessage(), requestURI, requestMethod
		);
	}

	@Override
	public void logByLevel(Exception exception) {
		log.info(
			"{}",
			exception.getMessage()
		);
	}
}
