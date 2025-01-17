package baguni.domain.exception.user;

import org.springframework.http.HttpStatus;

import baguni.common.exception.base.ApiErrorCode;
import baguni.common.exception.level.ErrorLevel;

public enum ApiUserErrorCode implements ApiErrorCode {

	/**
	 * User Error Code (U)
	 * */
	USER_NOT_FOUND
		("U-000", HttpStatus.BAD_REQUEST, "사용자 없음", ErrorLevel.CAN_HAPPEN()),

	USER_CREATE_FAILURE
		("U-001", HttpStatus.BAD_REQUEST, "사용자 생성 실패", ErrorLevel.MUST_NEVER_HAPPEN()),

	;

	// ------------------------------------------------------------
	// 하단 코드는 모든 ApiErrorCode 들에 반드시 포함되야 합니다.
	// 새로운 ErrorCode 구현시 복사 붙여넣기 해주세요.

	private final String code;

	private final HttpStatus httpStatus;

	private final String errorMessage;

	private final ErrorLevel logLevel;

	ApiUserErrorCode(String code, HttpStatus status, String message, ErrorLevel errorLevel) {
		this.code = code;
		this.httpStatus = status;
		this.errorMessage = message;
		this.logLevel = errorLevel;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public String getMessage() {
		return this.errorMessage;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return this.httpStatus;
	}

	@Override
	public ErrorLevel getErrorLevel() {
		return this.logLevel;
	}

	@Override
	public String toString() {
		return convertCodeToString(this);
	}
}
