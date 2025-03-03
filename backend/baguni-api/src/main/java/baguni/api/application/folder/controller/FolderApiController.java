package baguni.api.application.folder.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import baguni.api.application.folder.dto.FolderApiMapper;
import baguni.api.application.folder.dto.FolderApiRequest;
import baguni.api.application.folder.dto.FolderApiResponse;
import baguni.infra.infrastructure.folder.dto.FolderResult;
import baguni.api.service.folder.service.FolderService;
import baguni.api.service.sharedFolder.service.SharedFolderService;
import baguni.security.annotation.LoginUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/folders")
@Tag(name = "폴더 API", description = "폴더 관련 API")
public class FolderApiController {

	private final FolderService folderService;
	private final SharedFolderService sharedFolderService;
	private final FolderApiMapper folderApiMapper;

	@GetMapping
	@Operation(summary = "루트 폴더와 하위 리스트 조회", description = "사용자의 루트 폴더와 루트 하위 전체 폴더를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(value = FolderApiConstant.ROOT_FOLDER_EXAMPLE)
			)),
		@ApiResponse(responseCode = "401", description = "본인 폴더만 조회할 수 있습니다.")
	})
	public ResponseEntity<List<FolderApiResponse>> getAllRootFolderList(@LoginUserId Long userId) {
		return ResponseEntity.ok(
			folderService.getAllRootFolderList(userId).stream()
						 .map(this::toApiResponseWithFolderAccessToken)
						 .toList()
		);
	}

	@GetMapping("/basic")
	@Operation(summary = "기본 폴더 리스트 조회", description = "사용자의 루트, 미분류, 휴지통 폴더를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "본인 폴더만 조회할 수 있습니다.")
	})
	public ResponseEntity<List<FolderApiResponse>> getBasicFolderList(@LoginUserId Long userId) {
		return ResponseEntity.ok(
			folderService.getBasicFolderList(userId)
						 .stream()
						 .map(folderApiMapper::toApiResponse)
						 .toList()
		);
	}

	@PostMapping
	@Operation(summary = "폴더 추가", description = "새로운 폴더를 추가합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "폴더 추가 성공")
	})
	public ResponseEntity<FolderApiResponse> createFolder(@LoginUserId Long userId,
		@Valid @RequestBody FolderApiRequest.Create request) {
		var result = folderService.saveFolder(folderApiMapper.toCreateCommand(userId, request));
		var response = folderApiMapper.toApiResponse(result);
		return ResponseEntity.ok(response);
	}

	@PatchMapping
	@Operation(summary = "폴더 수정", description = "사용자가 등록한 폴더를 수정합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "폴더 수정 성공"),
		@ApiResponse(responseCode = "400", description = "기본 폴더는 수정할 수 없습니다."),
		@ApiResponse(responseCode = "401", description = "본인 폴더만 수정할 수 있습니다.")
	})
	public ResponseEntity<Void> updateFolder(@LoginUserId Long userId,
		@Valid @RequestBody FolderApiRequest.Update request) {
		var command = folderApiMapper.toUpdateCommand(userId, request);
		folderService.updateFolder(command);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/location")
	@Operation(summary = "폴더 이동", description = "사용자가 등록한 폴더를 이동합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "폴더 이동 성공"),
		@ApiResponse(responseCode = "400", description = "기본 폴더는 이동할 수 없습니다."),
		@ApiResponse(responseCode = "401", description = "본인 폴더만 이동할 수 있습니다."),
		@ApiResponse(responseCode = "406", description = "미분류폴더, 휴지통 폴더로 이동할 수 없습니다."),
		@ApiResponse(responseCode = "406", description = "부모가 다른 폴더들을 동시에 이동할 수 없습니다.")
	})
	public ResponseEntity<Void> moveFolder(@LoginUserId Long userId,
		@Valid @RequestBody FolderApiRequest.Move request) {
		var command = folderApiMapper.toMoveCommand(userId, request);
		folderService.moveFolder(command);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping
	@Operation(summary = "폴더 삭제", description = "사용자가 등록한 폴더를 삭제합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "폴더 삭제 성공"),
		@ApiResponse(responseCode = "400", description = "기본 폴더는 삭제할 수 없습니다."),
		@ApiResponse(responseCode = "401", description = "본인 폴더만 삭제할 수 있습니다.")
	})
	public ResponseEntity<Void> deleteFolder(@LoginUserId Long userId,
		@Valid @RequestBody FolderApiRequest.Delete request) {
		var command = folderApiMapper.toDeleteCommand(userId, request);
		folderService.deleteFolder(command);
		return ResponseEntity.noContent().build();
	}

	// ----------------------------------------------------------------
	// internal helper method

	private FolderApiResponse toApiResponseWithFolderAccessToken(FolderResult folder) {
		String folderAccessToken = sharedFolderService
			.findFolderAccessTokenByFolderId(folder.id())
			.orElse(null);
		return folderApiMapper.toApiResponse(folder, folderAccessToken);
	}
}
