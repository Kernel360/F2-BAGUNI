package baguni.api.application.suggestion.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import baguni.api.service.link.service.LinkService;
import baguni.infra.infrastructure.link.dto.BlogLinkInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/suggestion")
@Tag(name = "추천/소개 API", description = "유명 블로그 글 소개")
public class BlogArticleApiController {

	private final LinkService linkService;

	@GetMapping("/blog-articles")
	@Operation(
		summary = "최근 유명 블로그 게시글 획득 (15개)",
		description = "최근 유명 블로그 게시글 리스트 15개를 획득합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "조회 성공")
	})
	public ResponseEntity<List<BlogLinkInfo>> getRecentBlogArticles() {
		var result = linkService.getRssLinkList(15);
		return ResponseEntity.ok(result);
	}
}
