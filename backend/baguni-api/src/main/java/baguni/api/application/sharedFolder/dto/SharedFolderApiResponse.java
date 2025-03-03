package baguni.api.application.sharedFolder.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import baguni.infra.infrastructure.sharedFolder.dto.SharedFolderResult;

public class SharedFolderApiResponse {

	public record Create(
		@Schema(description = "{shared.folderAccessToken.description}", example = "9b1deb4d-3b7d-4bad-9bdd"
			+ "-2b0d7b3dcb6d")
		@NotNull(message = "{shared.folderAccessToken.notNull}")
		String folderAccessToken
	) {
	}

	/**
	 * @author minkyeu kim
	 * 내 공유 폴더 목록 획득시 사용되는 DTO.
	 * 내부 픽 목록, 태그 조회 등을 하지 않는다.
	 */
	public record ReadFolderPartial(
		@Schema(description = "원본 폴더의 이름", example = "리액트 모음집")
		@NotNull
		Long sourceFolderId,

		@Schema(description = "원본 폴더의 이름", example = "리액트 모음집")
		@NotBlank(message = "{folder.name.notBlank}")
		String sourceFolderName,

		@Schema(description = "원본 폴더의 생성 시점", example = "2024-11-29T06:03:49.182Z")
		@NotNull
		LocalDateTime sourceFolderCreatedAt,

		@Schema(description = "원본 폴더의 마지막 업데이트 시점", example = "2024-11-29T06:03:49.182Z")
		@NotNull
		LocalDateTime sourceFolderUpdatedAt,

		@Schema(
			description = "{shared.folderAccessToken.description}",
			example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
		)
		@NotNull
		String folderAccessToken
	) {
	}

	/**
	 * @author minkyeu kim
	 * 외부에서 공유 폴더 접근 시, 모든 정보를 내려줘야 한 페이지로 그려낼 수 있다.
	 * 이를 위해서 DB에서 모든 픽, 태그 정보를 반환한다.
	 */
	public record ReadFolderFull(
		@Schema(description = "원본 폴더의 이름", example = "리액트 모음집")
		@NotBlank(message = "{folder.name.notBlank}")
		String folderName,

		@Schema(description = "원본 폴더의 생성 시점", example = "2024-11-29T06:03:49.182Z")
		@NotNull
		LocalDateTime createdAt,

		@Schema(description = "원본 폴더의 마지막 업데이트 시점", example = "2024-11-29T06:03:49.182Z")
		@NotNull
		LocalDateTime updatedAt,

		@Schema(description = "폴더 내 pick 리스트")
		@NotNull
		List<SharedFolderResult.SharedPickInfo> pickList,

		@Schema(
			description = "해당 폴더 내에서 사용된 모든 태그 정보가 담길 배열. tagList.get(idx) 로 태그 정보를 획득할 수 있습니다.",
			example = """
				    [
				        { "name": "리액트", "colorNumber": "2" },
				        { "name": "CSS", "colorNumber": "8" }
				    ]
				"""
		)
		@NotNull
		List<SharedFolderResult.SharedTagInfo> tagList
	) {
	}
}
