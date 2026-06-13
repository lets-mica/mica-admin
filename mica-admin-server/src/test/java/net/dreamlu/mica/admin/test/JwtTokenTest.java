package net.dreamlu.mica.admin.test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import net.dreamlu.mica.core.utils.DigestUtil;
import net.dreamlu.mica.core.utils.StringUtil;

import java.time.Duration;
import java.util.Date;

public class JwtTokenTest {

	/**
	 * 生成密码
	 */
	public static String getJwtSecret() {
		String jwtSecret = DigestUtil.sha256Hex("mica-admin");
		System.out.println(jwtSecret);
		return jwtSecret;
	}

	public static void test() {
		String jwtSecret = getJwtSecret();
		System.out.println(jwtSecret);
		String audience = "Mica-Less-Web";
		String issuer = "Mica-Less-Api";
		Duration expireTime = Duration.ofMinutes(30);
		Date now = new Date();
		Date expiresAt = new Date(now.getTime() + expireTime.toMillis());

		Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

		String token = JWT.create()
			.withJWTId(StringUtil.getUUID())
			.withAudience(audience)
			.withIssuer(issuer)
			.withIssuedAt(now)
			.withSubject("admin")
			.withNotBefore(now)
			.withExpiresAt(expiresAt)
			.sign(algorithm);
		DecodedJWT decodedJWT = JWT.require(algorithm)
			.withIssuer(issuer)
			.withAudience(audience)
			.build()
			.verify(token);
		System.out.println(decodedJWT);
	}

	public static void main(String[] args) {
		test();
	}

}