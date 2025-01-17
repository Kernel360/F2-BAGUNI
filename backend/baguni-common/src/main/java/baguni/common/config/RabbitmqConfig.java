package baguni.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

	public static final class EXCHANGE {
		public static final String EVENT = "exchange.event";
	}

	public static final class QUEUE {
		public static final String PICK_RANKING = "queue.pick-ranking";
		public static final String PICK_CRAWLING = "queue.pick-crawling";
		public static final String SLACK_NOTIFICATION = "queue.slack-notification";
	}

	@Value("${spring.application.name}")
	private String appName;

	@Value("${spring.rabbitmq.url}")
	private String url;

	@Value("${spring.rabbitmq.username}")
	private String username;

	@Value("${spring.rabbitmq.password}")
	private String password;

	/**
	 * 1. Exchange 구성 */
	@Bean
	DirectExchange directExchange() {
		return new DirectExchange(EXCHANGE.EVENT);
	}

	/**
	 * 2. 큐 구성 */
	@Bean
	Queue pickRanking() {
		return new Queue(QUEUE.PICK_RANKING, false);
	}

	@Bean
	Queue pickCrawling() {
		return new Queue(QUEUE.PICK_CRAWLING, false);
	}

	@Bean
	Queue slackNotification() {
		return new Queue(QUEUE.SLACK_NOTIFICATION, false);
	}


	/**
	 * 3. 큐와 DirectExchange를 바인딩 */
	@Bean
	Binding rankingDirectBinding(DirectExchange directExchange, Queue pickRanking) {
		return BindingBuilder
			.bind(pickRanking)
			.to(directExchange)
			.with("ranking"); // directExchange 에 ranking 라우팅 키를 가진 메시지가 있으면 pickRanking 큐에 메시지 전달
	}

	@Bean
	Binding crawlingDirectBinding(DirectExchange directExchange, Queue pickCrawling) {
		return BindingBuilder
			.bind(pickCrawling)
			.to(directExchange)
			.with("crawling");
	}

	@Bean
	Binding slackNotificationBinding(DirectExchange directExchange, Queue slackNotification) {
		return BindingBuilder
			.bind(slackNotification)
			.to(directExchange)
			.with("slack");
	}

	/**
	 * 4. RabbitMQ 연결을 위한 ConnectionFactory 구성
	 * application.yaml의 RabbitMQ 사용자 정보를 가져온 후
	 * RabbitMQ 연결에 필요한 ConnectionFactory 구성 */
	@Bean
	ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		String amqpUri = new StringBuilder()
			.append("amqp://")
			.append(username).append(":").append(password)
			.append("@").append(url)
			.toString();

		connectionFactory.setUri(amqpUri);
		connectionFactory.setConnectionNameStrategy(cn -> appName + "-" + cn);
		return connectionFactory;
	}

	/**
	 * 5. 메시지를 전송하고 수신하기 위한 JSON 타입으로 메시지를 변경
	 * Jackson2JsonMessageConverter를 사용하여 메시지 변환을 수행
	 * JSON 형식으로 메시지를 전송하고 수신 */
	@Bean
	MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/**
	 * 6. 구성한 ConnectionFactory, MessageConverter를 통해 템플릿 구성
	 */
	@Bean
	RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter);
		return rabbitTemplate;
	}
}
