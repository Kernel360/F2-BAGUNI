package baguni.security.config;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import baguni.common.util.RequestLoggingFilter;
import baguni.security.filter.TestUserAuthenticationFilter;
import baguni.security.handler.BaguniOAuth2FlowFailureHandler;
import baguni.security.handler.BaguniApiAuthExceptionEntrypoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import baguni.security.filter.TokenAuthenticationFilter;
import baguni.security.handler.OAuth2SuccessHandler;
import baguni.security.handler.BaguniLogoutHandler;
import baguni.security.repository.BaguniAuthorizationRequestRepository;
import baguni.security.service.CustomOAuth2Service;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

	private final TokenAuthenticationFilter tokenAuthenticationFilter;
	private final TestUserAuthenticationFilter testUserAuthenticationFilter;
	private final RequestLoggingFilter requestLoggingFilter;

	private final CustomOAuth2Service customOAuth2Service;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final BaguniLogoutHandler logoutHandler;
	private final BaguniOAuth2FlowFailureHandler loginFailureHandler;
	private final BaguniAuthorizationRequestRepository requestRepository;
	private final BaguniApiAuthExceptionEntrypoint authExceptionEntrypoint;

	private final SecurityProperties properties;

	/* ********************************************
	 *       PRODUCTION SECURITY SETTING
	 * ********************************************/
	@Bean
	@Profile({"prod"})
	public SecurityFilterChain ProductionConfig(HttpSecurity http) throws Exception {
		log.info("운영 환경 SECURITY 설정을 적용 합니다. \n[#SecurityConfig.ProductionConfig]");
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(config -> {
				config.logoutUrl("/api/logout")
					  .addLogoutHandler(logoutHandler)
					  .logoutSuccessHandler(logoutHandler);
			})
			.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(
				authRequest -> authRequest
					.requestMatchers(HttpMethod.GET, "/api/shared/{uuid}").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/events/shared/**").permitAll() // 이벤트는 shared의 경우 검증 X
					.requestMatchers("/api/login/**").permitAll()
					.requestMatchers("/api/links").permitAll()
					.anyRequest().authenticated()
			)
			.exceptionHandling((configurer ->
				configurer.defaultAuthenticationEntryPointFor(
					authExceptionEntrypoint,
					new AntPathRequestMatcher("/api/**")
				)
			))
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

	/* ******************************************************
	 *       DEVELOPMENT + STAGING SECURITY SETTING
	 * ******************************************************
	 * (1)  테스트 자동화를 위한 API KEY 필터 추가
	 *      - testUserAuthenticationFilter
	 *
	 * (2)  Swagger 경로는 모두 허용
	 */
	@Bean
	@Profile({"local", "dev", "staging"})
	public SecurityFilterChain DevelopmentConfig(HttpSecurity http) throws Exception {
		log.info("로컬/개발/스테이징 환경 SECURITY 설정을 적용 합니다 \n[#SecurityConfig.DevelopmentConfig]");
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(config -> {
				config.logoutUrl("/api/logout")
					  .addLogoutHandler(logoutHandler)
					  .logoutSuccessHandler(logoutHandler);
			})
			.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(testUserAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(
				authRequest -> authRequest
					.requestMatchers(HttpMethod.GET, "/api/shared/{uuid}").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/events/shared/**").permitAll() // 이벤트는 shared의 경우 검증 X
					.requestMatchers("/api/development/**").permitAll() // 개발 환경 전용
					.requestMatchers("/api-docs/**").permitAll()
					.requestMatchers("/swagger-ui/**").permitAll()
					.requestMatchers("/api/login/**").permitAll()
					.requestMatchers("/api/links").permitAll()
					.anyRequest().authenticated()
			)
			.exceptionHandling((configurer ->
				configurer.defaultAuthenticationEntryPointFor(
					authExceptionEntrypoint,
					new AntPathRequestMatcher("/api/**")
				)
			))
			.oauth2Login(
				oauth -> oauth
					.authorizationEndpoint(authorization -> authorization
						.baseUri("/api/login")
						.authorizationRequestRepository(requestRepository)
					)
					.redirectionEndpoint(
						redirection -> redirection
							.baseUri("/api/login/oauth2/code/*")
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