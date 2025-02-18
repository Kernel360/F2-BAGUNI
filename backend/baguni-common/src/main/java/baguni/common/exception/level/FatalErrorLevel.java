package baguni.common.exception.level;

import lombok.extern.slf4j.Slf4j;
import baguni.common.util.CachedHttpServletRequest;

@Slf4j
public class FatalErrorLevel extends ErrorLevel {

	@Override
	public void logByLevel(Exception exception) {
		log.error(
			"{}",
			exception.getMessage(),
			exception // stack trace 출력
		);
	}
}
