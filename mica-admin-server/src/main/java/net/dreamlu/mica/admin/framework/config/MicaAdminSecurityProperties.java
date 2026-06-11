package net.dreamlu.mica.admin.framework.config;

import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;
import net.dreamlu.mica.core.tuple.KeyPair;
import net.dreamlu.mica.core.utils.RsaUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * mica 安全框架配置
 *
 * @author L.cm
 */
@Getter
@Setter
@Validated
@ConfigurationProperties("mica.security")
public class MicaAdminSecurityProperties {

	/**
	 * 忽略的地址
	 */
	private final List<String> permitAll = new ArrayList<>();
	private final Login login = new Login();
	private final JwtToken jwtToken = new JwtToken();

	/**
	 * 登录密码加解密 rsa 秘钥
	 */
	@NestedConfigurationProperty
	private KeyPair loginKeyPair = RsaUtil.genKeyPair();

	/**
	 * 用户密码加解密 rsa 秘钥
	 */
	@NestedConfigurationProperty
	private KeyPair userKeyPair = RsaUtil.genKeyPair();

	/**
	 * 登录配置
	 */
	@Getter
	@Setter
	public static class Login {
		/**
		 * 登录重试锁定次数，默认：5
		 */
		private int retryLimit = 5;
		/**
		 * 登录重试锁定cache名，默认：retryLimitCache
		 */
		private String retryLimitCacheName = "retryLimitCache#30m";
	}

	/**
	 * 登录配置
	 */
	@Getter
	@Setter
	public static class JwtToken {
		/**
		 * 令牌自定义标识
		 */
		private String header = "Authorization";
		/**
		 * 令牌秘钥
		 */
		@NotBlank
		private String secret;
		/**
		 * 秘钥的签名算法
		 */
		private SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		/**
		 * jwt token 接受者
		 */
		private String audience = "Mica-Admin";
		/**
		 * jwt token 签发者
		 */
		private String issuer = "Mica-Admin-Web";
		/**
		 * 令牌有效期（默认8小时），拒绝 996 从你我做起。
		 */
		private Duration expireTime = Duration.ofHours(8);
		/**
		 * 记住密码有效期（默认30天）
		 */
		private Duration rememberMeTime = Duration.ofDays(30);
		/**
		 * redis token 存储前缀
		 */
		private String storePrefix = "mica-admin:tokens:";
	}

}
