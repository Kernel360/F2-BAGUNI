package techpick.api.domain.folder.dto;

import java.util.List;

public class FolderCommand {

	public record Create(
		Long userId,
		String name,
		Long parentFolderId) {
	}

	public record Read(
		Long userId,
		Long id) {
	}

	public record Update(
		Long userId,
		Long id,
		String name
	) {
	}

	public record Move(
		Long userId,
		List<Long> idList,
		Long destinationFolderId,
		int orderIdx
	) {
	}

	public record Delete(
		Long userId,
		List<Long> idList
	) {
	}
}
