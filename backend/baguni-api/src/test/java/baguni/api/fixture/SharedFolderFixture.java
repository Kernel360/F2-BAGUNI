package baguni.api.fixture;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import baguni.infra.model.folder.Folder;
import baguni.infra.model.sharedFolder.SharedFolder;
import baguni.infra.model.user.User;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SharedFolderFixture {

	private UUID id;

	private Folder folder;

	private User user;

	public SharedFolder get() {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.convertValue(this, SharedFolder.class);
	}
}
