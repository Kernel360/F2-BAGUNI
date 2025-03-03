package baguni.infra.infrastructure.sharedFolder.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import baguni.infra.infrastructure.folder.dto.FolderResult;
import baguni.infra.infrastructure.link.dto.LinkInfo;

public class SharedFolderResult {

	public record Create(
		String folderAccessToken
	) {
	}

	// TODO: 추후 공유자의 정보까지 DTO에 담아주는게 좋겠다.
	public record Read(
		FolderResult sourceFolder,
		String folderAccessToken
	) {
	}

	// 폴더, 태그, 픽이 모두 포함되서 반환
	@Builder
	public record SharedFolderInfo(

		@NotEmpty
		String folderName,

		@NotNull
		LocalDateTime createdAt,

		@NotNull
		LocalDateTime updatedAt,

		@NotNull
		List<SharedPickInfo> pickList,

		@Schema(description = "해당 폴더 내에서 사용된 모든 태그들 정보. tagIdxList의 각 값을 index로 사용하세요.", example = "[0, 5, 2, 3]")
		@NotNull
		List<SharedTagInfo> tagList
	) {
	}

	// id 같은 예민한 값을 모두 제외한 DTO
	@Builder
	public record SharedPickInfo(

		@NotEmpty
		@Schema(example = "자바 레코드 참고 블로그 1")
		String title,

		@NotNull
		LinkInfo linkInfo,

		@NotNull
		@Schema(description = "tagList.get(idx) 로 태그 정보를 획득할 수 있습니다.", example = "[0, 5, 2, 3]")
		List<Integer> tagIdxList,

		@NotNull
		LocalDateTime createdAt,

		@NotNull
		LocalDateTime updatedAt
	) {
	}

	// id 같은 예민한 값을 모두 제외한 DTO
	@Builder
	public record SharedTagInfo(

		@NotEmpty
		String name,

		@NotNull
		Integer colorNumber
	) {
	}
}