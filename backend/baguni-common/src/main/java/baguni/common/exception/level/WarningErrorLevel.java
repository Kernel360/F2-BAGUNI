package baguni.common.exception.level;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WarningErrorLevel extends ErrorLevel {

	@Override
	public void logByLevel(Exception exception) {
		log.warn(
			"{}",
			exception.getMessage()
		);
	}
}
