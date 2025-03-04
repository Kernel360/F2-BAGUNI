package baguni.api.fixture;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import baguni.infra.model.user.SocialProvider;
import baguni.infra.model.util.IDToken;
import lombok.Builder;
import lombok.Getter;
import baguni.infra.model.user.Role;
import baguni.infra.model.user.User;

@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFixture {

	private Long id;

	private String nickname;

	private String email;

	private Role role;

	private String password;

	private SocialProvider socialProvider;

	private String socialProviderId;

	private List<Long> tagOrderList;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private IDToken idToken;

	public User get() {
		if (tagOrderList == null) {
			tagOrderList = new ArrayList<>();
		}
		idToken = IDToken.makeNew();
		ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue(this, User.class);
	}
}
