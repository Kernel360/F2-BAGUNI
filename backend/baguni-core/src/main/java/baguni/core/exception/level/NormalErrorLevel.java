package baguni.core.exception.level;

import lombok.extern.slf4j.Slf4j;
import baguni.core.exception.base.ApiException;
import baguni.core.util.CachedHttpServletRequest;

@Slf4j
public class NormalErrorLevel extends ErrorLevel {

	@Override
	public void handleError(ApiException exception, CachedHttpServletRequest request) {
		log.info(exception.getMessage(), exception, request.toString());
	}

	@Override
	public void handleError(ApiException exception) {
		log.info(exception.getMessage(), exception);
	}
}