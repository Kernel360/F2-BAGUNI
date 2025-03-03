package baguni.api.application.user.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import baguni.BaguniApiApplication;
import baguni.api.application.user.controller.dto.UserApiMapper;
import baguni.api.application.user.controller.dto.UserApiMapperImpl;
import baguni.api.service.user.service.UserService;
import baguni.infra.infrastructure.user.dto.UserInfo;
import baguni.infra.model.util.IDToken;
import baguni.security.util.CookieUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = BaguniApiApplication.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc // MockMvc 사용을 위한 설정
@Import(UserApiMapperImpl.class) // @SpyBean 구현체 인식을 못하여 추가
@DisplayName("유저 컨트롤러 - 슬라이스 테스트")
class UserApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SecurityFilterChain securityFilterChain; // 실제 시큐리티 구현체 사용하지 않기 위함.

	@MockBean
	private UserService userService;

	@SpyBean
	private UserApiMapper userApiMapper;

	@MockBean
	private CookieUtil cookieUtil;

	Long userId;
	IDToken idToken;
	UserInfo userInfo;

	@BeforeEach
	void setUp() {
		userId = 1L;

		idToken = IDToken.fromString(UUID.randomUUID().toString());
		var authentication = new UsernamePasswordAuthenticationToken(idToken, null,
			List.of(new SimpleGrantedAuthority("ROLE_USER")));

		// @LoginUserId에서 사용하므로 임의의 데이터 세팅
		SecurityContextHolder.getContext().setAuthentication(authentication);

		userInfo = new UserInfo(userId, "user", idToken, "email@example.com");

		// 반드시 있어야 합니다. userInfo 객체를 인식합니다.
		given(userService.getUserInfoByToken(idToken)).willReturn(userInfo);
	}

	@AfterEach
	void cleanUp() {
		// 설정했던 시큐리티 데이터 제거
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("회원 탈퇴")
	void delete_user() throws Exception {
		// given

		// when
		mockMvc.perform(delete("/api/users")
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isNoContent());

		// then
		then(userService).should(times(1)).deleteUser(userId);
		then(cookieUtil).should(times(1)).clearCookies(any());
	}

	@Test
	@DisplayName("유저 정보 조회")
	void get_user() throws Exception {
		// given
		given(userService.getUserInfoById(userId)).willReturn(userInfo);

		// when
		mockMvc.perform(get("/api/users")
				   .header("Authorization", "test-token")
				   .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isOk());

		// then
		then(userService).should(times(1)).getUserInfoById(userId);
		then(userApiMapper).should(times(1)).toApiResponse(userInfo);

		userApiMapper.toApiResponse(null);
	}
}