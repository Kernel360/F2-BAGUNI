package baguni.common.exception.error_code;

import org.springframework.http.HttpStatus;

import baguni.common.exception.base.ErrorCode;
import baguni.common.exception.level.ErrorLevel;

public class TagErrorCode extends ErrorCode {

	/**
	 * Tag Error Code (TG)
	 */
	public static final ErrorCode TAG_NOT_FOUND = new TagErrorCode(
		"TG-000", HttpStatus.BAD_REQUEST, "존재하지 않는 태그", ErrorLevel.CAN_HAPPEN()
	);
	public static final ErrorCode TAG_ALREADY_EXIST = new TagErrorCode(
		"TG-001", HttpStatus.BAD_REQUEST, "이미 존재하는 태그", ErrorLevel.CAN_HAPPEN()
	);
	public static final ErrorCode TAG_INVALID_NAME = new TagErrorCode(
		"TG-002", HttpStatus.BAD_REQUEST, "유효하지 않은 태그 이름", ErrorLevel.CAN_HAPPEN()
	);
	public static final ErrorCode UNAUTHORIZED_TAG_ACCESS = new TagErrorCode(
		"TG-003", HttpStatus.UNAUTHORIZED, "잘못된 태그 접근", ErrorLevel.SHOULD_NOT_HAPPEN()
	);
	public static final ErrorCode TAG_INVALID_ORDER = new TagErrorCode(
		"TG-004", HttpStatus.BAD_REQUEST, "유효하지 않은 태그 순서", ErrorLevel.SHOULD_NOT_HAPPEN()
	);
	public static final ErrorCode TAG_NAME_TOO_LONG = new TagErrorCode(
		"TG-005", HttpStatus.BAD_REQUEST, "태그 이름이 허용된 최대 길이를 초과했습니다", ErrorLevel.CAN_HAPPEN()
	);

	protected TagErrorCode(String code, HttpStatus status, String message, ErrorLevel errorLevel) {
		super(code, status, message, errorLevel);
	}
}
