package baguni.api.application.pick.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import baguni.infra.infrastructure.link.dto.LinkInfo;

public class PickApiResponse {

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
		Long parentFolderId,
		List<Long> tagIdOrderedList,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
	}

	public record FolderPickList(
		Long folderId,
		List<PickApiResponse.Pick> pickList
	) {
	}

	public record Exist(
		@NotNull Boolean exist
	) {
	}
}
