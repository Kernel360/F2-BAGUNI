package baguni.api.application.pick.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import baguni.api.service.link.dto.LinkInfo;

public class PickApiRequest {

	public record Create(
		@Schema(example = "Record란?") String title,
		@Schema(example = "[4, 5, 2, 1, 3]") List<Long> tagIdOrderedList,
		@Schema(example = "1") Long parentFolderId,
		LinkInfo linkInfo
	) {
	}

	public record Extension(
		@Schema(example = "https://d2.naver.com/helloworld/8149881") String url,
		@Schema(example = "GitHub Actions를 이용한 코드 리뷰 문화 개선기") String title
	) {
	}

	public record Read(
		@Schema(example = "1") @NotNull(message = "{id.notNull}") Long id
	) {
	}

	public record Update(
		@Schema(example = "1") @NotNull(message = "{id.notNull}") Long id,
		@Schema(example = "Record란 뭘까?") String title,
		@Schema(example = "[4, 5, 2, 1]") List<Long> tagIdOrderedList
	) {
	}

	/**
	 * @deprecated
	 * 구 버전 익스텐션은 폴더 위치도 수정할 수 있습니다.
	 * 해당 기능을 유지하기 위한 임시 기능이며, 익스텐션 버전 업과 동시에 삭제 예정입니다.
	 */
	public record UpdateXXX(
		@Schema(example = "1") @NotNull(message = "{id.notNull}") Long id,
		@Schema(example = "Record란 뭘까?") String title,
		@Schema(example = "3") Long parentFolderId,
		@Schema(example = "[4, 5, 2, 1]") List<Long> tagIdOrderedList
	) {
	}

	public record Move(
		@Schema(example = "[1, 2]") @NotNull(message = "{idList.notNull}") List<Long> idList,
		@Schema(example = "3") @NotNull(message = "{destinationFolderId.notNull}") Long destinationFolderId,
		@Schema(example = "0") int orderIdx
	) {
	}

	public record Delete(
		@Schema(example = "[1]") @NotNull(message = "{idList.notNull}") List<Long> idList
	) {
	}
}