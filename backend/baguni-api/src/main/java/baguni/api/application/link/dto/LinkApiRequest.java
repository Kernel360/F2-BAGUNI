package baguni.api.application.link.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LinkApiRequest {

	public record Update(
		@Schema(example = "https://techblog.woowa.in/wp-content/uploads/2023/02/2023-우아한테크-로고-2-e1675772695839.png") @NotNull String imageUrl,
		@Schema(example = "https://app.baguni.kr/image/og_image.png") @NotNull String updateImageUrl
	) {
	}
}
