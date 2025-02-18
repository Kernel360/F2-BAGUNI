package baguni.common.exception.base;

import baguni.common.exception.level.FatalErrorLevel;
import baguni.common.exception.level.NormalErrorLevel;
import baguni.common.exception.level.WarningErrorLevel;
import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

	private final ErrorCode errorCode;

	public ServiceException(ErrorCode errorCode) {
		super(errorCode.toString());
		this.errorCode = errorCode;
	}

	public ServiceException(ErrorCode errorCode, String additionalHint) {
		super(errorCode.toString() + ": " + additionalHint);
		this.errorCode = errorCode;
	}

	public void logByLevel() {
		this.errorCode.getErrorLevel().logByLevel(this);
	}

	public final boolean isFatal() {
		return (this.errorCode.getErrorLevel() instanceof FatalErrorLevel);
	}

	public final boolean isWarning() {
		return (this.errorCode.getErrorLevel() instanceof WarningErrorLevel);
	}

	public final boolean isNormal() {
		return (this.errorCode.getErrorLevel() instanceof NormalErrorLevel);
	}
}
