package baguni.common.exception.error_code;

import org.springframework.http.HttpStatus;

import baguni.common.exception.base.ErrorCode;
import baguni.common.exception.level.ErrorLevel;

public class UserErrorCode extends ErrorCode {

	/**
	 * User Error Code (U)
	 * */
	public static final ErrorCode USER_NOT_FOUND = new UserErrorCode(
		"U-000", HttpStatus.BAD_REQUEST, "사용자 없음", ErrorLevel.CAN_HAPPEN()
	);
	public static final ErrorCode USER_CREATE_FAILURE = new UserErrorCode(
		"U-001", HttpStatus.BAD_REQUEST, "사용자 생성 실패", ErrorLevel.MUST_NEVER_HAPPEN()
	);

	protected UserErrorCode(String code, HttpStatus status, String message, ErrorLevel errorLevel) {
		super(code, status, message, errorLevel);
	}
}
