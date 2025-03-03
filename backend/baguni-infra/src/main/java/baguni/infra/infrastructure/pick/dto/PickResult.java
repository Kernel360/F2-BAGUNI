package baguni.infra.infrastructure.pick.dto;

import java.time.LocalDateTime;
import java.util.List;

import baguni.infra.infrastructure.link.dto.LinkInfo;

public class PickResult {

	public record Pick(
		Long id,
		String title,
		LinkInfo linkInfo,
		Long parentFolderId,
		List<Long> tagIdOrderedList,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
	}

	public record Extension(
		Long id,
		String title,
		Long linkId,
		String url,
		Long parentFolderId,
		List<Long> tagIdOrderedList,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
	}

	public record FolderPickList(
		Long folderId,
		List<PickResult.Pick> pickList
	) {
	}
}