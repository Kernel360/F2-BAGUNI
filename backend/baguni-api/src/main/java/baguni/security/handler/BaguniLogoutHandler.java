package baguni.security.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import baguni.security.config.SecurityProperties;
import baguni.security.util.CookieUtil;

@Component
@RequiredArgsConstructor
public class BaguniLogoutHandler implements LogoutHandler, LogoutSuccessHandler {

	private final CookieUtil cookieUtil;
	private final SecurityProperties properties;

	@Override
	public void logout(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication
	) {
		cookieUtil.deleteCookie(request, response, properties.ACCESS_TOKEN_KEY);
		cookieUtil.deleteCookie(request, response, properties.LOGIN_FLAG_FOR_FRONTEND);
		cookieUtil.deleteCookie(request, response, "JSESSIONID");
	}

	@Override
	public void onLogoutSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication
	) {
		// do not redirect
		response.setStatus(HttpServletResponse.SC_OK);
	}
}