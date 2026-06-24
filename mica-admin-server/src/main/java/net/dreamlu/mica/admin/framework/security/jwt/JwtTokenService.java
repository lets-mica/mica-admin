package net.dreamlu.mica.admin.framework.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.config.MicaAdminSecurityProperties;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
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
	private Algorithm algorithm;
	private JWTVerifier verifier;

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
		DecodedJWT decodedJWT = verifier.verify(token);
		String subject = decodedJWT.getSubject();
		if (StringUtil.isBlank(subject)) {
			return null;
		}
		return subject;
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
		Date expiresAt = new Date(now.getTime() + expireTime.toMillis());
		JWTCreator.Builder builder = JWT.create()
			.withJWTId(StringUtil.getUUID())
			.withAudience(jwtToken.getAudience())
			.withIssuer(jwtToken.getIssuer())
			.withIssuedAt(now)
			.withSubject(authUser.getUsername())
			.withNotBefore(now)
			.withExpiresAt(expiresAt);
		return builder.sign(algorithm);
	}

	@Override
	public void afterSingletonsInstantiated() {
		// jwt token 配置信息
		MicaAdminSecurityProperties.JwtToken jwtToken = properties.getJwtToken();
		String secret = jwtToken.getSecret();
		// 根据配置的算法名称构造对应的 auth0 Algorithm
		algorithm = createAlgorithm(jwtToken.getSignatureAlgorithm(), secret);
		// verifier：校验签名 + issuer + audience
		verifier = JWT.require(algorithm)
			.withIssuer(jwtToken.getIssuer())
			.withAudience(jwtToken.getAudience())
			.build();
	}

	/**
	 * 将字符串算法名（HS256 / HS384 / HS512）转换为 auth0 Algorithm 实例
	 *
	 * @param name   算法名
	 * @param secret 秘钥
	 * @return Algorithm
	 */
	private static Algorithm createAlgorithm(String name, String secret) {
		if (StringUtil.isBlank(name)) {
			name = "HS256";
		}
		switch (name.toUpperCase()) {
			case "HS384":
				return Algorithm.HMAC384(secret);
			case "HS512":
				return Algorithm.HMAC512(secret);
			case "HS256":
			default:
				return Algorithm.HMAC256(secret);
		}
	}

	/**
	 * 暴露给过滤器判断是否是 jwt 验证异常
	 *
	 * @param t 异常
	 * @return 是否为 JWT 验证异常
	 */
	public static boolean isJwtException(Throwable t) {
		return t instanceof JWTVerificationException;
	}

}
