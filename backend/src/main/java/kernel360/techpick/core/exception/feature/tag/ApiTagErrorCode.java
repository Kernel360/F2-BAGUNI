package kernel360.techpick.core.exception.feature.tag;

import org.springframework.http.HttpStatus;

import kernel360.techpick.core.exception.base.ApiErrorCode;

public enum ApiTagErrorCode implements ApiErrorCode {

	/**
	 * Tag Error Code (TG)
	 */
	TAG_NOT_FOUND("TG-000", HttpStatus.BAD_REQUEST, "존재하지 않는 태그"),
	TAG_ALREADY_EXIST("TG-001", HttpStatus.BAD_REQUEST, "이미 존재하는 태그"),
	TAG_INVALID_NAME("TG-002", HttpStatus.BAD_REQUEST, "유효하지 않은 태그 이름"),
	UNAUTHORIZED_TAG_ACCESS("TG-003", HttpStatus.UNAUTHORIZED, "잘못된 태그 접근"),
	TAG_HAS_PICK("TG-004", HttpStatus.BAD_REQUEST, "픽을 가지고 있는 태그");

	// ------------------------------------------------------------
	// 하단 코드는 모든 ApiErrorCode 들에 반드시 포함되야 합니다.
	// 새로운 ErrorCode 구현시 복사 붙여넣기 해주세요.

	private final String code;

	private final HttpStatus httpStatus;

	private final String errorMessage;

	ApiTagErrorCode(String code, HttpStatus status, String message) {
		this.code = code;
		this.httpStatus = status;
		this.errorMessage = message;
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
	public String toString() {
		return convertCodeToString(this);
	}
}
