package baguni.common.event.messenger;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import baguni.common.config.RabbitmqConfig;
import baguni.common.event.events.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventMessenger {

	private final RabbitTemplate rabbitTemplate;

	public void send(Event event) {
		try {
			rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE.NAME, event.getTopic(), event);
		} catch (AmqpException e) {
			log.error(e.getMessage(), e);
		}
	}
}
