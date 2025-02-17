package baguni.security.filter;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import baguni.api.service.user.service.UserService;
import baguni.domain.infrastructure.user.dto.UserInfo;
import baguni.domain.model.user.Role;
import baguni.domain.model.util.IDToken;
import baguni.security.util.AuthHeaderTokenExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestUserAuthenticationFilter extends OncePerRequestFilter {

	private final UserService userService;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		var securityContext = SecurityContextHolder.getContext();
		try {
			AuthHeaderTokenExtractor.extractBearerToken(request)
									.map(IDToken::fromString)
									.map(userService::getUserInfoByToken)
									.filter(ONLY_TEST_USER)
									.map(this::mapToAuthenticationToken)
									.ifPresent(securityContext::setAuthentication);
		} catch (Exception e) {
			log.error("Test 유저용 ID 토큰 인증 실패. {}", e.getMessage(), e);
		} finally {
			filterChain.doFilter(request, response);
		}
	}

	private final Predicate<UserInfo> ONLY_TEST_USER = (userInfo) -> userInfo.role().equals(Role.ROLE_TEST);

	private UsernamePasswordAuthenticationToken mapToAuthenticationToken(UserInfo userInfo) {
		return new UsernamePasswordAuthenticationToken(
			userInfo.idToken(), this,
			List.of(new SimpleGrantedAuthority(userInfo.role().name()))
		);
	}
}
