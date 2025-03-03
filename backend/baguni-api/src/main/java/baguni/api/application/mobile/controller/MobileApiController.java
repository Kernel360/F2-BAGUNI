package baguni.api.application.mobile.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import baguni.api.application.folder.dto.FolderApiMapper;
import baguni.api.application.folder.dto.FolderApiResponse;
import baguni.api.service.folder.service.FolderService;
import baguni.api.service.sharedFolder.service.SharedFolderService;
import baguni.infra.infrastructure.folder.dto.FolderResult;
import baguni.security.annotation.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mobile")
@Tag(name = "모바일 API", description = "모바일 전용 API")
public class MobileApiController {

	private final FolderService folderService;
	private final SharedFolderService sharedFolderService;
	private final FolderApiMapper folderApiMapper;

	@GetMapping("/folders")
	@Operation(summary = "미분류, 휴지통 폴더와 루트 하위 폴더 조회",
		description = """
				폴더 depth가 1인 경우만 조회합니다.
				사용자의 미분류, 휴지통 폴더와 루트 하위 전체 폴더를 조회합니다.
				루트 폴더는 조회하지 않습니다.
			""")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "본인 폴더만 조회할 수 있습니다.")
	})
	public ResponseEntity<List<FolderApiResponse>> getMobileFolderList(@LoginUserId Long userId) {
		return ResponseEntity.ok(
			folderService.getMobileFolderList(userId).stream()
						 .map(this::toApiResponseWithFolderAccessToken)
						 .toList()
		);
	}

	private FolderApiResponse toApiResponseWithFolderAccessToken(FolderResult folder) {
		String folderAccessToken = sharedFolderService
			.findFolderAccessTokenByFolderId(folder.id())
			.orElse(null);
		return folderApiMapper.toApiResponse(folder, folderAccessToken);
	}
}
