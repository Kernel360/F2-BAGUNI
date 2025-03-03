package baguni.batch.listener;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import baguni.batch.domain.link.service.LinkService;
import baguni.common.config.RabbitmqConfig;
import baguni.common.event.BookmarkCreateEvent;
import baguni.common.event.LinkCreateEvent;
import baguni.common.event.LinkReadEvent;
import baguni.infra.infrastructure.link.dto.LinkResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sangwon
 * 익스텐션을 이용하여 미분류로 픽을 담을 때 이벤트를 메세지 큐에 담음.
 * 메세지 큐에 담긴 데이터를 꺼내 imageUrl, description이 비어있을 때 크롤링함.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = {RabbitmqConfig.QUEUE.LINK_UPDATE})
public class LinkUpdateEventListener {

	private final LinkService linkService;

	/**
	 * TODO: Url 필드를 가진 부모 클래스를 상속 받는 방식으로 타입 개선
	 * ex. public void consumeMessage( UrlEvent ev ) { ... };
	 */

	@RabbitHandler
	public void consumeMessage(LinkCreateEvent ev) {
		log.info("메시지 소비: topic {}, url {}", ev.getTopicString(), ev.getUrl());
		LinkResult link = linkService.getLinkResultByUrl(ev.getUrl());
		doLinkUpdate(link);
	}

	@RabbitHandler
	public void consumeMessage(LinkReadEvent ev) {
		log.info("메시지 소비: topic {}, url {}", ev.getTopicString(), ev.getUrl());
		LinkResult link = linkService.getLinkResultByUrl(ev.getUrl());
		doLinkUpdate(link);
	}

	@RabbitHandler
	public void consumeMessage(BookmarkCreateEvent ev) {
		log.info("메시지 소비: topic {}, url {}", ev.getTopicString(), ev.getUrl());
		LinkResult link = linkService.getLinkResultByUrl(ev.getUrl());
		doLinkUpdate(link);
	}

	/**
	 * 매핑되지 않은 이벤트
	 */
	@RabbitHandler(isDefault = true)
	public void defaultMethod(Object object) {
		log.error("일치하는 이벤트 타입이 없습니다! {}", object.toString());
	}

	// Internal helper method ----------------------------

	private void doLinkUpdate(LinkResult oldLink) {
		var prevUpdatedDate = oldLink.updatedAt().toLocalDate();
		var daysPassedSinceLastUpdate = ChronoUnit.DAYS.between(prevUpdatedDate, LocalDate.now());
		log.info("링크를 최신화한지 {}일 경과", daysPassedSinceLastUpdate);

		if (
			StringUtils.isEmpty(oldLink.imageUrl())
				|| StringUtils.isEmpty(oldLink.description())
				|| (90 <= daysPassedSinceLastUpdate)
		) {
			linkService.analyzeAndUpdateLink(oldLink.url());
		}
	}
}