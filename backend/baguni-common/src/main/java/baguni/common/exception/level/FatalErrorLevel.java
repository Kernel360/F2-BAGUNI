package baguni.common.exception.level;

import lombok.extern.slf4j.Slf4j;
import baguni.common.util.CachedHttpServletRequest;

@Slf4j
public class FatalErrorLevel extends ErrorLevel {

	/**
	 * @deprecated 필터에서 로그를 남기고 삭제할 예정
	 */
	@Override
	public void logByLevel(Exception exception, CachedHttpServletRequest request) {
		log.error(
			"{}{}",
			exception.getMessage(), request,
			exception // stack trace 출력
		);
	}

	/**
	 * @deprecated 필터에서 로그를 남기고 삭제할 예정
	 */
	@Override
	public void logByLevel(Exception exception, String requestURI, String requestMethod) {
		log.error(
			"{} requestURI={} requestMethod={}",
			exception.getMessage(), requestURI, requestMethod,
			exception // stack trace 출력
		);
	}

	@Override
	public void logByLevel(Exception exception) {
		log.error(
			"{}",
			exception.getMessage(),
			exception // stack trace 출력
		);
	}
}
