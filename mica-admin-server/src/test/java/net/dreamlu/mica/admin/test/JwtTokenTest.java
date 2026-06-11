package net.dreamlu.mica.admin.test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.dreamlu.mica.core.utils.Charsets;
import net.dreamlu.mica.core.utils.DateUtil;
import net.dreamlu.mica.core.utils.DigestUtil;
import net.dreamlu.mica.core.utils.StringUtil;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
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
		SignatureAlgorithm algorithm = SignatureAlgorithm.HS256;
		String audience = "Mica-Less-Web";
		String issuer = "Mica-Less-Api";
		Duration expireTime = Duration.ofMinutes(30);
		Date now = new Date();

		Key keySpec = new SecretKeySpec(jwtSecret.getBytes(Charsets.UTF_8), algorithm.getJcaName());

		String token = Jwts.builder()
			.setId(StringUtil.getUUID())
			.setAudience(audience)
			.setIssuer(issuer)
			.setIssuedAt(now)
			.setSubject("admin")
			.setNotBefore(now)
			.setExpiration(DateUtil.plus(now, expireTime))
			.signWith(keySpec, algorithm)
			.compact();
		Jws<Claims> claimsJws = Jwts.parserBuilder()
			.setSigningKey(keySpec)
			.build()
			.parseClaimsJws(token);
		System.out.println(claimsJws);
	}

	public static void main(String[] args) {
		test();
	}

}
