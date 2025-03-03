package baguni.common.event;

import lombok.Getter;

@Getter
public class ErrorLogEvent extends Event {

	private static final Topic topic = new Topic("log.error");

	private final String exceptionClass; // Exception 종류
	private final String exceptionMessage; // 예외 메세지
	private final String requestUri; // ex) /api/picks
	private final String requestMethod; // ex) GET, POST
	private final String requestTime; // 예외 발생 시간
	private final String requestAddress; // IP
	private final String profile; // local, dev, prod 구분
	private final int httpStatusCode; // 응답 상태 코드 ex) 500
	private final String httpStatusMessage; // 응답 상태 메세지 ex) INTERNAL SERVER ERROR

	public ErrorLogEvent(
		String exceptionClass, String exceptionMessage, String requestUri, String requestMethod,
		String requestAddress, String profile, int httpStatusCode, String httpStatusMessage
	) {
		super(topic);
		this.exceptionClass = exceptionClass;
		this.exceptionMessage = exceptionMessage;
		this.requestUri = requestUri;
		this.requestMethod = requestMethod;
		this.httpStatusCode = httpStatusCode;
		this.requestTime = super.getTimeFormatted();
		this.requestAddress = requestAddress;
		this.profile = profile;
		this.httpStatusMessage = httpStatusMessage;
	}
}
