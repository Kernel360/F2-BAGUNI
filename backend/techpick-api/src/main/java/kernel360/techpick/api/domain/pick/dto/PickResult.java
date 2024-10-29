package kernel360.techpick.api.domain.pick.dto;

import java.time.LocalDateTime;
import java.util.List;

import kernel360.techpick.api.domain.link.dto.LinkInfo;

public record PickResult(
	Long id,
	String title,
	String memo,
	LinkInfo linkInfo,
	Long parentFolderId,
	List<Long> tagOrderList,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
}