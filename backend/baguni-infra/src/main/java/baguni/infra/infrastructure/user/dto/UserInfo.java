package baguni.infra.infrastructure.user.dto;

import baguni.infra.model.user.Role;
import baguni.infra.model.user.User;
import baguni.infra.model.util.IDToken;

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
