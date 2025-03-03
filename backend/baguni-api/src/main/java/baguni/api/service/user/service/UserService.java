package baguni.api.service.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import baguni.domain.infrastructure.folder.FolderDataHandler;
import baguni.domain.infrastructure.user.UserDataHandler;
import baguni.domain.infrastructure.user.dto.UserInfo;
import baguni.domain.model.util.IDToken;
import baguni.security.exception.SecurityException;
import baguni.security.exception.AuthErrorCode;
import baguni.security.model.OAuth2UserInfo;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserDataHandler userDataHandler;
	private final FolderDataHandler folderDataHandler;

	@WithSpan
	@Transactional
	public UserInfo createSocialUser(OAuth2UserInfo oAuthInfo) {
		var user = userDataHandler.createSocialUser(
			oAuthInfo.getProvider(),
			oAuthInfo.getProviderId(),
			oAuthInfo.getEmail()
		);
		try {
			folderDataHandler.createMandatoryFolder(user);
			return UserInfo.from(user);
		} catch (Exception e) {
			throw new SecurityException(AuthErrorCode.AUTH_SERVER_FAILURE);
		}
	}

	@Transactional
	public UserInfo createTestUser() {
		var user = userDataHandler.createTestUser();
		try {
			folderDataHandler.createMandatoryFolder(user);
			return UserInfo.from(user);
		} catch (Exception e) {
			throw new SecurityException(AuthErrorCode.AUTH_SERVER_FAILURE);
		}
	}

	@WithSpan
	@Transactional(readOnly = true)
	public boolean isSocialUserExists(OAuth2UserInfo oAuthInfo) {
		return userDataHandler.findSocialUser(oAuthInfo.getProvider(), oAuthInfo.getProviderId())
							  .isPresent();
	}

	@WithSpan
	@Transactional(readOnly = true)
	public UserInfo getUserInfoByToken(IDToken idToken) {
		var user = userDataHandler.getUser(idToken);
		return UserInfo.from(user);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public UserInfo getUserInfoById(Long userId) {
		var user = userDataHandler.getUser(userId);
		return UserInfo.from(user);
	}

	@WithSpan
	@Transactional
	public void deleteUser(Long userId) {
		userDataHandler.deleteUser(userId);
	}
}
