package baguni.common.exception.level;

import lombok.extern.slf4j.Slf4j;
import baguni.common.util.CachedHttpServletRequest;

@Slf4j
public class WarningErrorLevel extends ErrorLevel {

	/**
	 * @deprecated 필터에서 로그를 남기고 삭제할 예정
	 */
	@Override
	public void logByLevel(Exception exception, CachedHttpServletRequest request) {
		log.warn(
			"{}{}",
			exception.getMessage(), request
		);
	}

	/**
	 * @deprecated 필터에서 로그를 남기고 삭제할 예정 (슬랙 알림 대시보드 클릭시 바로 묶어서 볼 수 있게 변경)
	 */
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
