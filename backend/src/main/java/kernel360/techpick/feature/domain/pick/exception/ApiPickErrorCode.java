package kernel360.techpick.feature.domain.pick.exception;

import org.springframework.http.HttpStatus;

import kernel360.techpick.core.exception.base.ApiErrorCode;
import kernel360.techpick.core.exception.level.ErrorLevel;

public enum ApiPickErrorCode implements ApiErrorCode {

	/**
	 * Pick Error Code (PK)
	 */
	PICK_NOT_FOUND
		("PK-000", HttpStatus.NOT_FOUND, "존재하지 않는 Pick", ErrorLevel.CAN_HAPPEN()),
	PICK_ALREADY_EXIST
		("PK-001", HttpStatus.BAD_REQUEST, "이미 존재하는 Pick", ErrorLevel.CAN_HAPPEN()),
	PICK_UNAUTHORIZED_USER_ACCESS
		("PK-002", HttpStatus.UNAUTHORIZED, "잘못된 Pick 접근, 다른 사용자의 Pick에 접근", ErrorLevel.SHOULD_NOT_HAPPEN()),
	PICK_UNAUTHORIZED_ROOT_ACCESS
		("PK-003", HttpStatus.UNAUTHORIZED, "잘못된 Pick 접근, 폴더가 아닌 Root에 접근", ErrorLevel.SHOULD_NOT_HAPPEN()),
	PICK_DELETE_NOT_ALLOWED
		("PK-004", HttpStatus.NOT_ACCEPTABLE, "휴지통이 아닌 폴더에서 픽 삭제는 허용되지 않음.", ErrorLevel.SHOULD_NOT_HAPPEN()),
	;

	private final String code;

	private final HttpStatus httpStatus;

	private final String errorMessage;

	private final ErrorLevel logLevel;

	ApiPickErrorCode(String code, HttpStatus status, String message, ErrorLevel errorLevel) {
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
