package baguni.api.application.pick.controller;

import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import baguni.common.event.BookmarkCreateEvent;
import baguni.common.event.EventMessenger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import baguni.api.application.pick.dto.PickApiMapper;
import baguni.api.application.pick.dto.PickApiRequest;
import baguni.api.application.pick.dto.PickApiResponse;
import baguni.api.application.pick.dto.PickSliceResponse;
import baguni.domain.infrastructure.pick.dto.PickResult;
import baguni.api.service.pick.service.PickSearchService;
import baguni.api.service.pick.service.PickService;
import baguni.security.annotation.LoginUserId;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/picks")
@Tag(name = "픽 API", description = "북마크 관련 API")
public class PickApiController {

	private final PickService pickService;
	private final PickApiMapper pickApiMapper;
	private final PickSearchService pickSearchService;
	private final EventMessenger eventMessenger;

	@GetMapping
	@Operation(summary = "폴더 리스트 내 픽 리스트 조회", description = "해당 폴더 리스트 각각의 픽 리스트를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "픽 리스트 조회 성공")
	})
	public ResponseEntity<List<PickApiResponse.FolderPickList>> getFolderChildPickList(
		@LoginUserId Long userId,
		@Parameter(description = "조회할 폴더 ID 목록", example = "1, 2, 3")
		@RequestParam(required = false, defaultValue = "")
		List<Long> folderIdList
	) {
		var folderPickList = pickService.getFolderListChildPickList(
			pickApiMapper.toReadListCommand(userId, folderIdList)
		);
		return ResponseEntity.ok(
			folderPickList.stream()
						  .map(pickApiMapper::toApiFolderPickList)
						  .toList());
	}

	@GetMapping("/pagination")
	@Operation(summary = "폴더 내 픽 페이지네이션 리스트 조회",
		description = """
				해당 폴더의 픽 리스트를 조회합니다.
				커서 기반 페이지네이션 처리된 리스트가 반환됩니다.
			""")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "픽 리스트 조회 성공")
	})
	public ResponseEntity<PickSliceResponse<PickApiResponse.Pick>> getFolderChildPickPagination(
		@LoginUserId Long userId,
		@Parameter(description = "조회할 폴더 ID", example = "1") @RequestParam Long folderId,
		@Parameter(description = """
				다음에 조회할 커서(cursor) 값입니다.
				처음 페이지를 조회할 때는 0을 넣어주세요.
				이후에는 응답으로 받은 lastCursor 값을 그대로 사용하시면 됩니다.
				예시: lastCursor = 1 → 다음 요청에 cursor=1을 넣으면, 1은 제외하고 2부터 조회합니다.
			""",
			example = "0") @RequestParam(required = false, defaultValue = "0") Long cursor,
		@Parameter(description = "한 페이지에 가져올 픽 개수", example = "20") @RequestParam(required = false, defaultValue = "20"
		) int size
	) {
		var command = pickApiMapper.toReadPaginationCommand(userId, folderId, cursor, size);
		var pickList = pickService.getFolderListChildPickPagination(command);
		return ResponseEntity.ok(pickApiMapper.toSliceApiResponse(pickList));
	}

	@GetMapping("/search")
	@Operation(summary = "픽 리스트 검색(페이지네이션)", description = "페이지네이션 처리 된 픽 리스트 검색")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "조회 성공")
	})
	public ResponseEntity<PickSliceResponse<PickApiResponse.Pick>> searchPickPagination(
		@LoginUserId Long userId,
		@Parameter(description = "조회할 폴더 ID 목록", example = "1, 2, 3") @RequestParam(required = false, defaultValue =
			"") List<Long> folderIdList,
		@Parameter(description = "검색 토큰 목록", example = "리액트, 쿼리, 서버") @RequestParam(required = false, defaultValue =
			"") List<String> searchTokenList,
		@Parameter(description = "검색 태그 ID 목록", example = "1, 2, 3") @RequestParam(required = false,
			defaultValue = "") List<Long> tagIdList,
		@Parameter(description = """
				다음에 조회할 커서(cursor) 값입니다.
				처음 페이지를 조회할 때는 0을 넣어주세요.
				이후에는 응답으로 받은 lastCursor 값을 그대로 사용하시면 됩니다.
				예시: lastCursor = 1 → 다음 요청에 cursor=1을 넣으면, 1은 제외하고 2부터 조회합니다.
			""",
			example = "0") @RequestParam(required = false, defaultValue = "0") Long cursor,
		@Parameter(description = "한 페이지에 가져올 픽 개수", example = "20") @RequestParam(required = false, defaultValue = "20"
		) int size
	) {
		var command = pickApiMapper.toSearchPaginationCommand(
			userId, folderIdList, searchTokenList, tagIdList, cursor, size
		);

		Slice<PickResult.Pick> pickResultList = pickSearchService.searchPickPagination(command);
		return ResponseEntity.ok(pickApiMapper.toSliceApiResponse(pickResultList));
	}

	@GetMapping("/link")
	@Operation(
		summary = "링크 픽 여부 조회",
		description = """
				해당 링크를 픽한 적이 있는지 확인합니다.
				boolean 값을 반환합니다.
				true : 존재, false : 존재하지 않음.
			""")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "픽 여부 조회 성공"),
	})
	public ResponseEntity<PickApiResponse.Exist> existPick(@LoginUserId Long userId,
		@RequestParam String link) {
		return ResponseEntity.ok(pickApiMapper.toApiExistResponse(pickService.existPickByUrl(userId, link)));
	}

	@PostMapping
	@Operation(summary = "웹 사이트에서 픽 생성", description = "웹 사이트에서 픽을 생성합니다. 또한, 픽 생성 이벤트가 랭킹 서버에 집계 됩니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "픽 생성 성공"),
		@ApiResponse(responseCode = "401", description = "잘못된 태그 접근"),
		@ApiResponse(responseCode = "403", description = "접근할 수 없는 폴더")
	})
	public ResponseEntity<PickApiResponse.Pick> savePick(
		@LoginUserId Long userId,
		@Valid @RequestBody PickApiRequest.Create request
	) {
		var command = pickApiMapper.toCreateCommand(userId, request);
		var result = pickService.saveNewPick(command);

		eventMessenger.send(new BookmarkCreateEvent(result.linkInfo().url()));

		var response = pickApiMapper.toApiResponse(result);
		return ResponseEntity.ok(response);
	}

	/**
	 *	익스텐션에서 사용하지 않게 되면 제거
	 */
	@PostMapping("/extension")
	@Operation(
		summary = "[익스텐션 전용] 미분류 폴더로 픽 생성",
		description = "익스텐션에서 미분류로 바로 픽 생성합니다. 또한, 픽 생성 이벤트가 랭킹 서버에 집계됩니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "픽 생성 성공"),
		@ApiResponse(responseCode = "404", description = "OG 태그 업데이트를 위한 크롤링 요청 실패")
	})
	public ResponseEntity<PickApiResponse.Extension> savePickAsUnclassified(
		@LoginUserId Long userId,
		@Valid @RequestBody PickApiRequest.CreateFromExtension request
	) {
		var command = pickApiMapper.toExtensionCommand(userId, request.title(), request.url());
		var result = pickService.savePickToUnclassified(command);

		eventMessenger.send(new BookmarkCreateEvent(request.url()));

		var response = pickApiMapper.toApiExtensionResponse(result);
		return ResponseEntity.ok(response);
	}

	// TODO: 다루는 도메인이 pick 외에 생길 경우 extension 컨트롤러로 빼기
	@PatchMapping("/extension")
	@Operation(summary = "[익스텐션 전용] 픽 수정", description = "픽 내용 수정 및 폴더 이동까지 지원합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "픽 내용 수정 성공")
	})
	public ResponseEntity<PickApiResponse.Pick> updatePickFromChromeExtension(
		@LoginUserId Long userId,
		@Valid @RequestBody PickApiRequest.UpdateFromExtension request
	) {
		var command = pickApiMapper.toUpdateCommand(userId, request);
		var result = pickService.updatePick(command);
		var response = pickApiMapper.toApiResponse(result);
		return ResponseEntity.ok(response);
	}

	@PatchMapping
	@Operation(summary = "웹 사이트에서 픽 내용만 수정 (폴더 이동 X)", description = "픽 내용 수정 및 폴더 이동까지 지원합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "픽 내용 수정 성공")
	})
	public ResponseEntity<PickApiResponse.Pick> updatePick(
		@LoginUserId Long userId,
		@Valid @RequestBody PickApiRequest.Update request
	) {
		var command = pickApiMapper.toUpdateCommand(userId, request);
		var result = pickService.updatePick(command);
		var response = pickApiMapper.toApiResponse(result);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/location")
	@Operation(summary = "픽 이동", description = "픽을 같은 폴더 혹은 다른 폴더로 이동합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "픽 이동 성공"),
		@ApiResponse(responseCode = "400", description = "폴더가 존재하지 않음.")
	})
	public ResponseEntity<Void> movePick(
		@LoginUserId Long userId,
		@Valid @RequestBody PickApiRequest.Move request
	) {
		var command = pickApiMapper.toMoveCommand(userId, request);
		pickService.movePick(command);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping
	@Operation(summary = "픽 삭제", description = "휴지통에 있는 픽만 삭제 가능합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "픽 삭제 성공"),
		@ApiResponse(responseCode = "406", description = "휴지통이 아닌 폴더에서 픽 삭제 불가"),
		@ApiResponse(responseCode = "500", description = "미확인 서버 에러 혹은 존재하지 않는 픽 삭제")
	})
	public ResponseEntity<Void> deletePick(
		@LoginUserId Long userId,
		@Valid @RequestBody PickApiRequest.Delete request
	) {
		var command = pickApiMapper.toDeleteCommand(userId, request);
		pickService.deletePick(command);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/recycle-bin")
	@Operation(summary = "휴지통 비우기", description = "휴지통에 있는 픽 리스트들을 모두 삭제합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "픽 삭제 성공"),
		@ApiResponse(responseCode = "406", description = "휴지통이 아닌 폴더에서 픽 삭제 불가"),
		@ApiResponse(responseCode = "500", description = "미확인 서버 에러 혹은 존재하지 않는 픽 삭제")
	})
	public ResponseEntity<Void> deleteAllPickFromRecycleBin(@LoginUserId Long userId) {
		pickService.deletePickFromRecycleBin(userId);
		return ResponseEntity.noContent().build();
	}
}
