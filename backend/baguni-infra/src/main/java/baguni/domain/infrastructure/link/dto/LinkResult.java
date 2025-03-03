package baguni.domain.infrastructure.link.dto;

import java.time.LocalDateTime;

public record LinkResult(
	Long id,
	String url,
	String title,
	String description,
	String imageUrl,
	LocalDateTime updatedAt
) {
}
