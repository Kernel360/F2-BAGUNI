package baguni.batch.domain.link.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import baguni.domain.exception.link.ApiLinkException;
import baguni.domain.model.link.Link;
import baguni.domain.infrastructure.link.LinkDataHandler;
import baguni.domain.infrastructure.link.dto.LinkInfo;
import baguni.domain.infrastructure.link.dto.LinkMapper;
import baguni.domain.infrastructure.link.dto.LinkResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

	private final LinkDataHandler linkDataHandler;
	private final LinkAnalyzer linkAnalyzer;
	private final LinkMapper linkMapper;

	@Transactional(readOnly = true)
	public LinkResult getLinkResultByUrl(String url) {
		Link link = linkDataHandler.getOptionalLink(url).orElseGet(() -> Link.createLinkByUrl(url));
		return linkMapper.toLinkResult(link);
	}

	/**
	 * 	크롤링 시 해당 메서드 호출하여 링크 업데이트
	 */
	@Transactional
	public LinkInfo updateLink(String url) {
		Link link = linkDataHandler.getLink(url);
		try {
			var updatedLink = linkAnalyzer.updateOgTag(url, link);
			return linkMapper.toLinkInfo(linkDataHandler.saveLink(updatedLink));
		} catch (Exception e) {
			log.info("OG Tag Selenium으로 업데이트 : ", e);
			throw ApiLinkException.LINK_OG_TAG_UPDATE_FAILURE();
		}
	}
}
