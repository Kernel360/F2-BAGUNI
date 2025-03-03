package baguni.domain.model.user;

// 로그인 타입
public enum SocialProvider {
	GOOGLE("google"),
	KAKAO("kakao"),
	;

	private final String provider;

	SocialProvider(String provider) {
		this.provider = provider;
	}

	public static SocialProvider of(String provider) throws IllegalArgumentException {
		for (SocialProvider socialProvider : SocialProvider.values()) {
			if (socialProvider.provider.equals(provider)) {
				return socialProvider;
			}
		}
		throw new IllegalArgumentException();
	}

	public String getName() {
		return this.provider;
	}
}
