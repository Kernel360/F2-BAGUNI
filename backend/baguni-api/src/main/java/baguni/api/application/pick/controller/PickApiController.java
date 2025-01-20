package baguni.api.application.pick.controller;

import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import baguni.common.annotation.MeasureTime;
import baguni.common.event.events.LinkCrawlingEvent;
import baguni.common.event.messenger.CrawlingEventMessenger;
import baguni.common.event.messenger.RankingEventMessenger;
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
import baguni.common.event.events.PickCreateEvent;
import baguni.security.annotation.LoginUserId;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/picks")
@Tag(name = "Pick API", description = "픽 API")
public class PickApiController {

	private final PickService pickService;
	private final PickApiMapper pickApiMapper;
	private final PickSearchService pickSearchService;
	private final RankingEventMessenger rankingEventMessenger;
	private final CrawlingEventMessenger crawlingEventMessenger;

	@GetMapping
	@Operation(summary = "폴더 리스트 내 픽 리스트 조회", description = "해당 폴더 리스트 각각의 픽 리스트를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "픽 리스트 조회 성공")
	})
	public ResponseEntity<List<PickApiResponse.FolderPickListWithViewCount>> getFolderChildPickList(
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
						  .map(pickApiMapper::toApiFolderPickListWithViewCount)
						  .toList());
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
		@Parameter(description = "픽 시작 id 조회", example = "0") @RequestParam(required = false, defaultValue = "0") Long cursor,
		@Parameter(description = "한 페이지에 가져올 픽 개수", example = "20") @RequestParam(required = false, defaultValue = "20"
		) int size
	) {
		var command = pickApiMapper.toSearchPaginationCommand(
			userId, folderIdList, searchTokenList, tagIdList, cursor, size
		);

		Slice<PickResult.Pick> pickResultList = pickSearchService.searchPickPagination(command);

		return ResponseEntity.ok(new PickSliceResponse<>(pickApiMapper.toSliceApiResponse(pickResultList)));
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
	public ResponseEntity<PickApiResponse.Exist> getPickUrl(@LoginUserId Long userId,
		@RequestParam String link) {
		return ResponseEntity.ok(pickApiMapper.toApiExistResponse(pickService.existPickByUrl(userId, link)));
	}

	/**
	 * @author minkyeu kim
	 * 프론트엔드에서 4XX를 픽 없음으로 판단하는 로직이 불편하기 때문에
	 * Boolean으로 픽이 있다 없다를 판단하도록 변경합니다.
	 * 익스텐션은 구글 심사 때문에 기존 getLinkDataXXX를 이용하고, 추후 getLinkDataV2 로 교체할 예정
	 */
	@MeasureTime
	@GetMapping("/link-v2")
	@Operation(summary = "링크 픽 여부 조회 + 픽 정보 조회", description = "해당 링크를 픽한 적이 있는지 확인합니다. + 픽에 대한 정보도 반환합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "픽 여부 조회 성공"),
	})

	public ResponseEntity<PickApiResponse.PickExists> doesUserHasPickWithGivenUrl(
		@LoginUserId Long userId, @RequestParam String link
	) {
		var response = pickService.findPickUrl(userId, link)
								  .map(pickApiMapper::toApiResponse)
								  .map(pick -> new PickApiResponse.PickExists(true, pick))
								  .orElseGet(() -> new PickApiResponse.PickExists(false, null));
		return ResponseEntity.ok(response);
	}

	@Deprecated
	@GetMapping("/{id}")
	@Operation(
		summary = "[Deprecated] 픽 상세 조회",
		description = """
				현재 사용되지 않는 api 입니다.
				추후 코드 제거 예정입니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "픽 상세 조회 성공")
	})
	public ResponseEntity<PickApiResponse.Pick> getPick(@LoginUserId Long userId,
		@PathVariable Long id) {
		return ResponseEntity.ok(
			pickApiMapper.toApiResponse(
				pickService.getPick(pickApiMapper.toReadCommand(userId, new PickApiRequest.Read(id)))));
	}

	@PostMapping
	@Operation(summary = "픽 생성", description = "픽을 생성합니다. 또한, 픽 생성 이벤트가 랭킹 서버에 집계 됩니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "픽 생성 성공"),
		@ApiResponse(responseCode = "401", description = "잘못된 태그 접근"),
		@ApiResponse(responseCode = "403", description = "접근할 수 없는 폴더")
	})
	public ResponseEntity<PickApiResponse.Pick> savePick(@LoginUserId Long userId,
		@Valid @RequestBody PickApiRequest.Create request) {
		var command = pickApiMapper.toCreateCommand(userId, request);
		var result = pickService.saveNewPick(command);
		var event = new PickCreateEvent(userId, result.id(), result.linkInfo().url());
		rankingEventMessenger.send(event);
		var response = pickApiMapper.toApiResponse(result);
		return ResponseEntity.ok(response);
	}

	@MeasureTime
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
		var rankingEvent = new PickCreateEvent(userId, result.id(), request.url());
		var linkCrawlingEvent = new LinkCrawlingEvent(request.url());
		rankingEventMessenger.send(rankingEvent);
		crawlingEventMessenger.send(linkCrawlingEvent);
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
	@Operation(summary = "웹사이트에서 픽 내용만 수정 (폴더 이동 X)", description = "픽 내용 수정 및 폴더 이동까지 지원합니다.")
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
}
