package baguni.security.util;

import java.util.Optional;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import baguni.security.config.SecurityProperties;

@Component
@RequiredArgsConstructor
public class CookieUtil {

	private final SecurityProperties properties;

	/**
	 * response에 쿠키를 등록하는 메소드
	 * 쿠키의 도메인은 application-security.yaml에서 읽어와서 설정
	 *
	 * @author Gyaak
	 *
	 * @param response 쿠키를 추가하려는 응답
	 * @param name 쿠키 이름
	 * @param value 쿠키 값
	 * @param maxAge 쿠키 유효기간
	 * @param httpOnly httpOnly 설정 : true / false
	 *
	 * */
	public void addCookie(
		HttpServletResponse response,
		String name,
		String value,
		int maxAge,
		boolean httpOnly
	) {
		ResponseCookie responseCookie = ResponseCookie.from(name, value)
													  .maxAge(maxAge)
													  .path("/")
													  .httpOnly(httpOnly)
													  .secure(true)
													  .domain(properties.getCookieDomain())
													  .build();
		response.addHeader("Set-Cookie", responseCookie.toString());

	}

	/**
	 * 쿠키 삭제를 위한 메소드
	 * 삭제하려는 쿠키를 덮어씌워 삭제함
	 * @author Gyaak
	 *
	 * @param request
	 * @param response
	 * @param name 삭제하려는 쿠키 이름
	 */
	public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		this.addCookie(response, name, "", 0, true);
	}

	public Optional<String> findCookieValue(Cookie[] cookies, String name) {
		if (cookies == null)
			return Optional.empty();

		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName()) && !cookie.getValue().isEmpty()) {
				return Optional.of(cookie.getValue());
			}
		}
		return Optional.empty();
	}
}
