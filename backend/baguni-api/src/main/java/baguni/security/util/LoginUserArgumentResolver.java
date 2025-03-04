package baguni.security.util;

import java.util.Objects;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import baguni.api.service.user.service.UserService;
import baguni.infra.model.util.IDToken;
import baguni.security.annotation.LoginUserId;
import baguni.security.exception.SecurityException;
import baguni.security.exception.AuthErrorCode;
import lombok.RequiredArgsConstructor;

/**
 * @author minkyeu kim
 * AccessToken에 들어있는 IdToken을 이용해서 사용자의 진짜 id 값을 찾고,
 * 그것을 컨트롤러의 @LoginUserId 파라미터에 넣어줍니다.
 */
@Component
@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

	private final UserService userService;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(LoginUserId.class) &&
			Long.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(
		MethodParameter parameter,
		ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest,
		WebDataBinderFactory binderFactory
	) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (Objects.isNull(authentication) || !authentication.isAuthenticated()) {
			throw new SecurityException(AuthErrorCode.AUTH_INVALID_AUTHENTICATION);
		}

		var principal = authentication.getPrincipal();
		if (false == (principal instanceof IDToken)) {
			throw new SecurityException(AuthErrorCode.AUTH_INVALID_AUTHENTICATION);
		}

		return userService.getUserInfoByToken((IDToken)principal).id();
	}
}
