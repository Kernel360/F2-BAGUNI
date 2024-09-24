package kernel360.techpick.core.auth.service;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import kernel360.techpick.core.auth.model.OAuth2UserInfo;
import kernel360.techpick.core.config.OAuth2AttributeConfigProvider;
import kernel360.techpick.core.feature.user.User;
import kernel360.techpick.feature.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2Service extends DefaultOAuth2UserService {

	private final UserRepository userRepository;
	private final OAuth2AttributeConfigProvider configProvider;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		String provider = userRequest.getClientRegistration().getRegistrationId();
		var oAuth2User = super.loadUser(userRequest);
		Map<String, Object> attributes = getAttributes(oAuth2User, provider);
		var oAuth2UserInfo = new OAuth2UserInfo(provider, attributes);

		if (!userRepository.existsBySocialProviderId(oAuth2UserInfo.getName())) {
			saveOAuth2UserInfo(oAuth2UserInfo);
		}
		return oAuth2UserInfo;
	}

	private Map<String, Object> getAttributes(OAuth2User oAuth2User, String provider) {
		Map<String, String> config = configProvider.getAttributeConfig(provider);
		Map<String, Object> attributes = new HashMap<>();
		for (String key : config.keySet()) {
			var value = searchAttribute(config.get(key), oAuth2User.getAttributes());
			attributes.put(key, value);
		}
		return attributes;
	}

	// TODO: 응답 body 에서 직접 값을 받아오는 형식으로 리팩토링 필요
	// BFS 로 nested map 구조를 탐색
	private Object searchAttribute(String targetKey, Map<String, Object> map) {
		Queue<Map<String, Object>> queue = new ArrayDeque<>();
		queue.add(map);
		while (!queue.isEmpty()) {
			var curMap = queue.poll();
			for (String key : curMap.keySet()) {
				Object value = curMap.get(key);
				if (key.equals(targetKey)) {
					return value;
				} else if (value instanceof Map<?, ?>) {
					queue.add((Map<String, Object>)value);
				}
			}
		}
		// TODO: ApiUserException 으로 리팩토링 예정
		throw new IllegalArgumentException("Attribute of " + targetKey + " is not found");
	}

	private void saveOAuth2UserInfo(OAuth2UserInfo oAuth2UserInfo) {
		User user = User.create(oAuth2UserInfo);
		userRepository.save(user);
	}
}
