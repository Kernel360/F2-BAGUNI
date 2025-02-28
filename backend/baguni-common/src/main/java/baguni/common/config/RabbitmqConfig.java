package baguni.common.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @see
 * <a href="https://docs.spring.io/spring-amqp/docs/1.5.1.RELEASE/reference/htmlsingle/#collection-declaration">
 *     spring amqp setup
 * </a>
 */
@Configuration
public class RabbitmqConfig {

	public static final class EXCHANGE {
		public static final String NAME = "exchange.domain";
	}

	public static final class QUEUE {
		public static final String LINK_RANKING_BATCH = "queue.link-ranking-batch";
		public static final String LINK_UPDATE = "queue.link-analyze";
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
	TopicExchange exchange() {
		return new TopicExchange(EXCHANGE.NAME);
	}

	/**
	 * 2. 큐 구성 */
	@Bean
	Queue linkRankingBatch() {
		return new Queue(QUEUE.LINK_RANKING_BATCH, false);
	}

	@Bean
	Queue linkUpdate() {
		return new Queue(QUEUE.LINK_UPDATE, false);
	}

	@Bean
	Declarables bindings() {
		return new Declarables(

			// link ranking v2 (batch)
			BindingBuilder.bind(linkRankingBatch()).to(exchange()).with("bookmark.create"),
			BindingBuilder.bind(linkRankingBatch()).to(exchange()).with("link.read"),

			// link analyze
			BindingBuilder.bind(linkUpdate()).to(exchange()).with("bookmark.create"),
			BindingBuilder.bind(linkUpdate()).to(exchange()).with("link.*")
		);
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

	@Bean
	MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/**
	 * ConnectionFactory를 통해서 RabbitMQ와의 연결을 설정한다.                     </br>
	 * 재시도 정책, 동시성 설정 등을 여기서 제어 가능.                                 </br>
	 * 현재는 단순한 링크 업데이트 뿐이라 Requeue 방식을 끄는 옵션으로 설정했으나,          </br>
	 *
	 * 만약 중요한 비즈니스 로직이 활용될 경우                                        </br>
	 *   1. 실패 메시지 큐잉 (Dead Letter Queue)                                 </br>
	 *   2. 재시도 + 폐기                                                       </br>
	 *   3. 재시도 + Parking Lot (재시도 N회 실패 한 메시지를 폐기하지 않고 별도로 저장)  </br>
	 *
	 * 이런 식으로 고도화 설정이 필요하다. 방법은 아래 링크 참조 바람.
	 *
	 * @see <a href="https://www.baeldung.com/spring-amqp-error-handling">
	 *     Baledung 글
	 * </a>
	 * @see <a href="https://codinghejow.tistory.com/426">
	 *     한국 블로그 글
	 * </a>
	 * ****************************
	 *      @Note 고려할 점
	 * ****************************
	 *
	 * 1. 근본적으로 잘못된 메세지는 재처리 과정이 불필요하다.
	 *    따라서 이 메시지의 실패 원인이 DB 오류 / 네트워크 / IO 오류인지, 그냥 잘못된 메시지인지 구분하는 것이 필요하다.
	 *
	 * 2. 메시지큐 예외는 ListenerExecutionFailedException --> ConditionalRejectingerrorHandler 를 타기에
	 *    GlobalExceptionHandler에서 잡히지 않는다. 따라서 슬랙으로 해당 알림을 주고 싶다면, 다른 방법을 찾아봐야 한다.
	 *
	 */
	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
		CachingConnectionFactory cachingConnectionFactory
	) {
		var containerFactory = new SimpleRabbitListenerContainerFactory();
		containerFactory.setConnectionFactory(cachingConnectionFactory);
		containerFactory.setDefaultRequeueRejected(false); // 그냥 바로 폐기
		containerFactory.setMessageConverter(messageConverter());
		containerFactory.setErrorHandler(new RabbitmqCustomErrorHandler());
		containerFactory.setObservationEnabled(true);
		return containerFactory;
	}

	/**
	 * 구성한 ConnectionFactory, MessageConverter를 통해 템플릿 구성
	 */
	@Bean
	RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter);
		return rabbitTemplate;
	}
}
