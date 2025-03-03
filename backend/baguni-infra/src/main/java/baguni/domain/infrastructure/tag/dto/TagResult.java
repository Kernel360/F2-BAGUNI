package baguni.domain.infrastructure.tag.dto;

public record TagResult(
	Long id,
	String name,
	Integer colorNumber,
	Long userId
) {
}
