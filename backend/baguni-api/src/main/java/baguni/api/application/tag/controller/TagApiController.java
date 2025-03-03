package baguni.api.application.tag.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import baguni.common.exception.base.ServiceException;
import baguni.common.exception.error_code.TagErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import baguni.api.application.tag.dto.TagApiMapper;
import baguni.api.application.tag.dto.TagApiRequest;
import baguni.api.application.tag.dto.TagApiResponse;
import baguni.api.service.tag.service.TagService;
import baguni.security.annotation.LoginUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
@Tag(name = "태그 API", description = "태그 관련 API")
public class TagApiController {

	private final TagService tagService;
	private final TagApiMapper tagApiMapper;

	@GetMapping
	@Operation(summary = "사용자 태그 조회", description = "사용자가 등록한 전체 태그를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "조회 성공")
	})
	public ResponseEntity<List<TagApiResponse.Read>> getAllUserTag(@LoginUserId Long userId) {
		return ResponseEntity.ok(
			tagService.getUserTagList(userId).stream()
					  .map(tagApiMapper::toReadResponse)
					  .toList()
		);
	}

	@PostMapping
	@Operation(summary = "태그 추가", description = "새로운 태그를 추가합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "태그 추가 성공"),
		@ApiResponse(responseCode = "400", description = "중복된 태그 이름", content = @Content(schema = @Schema()))
	})
	public ResponseEntity<TagApiResponse.Create> createTag(@LoginUserId Long userId,
		@Valid @RequestBody TagApiRequest.Create request) {
		if (!Objects.isNull(request.name()) && 200 < request.name().length()) {
			throw new ServiceException(TagErrorCode.TAG_NAME_TOO_LONG);
		}

		return ResponseEntity.ok(
			tagApiMapper.toCreateResponse(tagService.saveTag(tagApiMapper.toCreateCommand(userId, request))));
	}

	@PatchMapping
	@Operation(summary = "태그 수정", description = "사용자가 등록한 태그를 수정합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "태그 수정 성공"),
		@ApiResponse(responseCode = "400", description = "중복된 태그 이름"),
		@ApiResponse(responseCode = "401", description = "본인 태그만 수정할 수 있습니다.")
	})
	public ResponseEntity<Void> updateTag(@LoginUserId Long userId,
		@Valid @RequestBody TagApiRequest.Update request) {
		if (!Objects.isNull(request.name()) && 200 < request.name().length()) {
			throw new ServiceException(TagErrorCode.TAG_NAME_TOO_LONG);
		}

		tagService.updateTag(tagApiMapper.toUpdateCommand(userId, request));
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/location")
	@Operation(summary = "태그 이동", description = "사용자가 등록한 태그의 순서를 변경합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "태그 이동 성공"),
		@ApiResponse(responseCode = "401", description = "본인 태그만 이동할 수 있습니다.")
	})
	public ResponseEntity<Void> moveTag(@LoginUserId Long userId,
		@Valid @RequestBody TagApiRequest.Move request) {
		tagService.moveUserTag(tagApiMapper.toMoveCommand(userId, request));
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping
	@Operation(summary = "태그 삭제", description = "사용자가 등록한 태그를 삭제합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "태그 삭제 성공"),
		@ApiResponse(responseCode = "401", description = "본인 태그만 삭제할 수 있습니다.")
	})
	public ResponseEntity<Void> deleteTag(@LoginUserId Long userId,
		@Valid @RequestBody TagApiRequest.Delete request) {
		tagService.deleteTag(tagApiMapper.toDeleteCommand(userId, request));
		return ResponseEntity.noContent().build();
	}
}
