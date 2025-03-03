package baguni.api.application.extension.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import baguni.api.application.extension.dto.ExtensionApiMapper;
import baguni.api.application.extension.dto.ExtensionApiRequest;
import baguni.api.application.extension.dto.ExtensionApiResponse;
import baguni.api.service.pick.service.PickService;
import baguni.common.event.BookmarkCreateEvent;
import baguni.common.event.EventMessenger;
import baguni.security.annotation.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/extension")
@Tag(name = "익스텐션 API", description = "익스텐션 API는 메이저 버전을 명시합니다.")
public class ExtensionApiControllerV1 {

	private final PickService pickService;
	private final ExtensionApiMapper extensionApiMapper;
	private final EventMessenger eventMessenger;

	@PostMapping("/picks")
	@Operation(
		summary = "[익스텐션 v1] 픽 생성",
		description = """
				익스텐션에서 픽 생성합니다.
				익스텐션 메이저 버전을 명시합니다. (ex. /v1, /v2, /v3)
				또한, 픽 생성 이벤트가 랭킹 서버에 집계됩니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "픽 생성 성공"),
		@ApiResponse(responseCode = "400", description = "유저 또는 폴더가 존재하지 않습니다."),
		@ApiResponse(responseCode = "401", description = "잘못된 접근 (폴더, 태그)"),
		@ApiResponse(responseCode = "406", description = "허용되지 않는 폴더")
	})
	public ResponseEntity<ExtensionApiResponse.Pick> savePickFromExtension(
		@LoginUserId Long userId,
		@Valid @RequestBody ExtensionApiRequest.Create request
	) {
		var command = extensionApiMapper.toPickCreateCommand(userId, request);
		var result = pickService.savePickFromExtension(command);
		var response = extensionApiMapper.toApiPickResponse(result);
		eventMessenger.send(new BookmarkCreateEvent(request.url()));
		return ResponseEntity.ok(response);
	}

	/**
	 * 	익스텐션 전용 API가 필요한가? (내 폴더 리스트 조회, 태그 리스트 조회, 존재하는 픽)
	 * 	프론트에게 질문 후 반영
	 *
	 * 	픽 수정은 기획에 따라 생성 여부가 달라짐
	 */
}
