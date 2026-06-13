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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Spring Security 权限控制
 *
 * @author L.cm
 */
@EnableWebSecurity
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(MicaAdminSecurityProperties.class)
public class MicaAdminSecurityConfig extends WebSecurityConfigurerAdapter {
	private final SecUserDetailsService userDetailsService;
	private final SecAuthHandler authHandler;
	private final ICaptchaService captchaService;
	private final SecWebAuthDetailsSource authDetailsSource;
	private final MicaAdminSecurityProperties properties;
	private final CacheManager cacheManager;
	private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
	private final ApplicationContext applicationContext;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		// 关闭 csrf、iframe、session
		http.csrf()
			.disable()
			.headers()
			.frameOptions()
			.disable()
			.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		http.authorizeRequests()
			// 放行 options 方法
			.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
			// 放行的路由
			.antMatchers(properties.getPermitAll().toArray(new String[0])).permitAll()
			.anyRequest()
			.authenticated()
			.and()
			.exceptionHandling()
			.accessDeniedHandler(authHandler);

		http.formLogin()
			.loginPage("/")
			.loginProcessingUrl("/api/session")
			.failureHandler(authHandler)
			.successHandler(authHandler)
			.authenticationDetailsSource(authDetailsSource)
			.permitAll()
			.and()
			.logout()
			.logoutUrl("/api/logout")
			.clearAuthentication(false)
			.logoutSuccessHandler(authHandler)
			.logoutSuccessUrl("/");

		// jwt 认证的 filter
		http.addFilterAt(jwtAuthenticationTokenFilter, BasicAuthenticationFilter.class);
		// @formatter:on
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(applicationContext.getBean(SecAuthenticationProvider.class));
		auth.eraseCredentials(false);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public SecAuthenticationProvider authProvider() {
		final SecAuthenticationProvider authProvider = new SecAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setUserDetailsPasswordService(userDetailsService);
		authProvider.setCaptchaService(captchaService);
		authProvider.setMicaSecurityProperties(properties);
		authProvider.setCacheManager(cacheManager);
		authProvider.setPasswordEncoder(applicationContext.getBean(PasswordEncoder.class));
		return authProvider;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(4);
	}

}
