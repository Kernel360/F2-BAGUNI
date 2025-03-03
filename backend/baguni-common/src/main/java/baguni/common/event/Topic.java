package baguni.common.event;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Topic {

	private String topicString;

	public Topic(String topic) {
		if (StringUtils.isBlank(topic)) {
			throw new IllegalArgumentException("Topic cannot be null or empty");
		}
		if (255 < topic.getBytes().length) {
			throw new IllegalArgumentException("rabbitmq topic (routing key) 는 255 byte를 넘길 수 없습니다.");
		}
		this.topicString = topic;
	}
}
