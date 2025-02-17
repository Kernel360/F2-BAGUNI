package baguni.common.exception.base;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import baguni.common.event.messenger.EventMessenger;
import baguni.common.util.ErrorLogEventBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import baguni.common.exception.level.ErrorLevel;
import baguni.common.util.RequestHolder;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalServiceExceptionHandler {

	private final RequestHolder requestHolder;
	private final ErrorLogEventBuilder errorLogEventBuilder;
	private final EventMessenger eventMessenger;

	/**
	 * ServiceException 으로 분류 되지 않는 예외는
	 * 모두 5xx 코드 오류 입니다.
	 */
	@ExceptionHandler(Exception.class)
	public ApiErrorResponse handleGlobalException(Exception exception, HttpServletRequest request) {
		// TODO: 얘가 신규 로그 수정 작업. 테스트가 완료되면, 아래쪽 슬랙 코드 + Request Holder 삭제
		ErrorLevel.MUST_NEVER_HAPPEN().logByLevel(exception, request.getRequestURI(), request.getMethod());

		// TODO: 기존 슬랙 알림 로그. 얘는 혹시 모를 작업을 위해 남긴다.
		var errLogMessage = errorLogEventBuilder.buildWithException(
			exception,
			requestHolder.getRequest(),
			HttpStatus.INTERNAL_SERVER_ERROR
		);
		eventMessenger.send(errLogMessage);

		return new ApiErrorResponse(
			"UNKNOWN",
			"미확인 서버 에러",
			HttpStatus.INTERNAL_SERVER_ERROR
		);
	}

	/**
	 * ServiceException 을 공통 Response 형태로 변환 합니다.
	 */
	@ExceptionHandler(ServiceException.class)
	public ApiErrorResponse handleApiException(ServiceException exception, HttpServletRequest request) {
		ErrorCode errorCode = exception.getErrorCode();

		// TODO: 기존 슬랙 알림 로그. 얘는 혹시 모를 작업을 위해 남긴다.
		if (exception.isFatal()) {
			var errLogMessage = errorLogEventBuilder.buildWithServiceException(
				exception,
				requestHolder.getRequest()
			);
			eventMessenger.send(errLogMessage);
		}

		// TODO: 얘가 신규 로그 수정 작업. 테스트가 완료되면, 위쪽 슬랙 코드 + Request Holder 삭제
		errorCode.getErrorLevel().logByLevel(exception, request.getRequestURI(), request.getMethod());

		return ApiErrorResponse.fromErrorCode(errorCode);
	}

	/**
	 * Validation 관련 예외
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ApiErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
		// TODO: 작업이 다 되면 request holder 제거
		ErrorLevel.SHOULD_NOT_HAPPEN().logByLevel(exception, requestHolder.getRequest());

		return new ApiErrorResponse(
			"VALIDATION ERROR",
			exception.getBindingResult().getFieldError().getDefaultMessage(),
			HttpStatus.BAD_REQUEST
		);
	}

	/**
	 * Json 파싱 과정 중 에러가 났을때 처리하는 handler
	 * 참고 : https://be-student.tistory.com/52
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ApiErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
		// TODO: 작업이 다 되면 request holder 제거
		ErrorLevel.SHOULD_NOT_HAPPEN().logByLevel(exception, requestHolder.getRequest());

		return new ApiErrorResponse(
			"INVALID JSON ERROR", "올바르지 않은 Json 형식입니다.", HttpStatus.BAD_REQUEST
		);
	}

	/**
	 * Request Parameter 가 있어야 하는데 없는 경우를 처리하는 handler
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ApiErrorResponse handleMissingServletRequestParameterException(
		MissingServletRequestParameterException exception) {
		// TODO: 작업이 다 되면 request holder 제거
		ErrorLevel.SHOULD_NOT_HAPPEN().logByLevel(exception, requestHolder.getRequest());
		return new ApiErrorResponse(
			"INVALID REQUEST PARAMETER",
			"올바르지 않은 Request Parameter 형식입니다.",
			HttpStatus.BAD_REQUEST);
	}
}
