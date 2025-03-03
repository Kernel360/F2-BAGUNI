package baguni.infra.infrastructure.link.dto;

public class LinkCommand {

	public record Update(
		Long userId,
		String imageUrl,
		String updateImageUrl
	) {
	}
}
