package baguni.api.application.link.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import baguni.infra.infrastructure.link.dto.LinkInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import baguni.api.application.link.dto.LinkApiMapper;
import baguni.api.application.link.dto.LinkApiResponse;
import baguni.api.service.link.service.LinkService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/links")
@Tag(name = "링크 API", description = "링크 관련 API")
public class LinkApiController {

	private final LinkService linkService;
	private final LinkApiMapper linkApiMapper;

	@GetMapping
	@Operation(summary = "링크 정보 조회", description = "해당 링크의 데이터를 DB에서 가져옵니다. 해당 메서드에서 더 이상 스크래핑하지 않습니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "조회 성공")
	})

	public ResponseEntity<LinkApiResponse> getLinkData(
		@Parameter(description = "og 태그 데이터 가져올 url") @RequestParam String url
	) {
		LinkInfo linkInfo = linkService.getLinkInfo(url);
		var response = linkApiMapper.toLinkResponse(linkInfo);
		return ResponseEntity.ok(response);
	}
}
