package baguni.api.fixture;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import baguni.domain.model.user.SocialProvider;
import lombok.Builder;
import lombok.Getter;
import baguni.domain.model.user.Role;
import baguni.domain.model.user.User;

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

	private LocalDateTime deletedAt;

	private List<Long> tagOrderList;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	public User get() {
		if (tagOrderList == null) {
			tagOrderList = new ArrayList<>();
		}
		ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue(this, User.class);
	}
}
