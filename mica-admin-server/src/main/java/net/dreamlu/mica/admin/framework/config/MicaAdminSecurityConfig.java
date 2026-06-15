package net.dreamlu.mica.admin.framework.config;

import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.security.auth.SecAuthHandler;
import net.dreamlu.mica.admin.framework.security.auth.SecAuthenticationProvider;
import net.dreamlu.mica.admin.framework.security.auth.SecWebAuthDetailsSource;
import net.dreamlu.mica.admin.framework.security.jwt.JwtAuthenticationTokenFilter;
import net.dreamlu.mica.admin.framework.security.service.SecUserDetailsService;
import net.dreamlu.mica.captcha.service.ICaptchaService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Spring Security 权限控制
 *
 * @author L.cm
 */
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MicaAdminSecurityProperties.class)
public class MicaAdminSecurityConfig {
	private final SecUserDetailsService userDetailsService;
	private final SecAuthHandler authHandler;
	private final ICaptchaService captchaService;
	private final SecWebAuthDetailsSource authDetailsSource;
	private final MicaAdminSecurityProperties properties;
	private final CacheManager cacheManager;
	private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) {
		// 关闭 csrf、iframe、session
		http
			.csrf(AbstractHttpConfigurer::disable)
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		// 放行路由
		http
			.authorizeHttpRequests(auth -> auth
				// 放行 options 方法
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				// 放行的路由
				.requestMatchers(properties.getPermitAll().toArray(new String[0])).permitAll()
				.anyRequest().authenticated()
			)
			.exceptionHandling(ex -> ex.accessDeniedHandler(authHandler));
		// 登录、登出端点
		http
			.formLogin(form -> form
				.loginPage("/")
				.loginProcessingUrl("/api/session")
				.failureHandler(authHandler)
				.successHandler(authHandler)
				.authenticationDetailsSource(authDetailsSource)
				.permitAll()
			)
			.logout(logout -> logout
				.logoutUrl("/api/logout")
				.clearAuthentication(false)
				.logoutSuccessHandler(authHandler)
				.logoutSuccessUrl("/")
			);
		// jwt 认证的 filter
		http.addFilterAt(jwtAuthenticationTokenFilter, BasicAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
		ProviderManager providerManager = new ProviderManager(authenticationProvider);
		// 与原 WebSecurityConfigurerAdapter#configure(AuthenticationManagerBuilder) 中 eraseCredentials(false) 行为保持一致
		providerManager.setEraseCredentialsAfterAuthentication(false);
		return providerManager;
	}

	@Bean
	public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
		SecAuthenticationProvider authProvider = new SecAuthenticationProvider(
			userDetailsService, properties, captchaService, cacheManager
		);
		authProvider.setPasswordEncoder(passwordEncoder);
		return authProvider;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(4);
	}

}
