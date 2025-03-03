package baguni.api.fixture;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import baguni.infra.model.link.Link;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkFixture {

	private Long id;

	private String url;

	private String title;

	private String description;

	private String imageUrl;

	private LocalDateTime publishedAt;

	private Boolean isRss;

	public Link get() {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.convertValue(this, Link.class);
	}
}
