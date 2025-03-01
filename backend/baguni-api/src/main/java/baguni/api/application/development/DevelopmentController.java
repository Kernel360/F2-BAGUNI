package baguni.api.application.development;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import baguni.api.application.user.controller.dto.UserApiMapper;
import baguni.api.application.user.controller.dto.UserInfoApiResponse;
import baguni.api.service.user.service.UserService;
import baguni.security.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 개발 전용 API를 제공하는 Controller입니다.
 */
@RestController
@RequiredArgsConstructor
@Profile({"local", "dev", "staging"})
@RequestMapping("/api/development")
@Tag(
	name = "개발/테스트 전용 API",
	description = """
		1. 인증 없이 사용 가능합니다.
		2. 운영 환경에선 해당 API가 비활성화됩니다.
		""")
public class DevelopmentController {

	private final CookieUtil cookieUtil;
	private final UserService userService;
	private final UserApiMapper userApiMapper;

	@PostMapping("/users")
	@Operation(summary = "테스트 회원 가입", description = "테스트용 회원을 생성합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "테스트 계정 생성 성공")
	})
	public ResponseEntity<UserInfoApiResponse> createTestUser() {
		var userInfo = userService.createTestUser();
		var response = userApiMapper.toApiResponse(userInfo);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/users")
	@Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 하면 모든 폴더, 픽, 태그가 삭제됩니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "회원 탈퇴 성공")
	})
	public ResponseEntity<Void> deleteUser(
		@Valid @RequestBody Long userId,
		HttpServletResponse response
	) {
		userService.deleteUser(userId);
		cookieUtil.clearCookies(response);
		return ResponseEntity.noContent().build();
	}
}
