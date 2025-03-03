package baguni.domain.infrastructure.tag.dto;

public class TagCommand {

	public record Create(
		Long userId,
		String name,
		Integer colorNumber) {
	}

	public record Read(
		Long userId,
		Long id) {
	}

	public record Update(
		Long userId,
		Long id,
		String name,
		Integer colorNumber) {
	}

	public record Move(
		Long userId,
		Long id,
		int orderIdx
	) {
	}

	public record Delete(
		Long userId,
		Long id
	) {
	}
}
