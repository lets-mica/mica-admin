package net.dreamlu.mica.admin.framework.security.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.framework.security.auth.SecWebAuthDetailsSource;
import net.dreamlu.mica.admin.framework.security.service.SecUserDetailsService;
import net.dreamlu.mica.admin.framework.security.utils.SecurityUtil;
import net.dreamlu.mica.admin.framework.vo.TokenVo;
import net.dreamlu.mica.core.result.R;
import net.dreamlu.mica.core.utils.StringUtil;
import net.dreamlu.mica.core.utils.WebUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * token过滤器
 *
 * @author L.cm
 */
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
	private final JwtTokenStore jwtTokenStore;
	private final JwtTokenService jwtTokenService;
	private final SecUserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
		// 解析 token
		String token = jwtTokenService.getToken(request);
		if (StringUtil.isBlank(token)) {
			chain.doFilter(request, response);
			return;
		}
		TokenVo tokenVo = jwtTokenStore.get(token);
		if (tokenVo == null) {
			// jwt token 解析错误 401
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			WebUtil.renderJson(response, R.fail("请重新登录"));
			return;
		}
		// 判断 token 是否存在
		String subject;
		try {
			subject = jwtTokenService.getSubject(token);
		} catch (JWTVerificationException | IllegalArgumentException e) {
			// jwt token 解析错误 401
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			WebUtil.renderJson(response, R.fail("请重新登录"));
			return;
		}
		if (subject != null && SecurityUtil.getAuthentication() == null) {
			AuthUser authUser = (AuthUser) userDetailsService.loadUserByUsername(subject);
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
			authenticationToken.setDetails(new SecWebAuthDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		}
		chain.doFilter(request, response);
	}

}
