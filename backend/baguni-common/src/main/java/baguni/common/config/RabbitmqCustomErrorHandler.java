package baguni.common.config;

import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

import baguni.common.exception.base.ServiceException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RabbitmqCustomErrorHandler extends ConditionalRejectingErrorHandler {

	@Override
	public void handleError(Throwable t) {
		if ((t.getCause() instanceof ServiceException exception)) {
			exception.logByLevel();
		}
		super.handleError(t);
	}
}
