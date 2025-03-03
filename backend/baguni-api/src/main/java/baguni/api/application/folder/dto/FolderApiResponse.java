package baguni.api.application.folder.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import baguni.infra.model.folder.FolderType;

public record FolderApiResponse(
	Long id,

	String name,

	@Schema(example = "GENERAL")
	FolderType folderType,

	Long parentFolderId,

	List<Long> childFolderIdOrderedList,

	LocalDateTime createdAt,

	LocalDateTime updatedAt,

	@Schema(
		nullable = true, description = """
		    비공개 폴더일 경우 null을 반환.
		    공유된 폴더일 경우 조회용 UUID 토큰을 반환.
		""")
	String folderAccessToken
) {
}
