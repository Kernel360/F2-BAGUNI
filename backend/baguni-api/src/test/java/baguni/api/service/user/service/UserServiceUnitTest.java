package baguni.api.service.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import baguni.api.fixture.UserFixture;
import baguni.domain.infrastructure.user.UserDataHandler;
import baguni.domain.infrastructure.folder.FolderDataHandler;
import baguni.domain.model.user.User;
import baguni.domain.model.util.IDToken;
import baguni.domain.model.util.IdTokenConversionException;
import baguni.security.exception.ApiAuthException;
import baguni.security.model.OAuth2UserInfo;

@DisplayName("유저 서비스 - 단위 테스트")
@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

	@Mock
	private UserDataHandler userDataHandler;

	@Mock
	private FolderDataHandler folderDataHandler;

	@InjectMocks
	private UserService userService;

	Map<String, Object> attributes;
	OAuth2UserInfo userInfo;
	User user;

	@BeforeEach
	void setUp() {
		attributes = Map.of(
			"email", "user@example.com",
			"name", "Test User"
		);
		userInfo = new OAuth2UserInfo("google", attributes);
		user = UserFixture
			.builder().id(1L).email("user@example.com").nickname("Test User").build().get();
	}

	@Test
	@DisplayName("유저 생성")
	void create_user() {
		// given
		given(userDataHandler.createSocialUser(
			userInfo.getProvider(),
			userInfo.getProviderId(),
			userInfo.getEmail()
		)).willReturn(user);

		// when
		userService.createSocialUser(userInfo);

		// then
		then(folderDataHandler).should(times(1)).createMandatoryFolder(user);
	}

	@Test
	@DisplayName("유저 생성 실패 - 잘못된 소셜 타입")
	void invalid_social_type() {
		// given
		// when, then
		assertThatThrownBy(() -> new OAuth2UserInfo("fail", attributes))
			.isInstanceOf(ApiAuthException.class)
			.hasMessageStartingWith(ApiAuthException.SOCIAL_TYPE_INVALID().getMessage());
	}

	@Test
	@DisplayName("유저 생성 실패")
	void fail_create_user() {
		// given
		given(userDataHandler.createSocialUser(
			userInfo.getProvider(),
			userInfo.getProviderId(),
			userInfo.getEmail()
		)).willReturn(user);
		willThrow(new RuntimeException("폴더 생성 실패"))
			.given(folderDataHandler).createMandatoryFolder(user);

		// when, then
		assertThatThrownBy(() -> userService.createSocialUser(userInfo))
			.isInstanceOf(ApiAuthException.class)
			.hasMessageStartingWith(ApiAuthException.AUTHENTICATION_SERVER_FAILURE().getMessage());
	}

	@Test
	@DisplayName("유저 존재 여부 확인")
	void exist_user() {
		// given
		given(userDataHandler.findSocialUser(userInfo.getProvider(), userInfo.getProviderId())).willReturn(
			Optional.of(user));

		// when
		userService.isSocialUserExists(userInfo);

		// then
		then(userDataHandler).should(times(1)).findSocialUser(userInfo.getProvider(), userInfo.getProviderId());
	}

	@Test
	@DisplayName("IDToken으로 유저 조회")
	void get_idToken_user() {
		// given
		IDToken idToken = IDToken.makeNew();
		given(userDataHandler.getUser(idToken)).willReturn(user);

		// when
		userService.getUserInfoByToken(idToken);

		// then
		then(userDataHandler).should(times(1)).getUser(idToken);
	}

	@Test
	@DisplayName("IDToken 예외 테스트")
	void idToken_test() {
		// given
		String invalidRaw = "invalid-uuid-format"; // UUID 형식이 아님

		// when, then
		assertThatThrownBy(() -> IDToken.fromString(invalidRaw))
			.isInstanceOf(IdTokenConversionException.class);
	}

	@Test
	@DisplayName("userId로 유저 조회")
	void get_userId_user() {
		// given
		given(userDataHandler.getUser(user.getId())).willReturn(user);

		// when
		userService.getUserInfoById(user.getId());

		// then
		then(userDataHandler).should(times(1)).getUser(user.getId());
	}

	@Test
	@DisplayName("유저 삭제")
	void delete_user() {
		// given

		// when
		userService.deleteUser(user.getId());

		// then
		then(userDataHandler).should(times(1)).deleteUser(user.getId());
	}

}