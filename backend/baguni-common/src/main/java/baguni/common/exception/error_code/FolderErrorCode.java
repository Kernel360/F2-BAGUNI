package baguni.common.exception.error_code;

import org.springframework.http.HttpStatus;

import baguni.common.exception.base.ErrorCode;
import baguni.common.exception.level.ErrorLevel;

public class FolderErrorCode extends ErrorCode {

	/**
	 * Folder Error Code (FO)
	 */
	public static final ErrorCode FOLDER_NOT_FOUND = new FolderErrorCode(
		"FO-000", HttpStatus.BAD_REQUEST, "존재하지 않는 폴더", ErrorLevel.SHOULD_NOT_HAPPEN()
	);
	public static final ErrorCode FOLDER_ALREADY_EXIST = new FolderErrorCode(
		"FO-001", HttpStatus.BAD_REQUEST, "이미 존재하는 폴더 이름", ErrorLevel.CAN_HAPPEN()
	);
	public static final ErrorCode FOLDER_ACCESS_DENIED = new FolderErrorCode(
		"FO-002", HttpStatus.FORBIDDEN, "접근할 수 없는 폴더", ErrorLevel.SHOULD_NOT_HAPPEN()
	);
	public static final ErrorCode BASIC_FOLDER_CANNOT_CHANGED = new FolderErrorCode(
		"FO-003", HttpStatus.BAD_REQUEST, "기본폴더는 변경(수정/삭제/이동)할 수 없음", ErrorLevel.MUST_NEVER_HAPPEN()
	);
	public static final ErrorCode CANNOT_DELETE_FOLDER_NOT_IN_RECYCLE_BIN = new FolderErrorCode(
		"FO-004", HttpStatus.BAD_REQUEST, "휴지통 안에 있는 폴더만 삭제할 수 있음", ErrorLevel.MUST_NEVER_HAPPEN()
	);
	public static final ErrorCode INVALID_FOLDER_TYPE = new FolderErrorCode(
		"FO-005", HttpStatus.NOT_IMPLEMENTED, "미구현 폴더 타입에 대한 서비스 요청", ErrorLevel.MUST_NEVER_HAPPEN()
	);
	public static final ErrorCode BASIC_FOLDER_ALREADY_EXISTS = new FolderErrorCode(
		"FO-006", HttpStatus.NOT_ACCEPTABLE, "기본 폴더는 1개만 존재할 수 있음", ErrorLevel.MUST_NEVER_HAPPEN()
	);
	public static final ErrorCode INVALID_TARGET = new FolderErrorCode(
		"FO-007", HttpStatus.NOT_ACCEPTABLE, "휴지통 또는 미분류 폴더에 폴더를 (생성/이동)할 수 없음", ErrorLevel.SHOULD_NOT_HAPPEN()
	);
	public static final ErrorCode INVALID_PARENT_FOLDER = new FolderErrorCode(
		"FO-008", HttpStatus.NOT_ACCEPTABLE, "부모 폴더가 올바르지 않음", ErrorLevel.SHOULD_NOT_HAPPEN()
	);
	// TODO: folder depth 추가 시 예외 삭제 예정
	public static final ErrorCode ROOT_FOLDER_SEARCH_NOT_ALLOWED = new FolderErrorCode(
		"FO-009", HttpStatus.NOT_ACCEPTABLE, "루트 폴더에 대한 검색은 허용되지 않음.", ErrorLevel.SHOULD_NOT_HAPPEN()
	);

	protected FolderErrorCode(String code, HttpStatus status, String message, ErrorLevel errorLevel) {
		super(code, status, message, errorLevel);
	}
}
