package baguni.common.util;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 요청을 로깅하기 위한 Filter<br>
 * UUID 를 MDC에 저장하여 Logback에서 활용<br>
 * 요청 내용을 RequestHolder에 저장
 * @author psh
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

	private final RequestHolder requestHolder;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		try {
			CachedHttpServletRequest cachedRequest = new CachedHttpServletRequest(request);

			// TODO: 아래 로그로 남기고, Thread Local에 저장하는 Request Holder를 제거.
			log.info(
				"{} {} {} {}",
				cachedRequest.getMethod(), cachedRequest.getRequestURI(), cachedRequest.getQueryString(),
				cachedRequest.getRequestBody()
			);

			// TODO: 아래 부분 + try catch 삭제
			requestHolder.setRequest(cachedRequest);
			filterChain.doFilter(cachedRequest, response);
		} finally {
			// 요청이 마무리되면 무조건 ThreadLocal에 저장한 요청정보를 초기화
			requestHolder.clearRequest();
		}
	}
}
