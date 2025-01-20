package baguni.batch.domain.link.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import baguni.common.config.RabbitmqConfig;
import baguni.common.event.events.LinkCrawlingEvent;
import baguni.domain.infrastructure.link.dto.LinkResult;
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
@RabbitListener(queues = {RabbitmqConfig.QUEUE.PICK_CRAWLING})
public class CrawlingEventListener {

	private final LinkService linkService;

	@RabbitHandler
	public void crawlingEvent(LinkCrawlingEvent event) {
		LinkResult link = linkService.getLinkResultByUrl(event.getUrl());
		long days = ChronoUnit.DAYS.between(link.updatedAt().toLocalDate(), LocalDate.now());

		// imageUrl, description이 비어있는 경우, link update가 90일이 지난 경우에만 OG 태그 업데이트 시도
		if (StringUtils.isEmpty(link.imageUrl()) || StringUtils.isEmpty(link.description()) || 90 <= days) {
			try {
				linkService.updateLink(link.url());
			} catch (Exception e) {
				log.info("메세지 큐에서 꺼낸 Link OG 크롤링 실패 : ", e);
			}
		}
	}
}
