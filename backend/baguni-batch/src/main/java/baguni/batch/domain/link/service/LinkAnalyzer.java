package baguni.batch.domain.link.service;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.stereotype.Component;

import baguni.common.lib.opengraph.Metadata;
import baguni.common.lib.opengraph.OpenGraph;
import baguni.common.lib.opengraph.OpenGraphException;
import baguni.common.lib.opengraph.OpenGraphReader;
import baguni.domain.model.link.Link;
import lombok.RequiredArgsConstructor;

/**
 * 	@author sangwon
 * 	링크 분석 클래스 (크롤링, 링크 유효성 검사)
 */
@Component
@RequiredArgsConstructor
public class LinkAnalyzer {

	private final OpenGraphReader openGraphReader;

	/**
	 *	Jsoup 또는 Selenium 이용하여 크롤링
	 *	현재는 Selenium 사용하도록 되어 있음.
	 *  Jsoup, Selenium 둘 다 사용하고 싶으면 @Qualifier 사용
 	 */
	public Link updateOgTag(String url, Link link) throws OpenGraphException {
		return updateOgTagCommon(url, link, openGraphReader);
	}

	private Link updateOgTagCommon(String url, Link link, OpenGraphReader openGraphReader) throws OpenGraphException {
		var openGraph = new OpenGraph(url, openGraphReader);
		link.updateMetadata(
			openGraph.getTag(Metadata.OG_TITLE)
					 .orElse(openGraph.getTag(Metadata.TITLE)
									  .orElse("")),
			openGraph.getTag(Metadata.OG_DESCRIPTION)
					 .orElse(openGraph.getTag(Metadata.DESCRIPTION)
									  .orElse("")),
			correctImageUrl(url, openGraph.getTag(Metadata.OG_IMAGE)
										  .orElse(openGraph.getTag(Metadata.IMAGE)
														   .orElse(openGraph.getTag(Metadata.ICON)
																			.orElse(""))))
		);
		return link;
	}

	/**
	 * 	og:image 가 완전한 url 형식이 아닐 수 있어 보정
	 * 	추론 불가능한 image url 일 경우 빈스트링("")으로 대치
	 *
	 *  @author sangwon
	 * 	protocol : https
	 * 	host : blog.dongolab.com
	 */
	private String correctImageUrl(String baseUrl, String imageUrl) {
		// "null"이 넘어오는 경우가 있음.
		// favicon 가져올 때 <link href=> -> "null"로 넘어옴
		if (imageUrl == null || imageUrl.trim().isEmpty() || imageUrl.equals("null")) {
			return "";
		}

		if (imageUrl.startsWith("://")) {
			return "https" + imageUrl;
		}
		if (imageUrl.startsWith("//")) {
			return "https:" + imageUrl;
		}

		try {
			URL url = new URL(baseUrl);
			// ex) https://blog.dongolab.com
			String domain = url.getProtocol() + "://" + url.getHost();

			// ex) /og-image.png -> https://blog.dongholab.com/og-image.png
			if (imageUrl.startsWith("/")) {
				return domain + imageUrl;
			}

			if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
				return domain + "/" + imageUrl;
			}

			return imageUrl;
		} catch (MalformedURLException e) {
			// baseUrl이 올바르지 않은 경우 빈 문자열 반환
			return "";
		}
	}
}
