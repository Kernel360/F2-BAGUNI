package baguni.security.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import baguni.infra.model.user.SocialProvider;
import baguni.security.exception.SecurityException;
import baguni.infra.model.user.Role;
import baguni.security.exception.AuthErrorCode;

public class OAuth2UserInfo implements OAuth2User {

	private final SocialProvider provider;
	private final Map<String, Object> attributes;

	public OAuth2UserInfo(String provider, Map<String, Object> attributes) {
		try {
			this.provider = SocialProvider.of(provider);
			this.attributes = attributes;
		} catch (IllegalArgumentException e) {
			throw new SecurityException(AuthErrorCode.AUTH_INVALID_AUTHENTICATION);
		}

	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(Role.ROLE_USER.toString()));
	}

	/**
	 * kakao 인증을 통했고, 해당 인증 제공자가 알려준 고객 id가 12345일 겨웅
	 * "kakao#12345"를 이름으로 사용하여 혹시 모를 id 중복을 방지한다.
	 */
	@Override
	public String getName() {
		var socialProviderId = attributes.get("name").toString();
		return provider.getName() + "#" + socialProviderId;
	}

	public String getEmail() {
		return this.attributes.get("email").toString();
	}

	public SocialProvider getProvider() {
		return this.provider;
	}

	public String getProviderId() {
		return this.attributes.get("name").toString();
	}
}