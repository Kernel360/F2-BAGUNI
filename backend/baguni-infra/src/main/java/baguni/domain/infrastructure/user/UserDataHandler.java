package baguni.domain.infrastructure.user;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import baguni.common.exception.base.ServiceException;
import baguni.common.exception.error_code.UserErrorCode;
import baguni.domain.infrastructure.folder.FolderRepository;
import baguni.domain.infrastructure.pick.PickRepository;
import baguni.domain.infrastructure.pick.PickTagRepository;
import baguni.domain.infrastructure.sharedFolder.SharedFolderRepository;
import baguni.domain.infrastructure.tag.TagRepository;
import baguni.domain.model.user.Role;
import baguni.domain.model.user.SocialProvider;
import baguni.domain.model.util.IDToken;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import baguni.domain.model.user.User;

@Component
@RequiredArgsConstructor
public class UserDataHandler {

	private final UserRepository userRepository;
	private final FolderRepository folderRepository;
	private final SharedFolderRepository sharedFolderRepository;
	private final PickRepository pickRepository;
	private final PickTagRepository pickTagRepository;
	private final TagRepository tagRepository;

	@WithSpan
	@Transactional(readOnly = true)
	public User getUser(Long userId) {
		return userRepository.findById(userId)
							 .orElseThrow(() -> new ServiceException(UserErrorCode.USER_NOT_FOUND));
	}

	@WithSpan
	@Transactional(readOnly = true)
	public Optional<User> findSocialUser(SocialProvider socialProvider, String socialProviderId) {
		return userRepository.findBySocialProviderAndSocialProviderId(
			socialProvider, socialProviderId
		);
	}

	@WithSpan
	@Transactional(readOnly = true)
	public User getUser(IDToken token) {
		return userRepository.findByIdToken(token)
							 .orElseThrow(() -> new ServiceException(UserErrorCode.USER_NOT_FOUND, token.value()));
	}

	@WithSpan
	@Transactional
	public User createSocialUser(SocialProvider provider, String socialProviderId, String email) {
		return userRepository.save(
			User.SocialUser(provider, socialProviderId, email)
		);
	}

	@Transactional
	public User createTestUser() {
		var userName = "test" + userRepository.countByRole(Role.ROLE_TEST);
		return userRepository.save(
			User.TestUser(userName, userName + "@baguni.com")
		);
	}

	/**
	 * @author sangwon
	 * 회원 탈퇴 기능
	 */
	@WithSpan
	@Transactional
	public void deleteUser(Long userId) {
		List<Long> pickIdList = pickRepository.findIdAllByUserId(userId);
		pickTagRepository.deleteAllByPickList(pickIdList);
		pickRepository.deleteAllByIdInBatch(pickIdList);
		tagRepository.deleteByUserId(userId);
		sharedFolderRepository.deleteByUserId(userId);
		folderRepository.deleteByUserId(userId);
		userRepository.deleteById(userId);
	}
}
