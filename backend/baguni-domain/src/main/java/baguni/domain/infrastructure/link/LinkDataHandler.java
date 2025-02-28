package baguni.domain.infrastructure.link;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import baguni.common.exception.base.ServiceException;
import baguni.domain.exception.link.LinkErrorCode;
import baguni.domain.model.link.Link;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LinkDataHandler {
	private final LinkRepository linkRepository;

	@WithSpan
	@Transactional(readOnly = true)
	public Link getLink(String url) {
		return linkRepository.findByUrl(url)
							 .orElseThrow(() -> new ServiceException(LinkErrorCode.LINK_NOT_FOUND));
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<Link> getLinkList(List<String> urlList) {
		return linkRepository.findByUrlIn(urlList);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public Optional<Link> getOptionalLink(String url) {
		return linkRepository.findByUrl(url);
	}

	@WithSpan
	@Transactional
	public Link saveLink(Link link) {
		return linkRepository.save(link);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public boolean existsByUrl(String url) {
		return linkRepository.existsByUrl(url);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public List<Link> getRssLinkList(int limit) {
		return linkRepository.findAllRssBlogArticlesOrderByPublishedDate(
			PageRequest.of(0, limit)
		);
	}
}
