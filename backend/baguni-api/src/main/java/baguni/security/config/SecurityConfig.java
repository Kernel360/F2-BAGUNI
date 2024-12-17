package baguni.security.config;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import baguni.security.handler.BaguniLoginFailureHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import baguni.security.filter.TokenAuthenticationFilter;
import baguni.security.handler.OAuth2SuccessHandler;
import baguni.security.handler.BaguniLogoutHandler;
import baguni.security.repository.BaguniAuthorizationRequestRepository;
import baguni.security.service.CustomOAuth2Service;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

	private final CustomOAuth2Service customOAuth2Service;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final TokenAuthenticationFilter tokenAuthenticationFilter;
	private final BaguniLogoutHandler logoutHandler;
	private final BaguniLoginFailureHandler loginFailureHandler;
	private final BaguniAuthorizationRequestRepository requestRepository;
	private final SecurityProperties properties;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		// TODO: 이후 설정 추가 필요
		http
			.csrf(AbstractHttpConfigurer::disable) // csrf 비활성화 시 logout 했을 때 GET 메서드로 요청됨. POST로만 보내도록 하기 위해 주석 처리
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(config -> {
				config.logoutUrl("/api/logout")
					  .addLogoutHandler(logoutHandler)
					  .logoutSuccessHandler(logoutHandler);
			})
			.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			// TokenAuthenticationFilter 를 UsernamePasswordAuthenticationFilter 앞에 추가
			.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(
				authRequest -> authRequest
					.requestMatchers(HttpMethod.GET, "/api/shared/{uuid}").permitAll()
					.requestMatchers(HttpMethod.GET, "/api/event/shared/**").permitAll() // 이벤트는 shared의 경우 검증 X
					.requestMatchers("/api-docs/**").permitAll()
					.requestMatchers("/swagger-ui/**").permitAll()
					.requestMatchers("/api/login/**").permitAll()
					.requestMatchers("/api/links").permitAll()
					.anyRequest().authenticated()
			)
			.oauth2Login(
				oauth -> oauth
					.authorizationEndpoint(authorization -> authorization
						.baseUri("/api/login") // /* 붙이면 안됨
						.authorizationRequestRepository(requestRepository)
					)
					.redirectionEndpoint(
						redirection -> redirection
							.baseUri("/api/login/oauth2/code/*")
						// 반드시 /* 으로 {registrationId}를 받아야 함 스프링 시큐리티의 문제!!
						// https://github.com/spring-projects/spring-security/issues/13251
					)
					.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2Service))
					.successHandler(oAuth2SuccessHandler)
					.failureHandler(loginFailureHandler)
			)
		;
		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowCredentials(true);
		config.setAllowedOrigins(List.of(properties.getBaseUrl()));
		config.setAllowedOriginPatterns(properties.getCorsPatterns());
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("*"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}