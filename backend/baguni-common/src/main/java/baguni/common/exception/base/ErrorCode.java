package baguni.common.exception.base;

import org.springframework.http.HttpStatus;

import baguni.common.exception.level.ErrorLevel;
import lombok.Getter;

@Getter
public abstract class ErrorCode {

	/**
	 * 도메인 + 에러 번호 형식의 문자열
	 * 클라이언트가 코드 값에 따라 뷰를 처리합니다.
	 * Ex. 첫번째 폴더 에러 코드 = "FO-001"
	 *     네번째 유저 에러 코드 = "U-004"
	 */
	private final String code;

	/**
	 * 해당 에러 코드의 의미 (= 설명)
	 * 클라이언트 응답과 백엔드 로그에 표시됩니다.
	 */
	private final String explanation;

	/**
	 * 에러 코드가 클라이언트 응답으로 전달 될 경우
	 * 반환할 HTTP 응답 코드.
	 */
	private final HttpStatus httpStatus;

	/**
	 * 에러 코드의 위험도 수준.
	 * Fatal, Warning, Normal 레벨이 존재합니다.
	 */
	private final ErrorLevel errorLevel;

	protected ErrorCode(String code, HttpStatus httpStatus, String explanation, ErrorLevel errorLevel) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.explanation = explanation;
		this.errorLevel = errorLevel;
	}

	@Override
	public String toString() {
		return String.format("[ 에러 코드 %s : %s ]", this.code, this.explanation);
	}
}
