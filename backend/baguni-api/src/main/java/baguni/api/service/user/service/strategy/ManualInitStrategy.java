package baguni.api.service.user.service.strategy;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import baguni.infra.infrastructure.user.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import baguni.infra.infrastructure.link.dto.LinkInfo;
import baguni.api.service.link.service.LinkService;
import baguni.infra.infrastructure.pick.dto.PickCommand;
import baguni.api.service.pick.service.PickService;

@Slf4j
@Component
@Qualifier("app-manual")
@RequiredArgsConstructor
public class ManualInitStrategy implements ContentInitStrategy {

	private final PickService pickService;
	private final LinkService linkService;

	private final List<String> MANUAL_URLS = List.of(
		// 설문 조사
		"https://positive-airboat-4de.notion.site/15841a7fba6580f78caee50c069a1247?pvs=4",
		// 확장 프로그램
		"https://positive-airboat-4de.notion.site/15841a7fba65808b8636e15e6c6d9679?pvs=4",
		// 앱 사용법
		"https://positive-airboat-4de.notion.site/15841a7fba65809d89a6dceb89060f70?pvs=4"
	);

	/**
	 * link 가 db 에 존재하지 않으면 새로 추가함 by psh
	 */
	@Override
	public void initContent(UserInfo info, Long folderId) {
		for (var url : MANUAL_URLS) {
			LinkInfo linkInfo = null;
			try {
				linkInfo = linkService.getLinkInfo(url);
			} catch (Exception e) {
				linkInfo = linkService.saveLink(url); // url 외에 다른 필드는 모두 빈 문자열인 Link 생성
			}
			var command = new PickCommand.Create(
				info.id(), linkInfo.title(), new ArrayList<>(), folderId, linkInfo
			);
			pickService.saveNewPick(command);
		}
	}
}
