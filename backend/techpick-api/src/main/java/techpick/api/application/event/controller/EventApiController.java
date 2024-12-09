package techpick.api.application.event.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import techpick.api.application.event.dto.EventApiRequest;
import techpick.api.domain.link.service.LinkService;
import techpick.core.event.EventMessenger;
import techpick.core.event.events.PickViewEvent;
import techpick.core.event.events.SharedFolderLinkViewEvent;
import techpick.security.annotation.LoginUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
@Tag(name = "이벤트 수집 API", description = "발생하는 이벤트를 이곳으로 보내줘야 집계에 반영됩니다.")
public class EventApiController {

	private final EventMessenger eventMessenger;
	private final LinkService linkService;

	/**
	 * @author minkyeu kim
	 * [사용자 인증 정보가 필요한 api]
	 * 내가 나의 북마크를 클릭했을 때 프론트엔드가 보내는 이벤트
	 */
	@PostMapping("/picks/view")
	@Operation(
		summary = "사용자 자신의 픽 조회 이벤트 수집",
		description = "[로그인 필요] 서버에게 사용자 자신의 북마크 조회를 알립니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "전송 성공")
	})
	public ResponseEntity<Void> bookmarkView(
		@Valid @RequestBody EventApiRequest.BookmarkView request,
		@LoginUserId Long userId
	) {
		eventMessenger.send(new PickViewEvent(userId, request.pickId(), request.url()));
		return ResponseEntity.noContent().build();
	}

	/**
	 * @author minkyeu kim
	 * [공개 api]
	 * 공유된 폴더에서 링크를 클릭했을 때 프론트엔드가 보내는 이벤트
	 */
	@PostMapping("/shared/view")
	@Operation(
		summary = "공개 폴더의 북마크 조회 이벤트 수집",
		description = "[인증 불필요] 서버에게 공개 폴더의 어떤 북마크가 조회됬는지 알립니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "전송 성공")
	})
	public ResponseEntity<Void> sharedFolderLinkView(
		@Valid @RequestBody EventApiRequest.SharedBookmarkView request
	) {
		var linkInfo = linkService.getLinkInfo(request.url()); // 서버에 링크 엔티티가 존재해야 이벤트 전송 가능
		eventMessenger.send(new SharedFolderLinkViewEvent(request.url(), request.folderAccessToken()));
		return ResponseEntity.noContent().build();
	}
}
