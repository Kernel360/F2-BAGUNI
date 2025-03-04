package baguni.api.fixture;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.Getter;
import baguni.infra.model.folder.Folder;
import baguni.infra.model.folder.FolderType;
import baguni.infra.model.user.User;

@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FolderFixture {
	private Long id;

	private String name;

	private FolderType folderType;

	private Folder parentFolder;

	private User user;

	private List<Long> childFolderIdOrderedList;

	private List<Long> childPickIdOrderedList;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	public Folder get() {
		if (childFolderIdOrderedList == null) {
			childFolderIdOrderedList = new ArrayList<>();
		}
		if (childPickIdOrderedList == null) {
			childPickIdOrderedList = new ArrayList<>();
		}
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.convertValue(this, Folder.class);
	}
}
