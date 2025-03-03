package baguni.infra.infrastructure.link.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record BlogLinkInfo(
	@Schema(example = "https://velog.io/@hyeok_1212/Java-Record-%EC%82%AC%EC%9A%A9%ED%95%98%EC%8B%9C%EB%82%98%EC%9A%94"
	) @NotNull String url,
	@Schema(example = "[Java] Record 사용하시나요?") String title,
	@Schema(example = "IntelliJ : 레코드 써봐") String description,
	@Schema(example = "https://velog.velcdn.com/images/hyeok_1212/post/5ea148fb-1490-4b03-811e-222b4d26b65e/image.png") String imageUrl,
	LocalDateTime publishedAt
) {
}

