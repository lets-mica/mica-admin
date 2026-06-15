package net.dreamlu.mica.admin.framework.security.auth;

import net.dreamlu.mica.admin.framework.config.MicaAdminSecurityProperties;
import net.dreamlu.mica.admin.framework.security.service.SecUserDetailsService;
import net.dreamlu.mica.admin.framework.security.service.UserLockService;
import net.dreamlu.mica.captcha.service.ICaptchaService;
import net.dreamlu.mica.core.exception.ServiceException;
import net.dreamlu.mica.core.result.SystemCode;
import net.dreamlu.mica.core.utils.RsaUtil;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义授权处理，添加验证码
 *
 * @author L.cm
 */
public class SecAuthenticationProvider extends DaoAuthenticationProvider {
	private final MicaAdminSecurityProperties micaSecurityProperties;
	private final ICaptchaService captchaService;
	private final CacheManager cacheManager;
	private Cache passwordRetryCache;

	public SecAuthenticationProvider(UserDetailsService userDetailsService,
	                                 MicaAdminSecurityProperties micaSecurityProperties,
	                                 ICaptchaService captchaService,
	                                 CacheManager cacheManager) {
		super(userDetailsService);
		this.micaSecurityProperties = micaSecurityProperties;
		this.captchaService = captchaService;
		this.cacheManager = cacheManager;
	}

	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException {
		if (auth.isAuthenticated()) {
			return auth;
		}
		SecWebAuthenticationDetails details = (SecWebAuthenticationDetails) auth.getDetails();
		String validateCode = details.getValidateCode();
		if (StringUtil.isBlank(validateCode)) {
			throw new ServiceException(SystemCode.PARAM_MISS, "请填写验证码");
		}
		String validateCodeId = details.getValidateCodeId();
		if (StringUtil.isBlank(validateCodeId)) {
			throw new ServiceException(SystemCode.PARAM_MISS, "验证码UUID为空");
		}
		boolean captchaValidated = captchaService.validate(validateCodeId, validateCode);
		if (!captchaValidated) {
			throw new ServiceException(SystemCode.PARAM_VALID_ERROR, "验证码已失效");
		}
		UsernamePasswordAuthenticationToken token;
		try {
			token = decodeRsaPassword(auth);
		} catch (Throwable e) {
			throw new ServiceException(SystemCode.PARAM_VALID_ERROR, "密码被篡改，解密失败");
		}
		return super.authenticate(token);
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		// 添加用户锁定的功能，用户尝试登录密码错误太多次锁定账号
		String username = userDetails.getUsername();
		// retry count + 1
		AtomicInteger retryCount = passwordRetryCache.get(username, AtomicInteger.class);
		if (retryCount == null) {
			retryCount = new AtomicInteger(0);
			passwordRetryCache.put(username, retryCount);
		}
		int retryLimit = micaSecurityProperties.getLogin().getRetryLimit();
		if (retryCount.incrementAndGet() > retryLimit) {
			//if retry count > retryLimit
			logger.warn("username: " + username + " tried to login more than " + retryLimit + " times in period");
			UserLockService userLockService = this.getUserLockService();
			userLockService.updateLockUser((AuthUser) userDetails);
			throw new ServiceException(SystemCode.PARAM_VALID_ERROR, "登录错误" + retryCount + "次，账号已锁定");
		} else {
			passwordRetryCache.put(username, retryCount);
		}
		super.additionalAuthenticationChecks(userDetails, authentication);
		//clear retry data
		passwordRetryCache.evict(username);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	@Override
	protected void doAfterPropertiesSet() {
		super.doAfterPropertiesSet();
		Assert.notNull(micaSecurityProperties, "dreamProperties is null");
		Assert.notNull(captchaService, "captchaService is null");
		Assert.notNull(cacheManager, "cacheManager is null");
		String retryLimitCacheName = micaSecurityProperties.getLogin().getRetryLimitCacheName();
		this.passwordRetryCache = cacheManager.getCache(retryLimitCacheName);
		Assert.notNull(this.passwordRetryCache, "retryLimitCache retryLimitCacheName: " + retryLimitCacheName + " is not config.");
	}

	private SecUserDetailsService getUserLockService() {
		UserDetailsService userDetailsService = super.getUserDetailsService();
		return (SecUserDetailsService) userDetailsService;
	}

	private UsernamePasswordAuthenticationToken decodeRsaPassword(Authentication auth) {
		Object credentials = auth.getCredentials();
		String privateBase64 = micaSecurityProperties.getLoginKeyPair().getPrivateBase64();
		String decryptPwd = RsaUtil.decryptFromBase64(privateBase64, (String) credentials);
		UsernamePasswordAuthenticationToken newToken = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), decryptPwd);
		newToken.setDetails(auth.getDetails());
		return newToken;
	}

}
