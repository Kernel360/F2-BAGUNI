package baguni.api.fixture;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.Getter;
import baguni.infra.model.folder.Folder;
import baguni.infra.model.link.Link;
import baguni.infra.model.pick.Pick;
import baguni.infra.model.user.User;

@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PickFixture {

	private Long id;

	private User user;

	private Link link;

	private Folder parentFolder;

	private String title;

	private List<Long> tagIdOrderedList;

	public Pick get() {
		if (tagIdOrderedList == null) {
			tagIdOrderedList = new ArrayList<>();
		}
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.convertValue(this, Pick.class);
	}
}
