package baguni.common.event;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import baguni.common.config.RabbitmqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventMessenger {

	private final RabbitTemplate rabbitTemplate;

	public void send(Event event) {
		try {
			rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE.NAME, event.getTopicString(), event);
		} catch (AmqpException e) {
			log.error(e.getMessage(), e);
		}
	}
}
