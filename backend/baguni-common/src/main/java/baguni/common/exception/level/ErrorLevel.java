package baguni.common.exception.level;

import lombok.extern.slf4j.Slf4j;
import baguni.common.util.CachedHttpServletRequest;

@Slf4j
public abstract class ErrorLevel {

	/**
	 * (1) 정상 예외 이며, 따로, 대응 하지 않아도 되는 예외.
	 * - 서버가 잘못된 사용자 요청을 잘 처리한 경우
	 */
	public static ErrorLevel CAN_HAPPEN() {
		return new NormalErrorLevel();
	}

	/**
	 * (2) 대응 하지 않아도 되지만, 예의 주시 해야 하는 예외.
	 * - 정상 운영은 되지만, 애초에 그 요청이 발생 해선 안되는 경우. Ex. 프론트 엔드 버그
	 */
	public static ErrorLevel SHOULD_NOT_HAPPEN() {
		return new WarningErrorLevel();
	}

	/**
	 * (3) 즉시 대응이 필요한 예외
	 * - 운영환경 에서 절대 발생해선 안되며, 발생 시 서버를 즉시 종료해야 하는 경우.
	 */
	public static ErrorLevel MUST_NEVER_HAPPEN() {
		return new FatalErrorLevel();
	}

	/**
	 * @deprecated 필터에서 로그를 남기고 삭제할 예정
	 */
	public abstract void logByLevel(Exception exception, CachedHttpServletRequest request);

	/**
	 * @deprecated 필터에서 로그를 남기고 삭제할 예정 (슬랙 알림 대시보드 클릭시 바로 묶어서 볼 수 있게 변경)
	 */
	public abstract void logByLevel(Exception exception, String requestURI, String requestMethod);

	public abstract void logByLevel(Exception exception);
}
