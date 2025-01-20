package baguni.api.application.chromebookmark.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import baguni.common.event.events.LinkCrawlingEvent;
import baguni.common.event.messenger.CrawlingEventMessenger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import baguni.api.service.chromebookmark.dto.ChromeImportResult;
import baguni.api.service.chromebookmark.service.ChromeBookmarkService;
import baguni.domain.infrastructure.folder.dto.FolderCommand;
import baguni.security.annotation.LoginUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chrome")
@Tag(name = "Chrome API", description = "Chrome Bookmark Import / Export API")
public class ChromeBookmarkController {

	private final ChromeBookmarkService chromeBookmarkService;
	private final CrawlingEventMessenger crawlingEventMessenger;

	@GetMapping("/{folderId}/export")
	@Operation(summary = "특정 폴더 다운로드", description = "사용자의 특정 폴더를 크롬 브라우저 북마크에 import 가능한 형태로 다운로드 받습니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "다운로드 성공"),
		@ApiResponse(responseCode = "401", description = "본인 폴더만 다운로드할 수 있습니다.")
	})
	public ResponseEntity<ByteArrayResource> exportFolder(@LoginUserId Long userId, @PathVariable Long folderId) {
		String exportResult = chromeBookmarkService.exportFolder(new FolderCommand.Export(userId, folderId));

		ByteArrayResource resource = new ByteArrayResource(exportResult.getBytes());

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=exported-folder.html");
		headers.add(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8");

		return ResponseEntity
			.status(HttpStatus.OK)
			.headers(headers)
			.body(resource);
	}

	@GetMapping("/export")
	@Operation(summary = "전체 폴더 다운로드", description = "사용자의 특정 폴더를 크롬 브라우저 북마크에 import 가능한 형태로 다운로드 받습니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "다운로드 성공")
	})
	public ResponseEntity<ByteArrayResource> exportUserFolder(@LoginUserId Long userId) {
		String exportResult = chromeBookmarkService.exportUserFolder(userId);

		ByteArrayResource resource = new ByteArrayResource(exportResult.getBytes());

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=exported-folder.html");
		headers.add(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8");

		return ResponseEntity
			.status(HttpStatus.OK)
			.headers(headers)
			.body(resource);
	}

	@PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces =
		MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "크롬 북마크 업로드", description = "내보내기한 크롬 북마크(.html)을 업로드 하여 일괄 추가합니다. 이미 등록된 url(중복 url)을 응답으로 "
		+ "보냅니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "다운로드 성공"),
		@ApiResponse(responseCode = "500", description = "파일 형식 및 파싱 오류")
	})
	public ResponseEntity<List<String>> importBookmark(@LoginUserId Long userId, @RequestParam("file")
	@Parameter(required = true) MultipartFile file) throws
		InterruptedException, IOException {
		String html = new String(file.getBytes(), StandardCharsets.UTF_8);
		ChromeImportResult result = chromeBookmarkService.importChromeBookmarks(userId, html);

		// og 태그의 경우 정적 크롤링이 필요해, 최초 등록시에는 og 태그를 모두 빈스트링("")으로 등록하고,
		// 이후 비동기적으로 업데이트 진행
		int maxThreadPoolSize = 5;
		ExecutorService executor = Executors.newFixedThreadPool(maxThreadPoolSize);
		for (String url : result.ogTagUpdateUrls()) {
			CompletableFuture.runAsync(() -> crawlingEventMessenger.send(new LinkCrawlingEvent(url)), executor)
							 .orTimeout(60, TimeUnit.SECONDS);
		}

		return ResponseEntity.ok(result.alreadyExistBookmarks());
	}
}
