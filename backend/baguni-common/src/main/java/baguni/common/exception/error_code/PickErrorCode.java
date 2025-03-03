package baguni.common.exception.error_code;

import org.springframework.http.HttpStatus;

import baguni.common.exception.base.ErrorCode;
import baguni.common.exception.level.ErrorLevel;

public class PickErrorCode extends ErrorCode {

	/**
	 * Pick Error Code (PK)
	 */
	public static final ErrorCode PICK_NOT_FOUND = new PickErrorCode(
		"PK-000", HttpStatus.NOT_FOUND, "존재하지 않는 Pick", ErrorLevel.CAN_HAPPEN()
	);
	public static final ErrorCode PICK_ALREADY_EXIST = new PickErrorCode(
		"PK-001", HttpStatus.BAD_REQUEST, "이미 존재하는 Pick", ErrorLevel.CAN_HAPPEN()
	);
	public static final ErrorCode PICK_UNAUTHORIZED_USER_ACCESS = new PickErrorCode(
		"PK-002", HttpStatus.UNAUTHORIZED, "잘못된 Pick 접근, 다른 사용자의 Pick에 접근", ErrorLevel.SHOULD_NOT_HAPPEN()
	);
	public static final ErrorCode PICK_UNAUTHORIZED_ROOT_ACCESS = new PickErrorCode(
		"PK-003", HttpStatus.UNAUTHORIZED, "잘못된 Pick 접근, 폴더가 아닌 Root에 접근", ErrorLevel.SHOULD_NOT_HAPPEN()
	);
	public static final ErrorCode PICK_DELETE_NOT_ALLOWED = new PickErrorCode(
		"PK-004", HttpStatus.NOT_ACCEPTABLE, "휴지통이 아닌 폴더에서 픽 삭제는 허용되지 않음", ErrorLevel.SHOULD_NOT_HAPPEN()
	);

	protected PickErrorCode(String code, HttpStatus status, String message, ErrorLevel errorLevel) {
		super(code, status, message, errorLevel);
	}
}
