package baguni.domain.infrastructure.user.dto;

import baguni.domain.model.user.Role;
import baguni.domain.model.user.User;
import baguni.domain.model.util.IDToken;

public record UserInfo(
	Long id,
	String name,
	IDToken idToken,
	String email,
	Role role
) {
	public static UserInfo from(User user) {
		return new UserInfo(user.getId(), user.getNickname(), user.getIdToken(), user.getEmail(), user.getRole());
	}
}
