package baguni.security.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import baguni.infra.infrastructure.user.UserDataHandler;
import baguni.security.config.JwtProperties;
import baguni.security.exception.AuthErrorCode;
import baguni.security.util.AccessToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import baguni.security.config.SecurityProperties;
import baguni.security.exception.SecurityException;
import baguni.security.model.OAuth2UserInfo;
import baguni.security.util.CookieUtil;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtProperties jwtProps;
	private final SecurityProperties securityProps;

	private final CookieUtil cookieUtil;
	private final UserDataHandler userDataHandler;

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication
	) throws IOException, ServletException {
		var auth = (OAuth2UserInfo)authentication.getPrincipal();
		var user = userDataHandler.findSocialUser(auth.getProvider(), auth.getProviderId())
								  .orElseThrow(
									  () -> new SecurityException(AuthErrorCode.AUTH_INVALID_AUTHENTICATION));
		var accessToken = AccessToken.makeNew(jwtProps, user.getIdToken(), user.getRole());

		assignNewAccessToken(response, accessToken);
		redirectUserToSuccessPage(request, response);

		super.clearAuthenticationAttributes(request);
		super.onAuthenticationSuccess(request, response, authentication);
	}

	private void redirectUserToSuccessPage(
		HttpServletRequest request,
		HttpServletResponse response
	) throws
		IOException {
		var successPage = cookieUtil.findCookieValue(request.getCookies(), securityProps.OAUTH_RETURN_URL_KEY)
									.orElse(securityProps.getDefaultRedirectUrl());

		cookieUtil.deleteCookie(response, securityProps.OAUTH_RETURN_URL_KEY);
		cookieUtil.deleteCookie(response, "JSESSIONID");
		response.sendRedirect(successPage);
	}

	/**
	 * 엑세스 토큰의 만료 기간 (exp) 와 쿠키의 만료 기간 (max age)를 동일하게 설정합니다.
	 */
	private void assignNewAccessToken(
		HttpServletResponse response,
		String token
	) {
		cookieUtil.deleteCookie(response, securityProps.ACCESS_TOKEN_KEY);
		cookieUtil.addCookie(
			response, securityProps.ACCESS_TOKEN_KEY, token,
			(int)AccessToken.EXPIRY_DAY.toSeconds(), true
		);
	}
}