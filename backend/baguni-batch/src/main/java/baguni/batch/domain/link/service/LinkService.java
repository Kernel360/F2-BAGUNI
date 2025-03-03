package baguni.batch.domain.link.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import baguni.batch.domain.link.dto.LinkAnalyzeResult;
import baguni.infra.model.link.Link;
import baguni.infra.infrastructure.link.LinkDataHandler;
import baguni.infra.infrastructure.link.dto.LinkMapper;
import baguni.infra.infrastructure.link.dto.LinkResult;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

	private final LinkDataHandler linkDataHandler;
	private final LinkAnalyzer linkAnalyzer;
	private final LinkMapper linkMapper;

	@WithSpan
	@Transactional(readOnly = true)
	public LinkResult getLinkResultByUrl(String url) {
		Link link = linkDataHandler.getLink(url);
		return linkMapper.toLinkResult(link);
	}

	@WithSpan
	@Transactional
	public void analyzeAndUpdateLink(String url) {
		Link link = linkDataHandler.getLink(url);
		LinkAnalyzeResult result = linkAnalyzer.analyze(url);

		if (StringUtils.isNotEmpty(result.title())) {
			link.updateMetadata(result.title(), result.description(), result.imageUrl());
		}
	}
}
