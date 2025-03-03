package baguni.domain.model.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import baguni.domain.model.util.IDToken;
import baguni.domain.model.util.IDTokenConverter;
import baguni.domain.model.util.OrderConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import baguni.domain.model.common.BaseEntity;

@Table(name = "user")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	// 이메일 (WARN: 두 소셜 로그인이 같은 이메일을 가질 수 있음)
	@Column(name = "email", nullable = false)
	private String email;

	// 유저 권한
	@Column(name = "role", nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role;

	// 유저 식별 토큰 ( Unique )
	@Convert(converter = IDTokenConverter.class)
	@Column(name = "id_token", nullable = false, columnDefinition = "char(36)", unique = true)
	private IDToken idToken;

	// 닉네임 (없으면 랜덤 생성 - Ex. "노래하는피치#145")
	@Column(name = "nickname")
	private String nickname;

	// 일반 로그인만 해당 ----------------------------------
	// 비밀번호 (소셜 로그인 사용자는 null)
	@Column(name = "password") // nullable
	private String password;

	// 소셜 로그인만 해당 ----------------------------------
	// 소셜 제공자 (null일 경우 자체 가입 회원)
	@Enumerated(EnumType.STRING)
	@Column(name = "social_provider") // nullable
	private SocialProvider socialProvider;

	// 소셜 제공자 Id (null일 경우 자체 가입 회원)
	@Column(name = "social_provider_id") // nullable
	private String socialProviderId;
	// -------------------------------------------------

	// 유저의 tag id들을 공백으로 분리된 String으로 변환하여 db에 저장
	// ex) [6,3,2,23,1] -> "6 3 2 23 1"
	@Convert(converter = OrderConverter.class)
	@Column(name = "tag_order", columnDefinition = "longblob", nullable = false)
	private List<Long> tagOrderList = new ArrayList<>();

	public void updateTagOrderList(Long id, int destination) {
		tagOrderList.remove(id);
		int calculatedDestination = Math.min(destination, tagOrderList.size());
		tagOrderList.add(calculatedDestination, id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof User user)) {
			return false;
		}
		return Objects.equals(id, user.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Builder
	private User(
		SocialProvider socialProvider,
		String socialProviderId,
		String nickname,
		String password,
		String email,
		Role role,
		IDToken idToken,
		List<Long> tagOrderList
	) {
		this.socialProviderId = socialProviderId;
		this.socialProvider = socialProvider;
		this.nickname = nickname;
		this.password = password;
		this.email = email;
		this.role = role;
		this.idToken = idToken;
		this.tagOrderList = tagOrderList;
	}

	public static User SocialUser(SocialProvider socialProvider, String socialProviderId, String email) {
		return User
			.builder()
			.socialProvider(socialProvider)
			.socialProviderId(socialProviderId)
			.email(email)
			.idToken(IDToken.makeNew())
			.role(Role.ROLE_USER)
			.build();
	}

	public static User TestUser(String name, String email) {
		return User
			.builder()
			.nickname(name)
			.email(email)
			.idToken(IDToken.makeNew())
			.role(Role.ROLE_TEST)
			.build();
	}
}
