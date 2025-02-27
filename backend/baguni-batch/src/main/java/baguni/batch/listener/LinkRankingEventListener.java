package baguni.batch.listener;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import baguni.common.config.RabbitmqConfig;
import baguni.common.event.events.BookmarkCreateEvent;
import baguni.common.event.events.LinkReadEvent;
import baguni.domain.infrastructure.link.LinkDataHandler;
import baguni.domain.infrastructure.link.LinkStatsRepository;
import baguni.domain.model.link.LinkStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMq Queue(PICK_RANKING) 에 들어오는 대로
 * 즉시 꺼내 집계에 반영하는 소비자 컴포넌트
 * */
@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = {RabbitmqConfig.QUEUE.LINK_RANKING_BATCH})
public class LinkRankingEventListener {

	private final LinkDataHandler linkDataHandler;

	@RabbitHandler
	public void updateViewCount(LinkReadEvent event) {
		log.info("메시지 소비: topic {}, url {}", event.getTopicString(), event.getUrl());
		var date = event.getTime().toLocalDate();
		var url = event.getUrl();
		var linkStats = linkDataHandler
			.findLinkStats(date, url)
			.orElseGet(() -> new LinkStats(date, url));

		linkStats.incrementViewCount();
		linkDataHandler.saveLinkStats(linkStats);
	}

	@RabbitHandler
	public void updateBookmarkedCount(BookmarkCreateEvent event) {
		log.info("메시지 소비: topic {}, url {}", event.getTopicString(), event.getUrl());
		var date = event.getTime().toLocalDate();
		var url = event.getUrl();
		var linkStats = linkDataHandler
			.findLinkStats(date, url)
			.orElseGet(() -> new LinkStats(date, url));

		linkStats.incrementBookmarkedCount();
		linkDataHandler.saveLinkStats(linkStats);
	}

	/**
	 * 매핑되지 않은 이벤트
	 */
	@RabbitHandler(isDefault = true)
	public void defaultMethod(Object object) {
		log.error("일치하는 이벤트 타입이 없습니다! {}", object.toString());
	}
}
