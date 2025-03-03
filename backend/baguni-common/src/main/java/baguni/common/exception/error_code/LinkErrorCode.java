package baguni.common.exception.error_code;

import org.springframework.http.HttpStatus;

import baguni.common.exception.base.ErrorCode;
import baguni.common.exception.level.ErrorLevel;

public class LinkErrorCode extends ErrorCode {

	/**
	 * Link Error Code (LI)
	 */
	public static final ErrorCode LINK_NOT_FOUND = new LinkErrorCode(
		"LI-000", HttpStatus.NOT_FOUND, "존재하지 않는 링크", ErrorLevel.SHOULD_NOT_HAPPEN()
	);
	public static final ErrorCode LINK_HAS_PICKS = new LinkErrorCode(
		"LI-001", HttpStatus.BAD_REQUEST, "링크를 픽한 사람이 존재", ErrorLevel.SHOULD_NOT_HAPPEN()
	);
	public static final ErrorCode LINK_ALREADY_EXIST = new LinkErrorCode(
		"LI-002", HttpStatus.BAD_REQUEST, "이미 존재하는 링크(URL)", ErrorLevel.CAN_HAPPEN()
	);
	public static final ErrorCode LINK_OG_TAG_UPDATE_FAILURE = new LinkErrorCode(
		"LI-003", HttpStatus.NOT_FOUND, "OG 태그 업데이트를 위한 크롤링 요청 실패", ErrorLevel.CAN_HAPPEN()
	);
	public static final ErrorCode LINK_URL_TOO_LONG = new LinkErrorCode(
		"LI-004", HttpStatus.URI_TOO_LONG, "저장 가능한 URL 길이 초과 [< 2048]", ErrorLevel.CAN_HAPPEN()
	);
	public static final ErrorCode LINK_ANALYZE_FAILURE = new LinkErrorCode(
		"LI-005", HttpStatus.URI_TOO_LONG, "링크 분석에 실패했습니다", ErrorLevel.CAN_HAPPEN()
	);

	protected LinkErrorCode(String code, HttpStatus status, String message, ErrorLevel errorLevel) {
		super(code, status, message, errorLevel);
	}
}
