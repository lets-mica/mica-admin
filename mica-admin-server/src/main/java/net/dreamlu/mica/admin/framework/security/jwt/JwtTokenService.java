package net.dreamlu.mica.admin.framework.security.jwt;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.core.utils.Charsets;
import net.dreamlu.mica.core.utils.DateUtil;
import net.dreamlu.mica.core.utils.StringUtil;
import net.dreamlu.mica.admin.framework.config.MicaAdminSecurityProperties;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

/**
 * token 验证处理
 *
 * @author L.cm
 */
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class JwtTokenService implements SmartInitializingSingleton {
	/**
	 * 令牌前缀
	 */
	public static final String TOKEN_PREFIX = "Bearer ";
	private final MicaAdminSecurityProperties properties;
	private JwtParser jwtParser;

	/**
	 * 获取 token
	 *
	 * @param request HttpServletRequest
	 * @return token
	 */
	public String getToken(HttpServletRequest request) {
		MicaAdminSecurityProperties.JwtToken jwtToken = properties.getJwtToken();
		// 1. 获取请求头携带的令牌
		String tokenHeaderKey = jwtToken.getHeader();
		String token = request.getHeader(tokenHeaderKey);
		// 2. 获取参数传递的 token
		if (StringUtil.isBlank(token)) {
			token = request.getParameter(tokenHeaderKey);
		}
		// 3. 解析 Bearer token
		if (StringUtil.isNotBlank(token) && token.startsWith(TOKEN_PREFIX)) {
			token = token.substring(TOKEN_PREFIX.length());
		}
		// 4. 如果为空返回
		if (StringUtil.isBlank(token)) {
			return null;
		}
		return token;
	}

	/**
	 * 获取用户身份信息
	 *
	 * @return 用户信息
	 */
	public String getSubject(HttpServletRequest request) {
		String token = getToken(request);
		return getSubject(token);
	}

	/**
	 * 获取用户身份信息
	 *
	 * @return 用户信息
	 */
	public String getSubject(String token) {
		if (StringUtil.isBlank(token)) {
			return null;
		}
		// 注意此处 有几个异常需要处理
		Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
		Claims body = claimsJws.getBody();
		if (body == null) {
			return null;
		}
		return body.getSubject();
	}

	/**
	 * 创建令牌
	 *
	 * @param authUser   AuthUser
	 * @param now        当前时间
	 * @param expireTime 有效期
	 * @return 令牌
	 */
	public String createToken(AuthUser authUser, Date now, Duration expireTime) {
		// jwt token 配置信息
		MicaAdminSecurityProperties.JwtToken jwtToken = properties.getJwtToken();
		String secret = jwtToken.getSecret();
		SignatureAlgorithm algorithm = jwtToken.getSignatureAlgorithm();
		// 加密 key
		Key keySpec = new SecretKeySpec(secret.getBytes(Charsets.UTF_8), algorithm.getJcaName());
		return Jwts.builder()
			.setId(StringUtil.getUUID())
			.setAudience(jwtToken.getAudience())
			.setIssuer(jwtToken.getIssuer())
			.setIssuedAt(now)
			.setSubject(authUser.getUsername())
			.setNotBefore(now)
			.setExpiration(DateUtil.plus(now, expireTime))
			.signWith(keySpec, algorithm)
			.compact();
	}

	@Override
	public void afterSingletonsInstantiated() {
		// jwt token 配置信息
		MicaAdminSecurityProperties.JwtToken jwtToken = properties.getJwtToken();
		String secret = jwtToken.getSecret();
		SignatureAlgorithm algorithm = jwtToken.getSignatureAlgorithm();
		// 加密 key
		Key keySpec = new SecretKeySpec(secret.getBytes(Charsets.UTF_8), algorithm.getJcaName());
		// jwtParser
		jwtParser = Jwts.parserBuilder()
			.setSigningKey(keySpec)
			.build();
	}

}
