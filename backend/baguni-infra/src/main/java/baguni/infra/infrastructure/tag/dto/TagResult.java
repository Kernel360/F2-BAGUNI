package baguni.infra.infrastructure.tag.dto;

public record TagResult(
	Long id,
	String name,
	Integer colorNumber,
	Long userId
) {
}
