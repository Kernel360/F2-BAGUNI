package baguni.common.exception.error_code;

import org.springframework.http.HttpStatus;

import baguni.common.exception.base.ErrorCode;
import baguni.common.exception.level.ErrorLevel;

public class SharedFolderErrorCode extends ErrorCode {

	/**
	 * Pick Error Code (PK)
	 */
	public static final ErrorCode SHARED_FOLDER_NOT_FOUND = new SharedFolderErrorCode(
		"SF-000", HttpStatus.NOT_FOUND, "존재하지 않는 SharedFolder", ErrorLevel.CAN_HAPPEN()
	);
	public static final ErrorCode SHARED_FOLDER_UNAUTHORIZED = new SharedFolderErrorCode(
		"SF-001", HttpStatus.UNAUTHORIZED, "SharedFolder 접근 권한 없음", ErrorLevel.SHOULD_NOT_HAPPEN()
	);
	public static final ErrorCode FOLDER_CANT_BE_SHARED = new SharedFolderErrorCode(
		"SF-002", HttpStatus.UNAUTHORIZED, "해당 폴더는 공유될 수 없는 폴더입니다!", ErrorLevel.MUST_NEVER_HAPPEN()
	);
	public static final ErrorCode FOLDER_ALREADY_SHARED = new SharedFolderErrorCode(
		"SF-003", HttpStatus.CONFLICT, "이미 공유된 폴더는 다시 공유 상태가 될 수 없습니다.", ErrorLevel.SHOULD_NOT_HAPPEN()
	);

	protected SharedFolderErrorCode(String code, HttpStatus status, String message, ErrorLevel errorLevel) {
		super(code, status, message, errorLevel);
	}
}
