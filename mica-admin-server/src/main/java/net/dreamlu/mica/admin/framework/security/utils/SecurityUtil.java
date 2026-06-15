package net.dreamlu.mica.admin.framework.security.utils;

import jakarta.servlet.http.HttpServletRequest;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.core.utils.WebUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

/**
 * 安全工具类
 *
 * @author L.cm
 */
public class SecurityUtil {
	/**
	 * 角色前缀
	 */
	public static final String SECURITY_ROLE_PREFIX = "ROLE_";

	/**
	 * 获取Authentication
	 */
	public static Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	/**
	 * 获取用户
	 */
	public static AuthUser getUser(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof AuthUser) {
			return ((AuthUser) principal);
		}
		return null;
	}

	/**
	 * 获取用户
	 */
	public static AuthUser getUser() {
		Authentication authentication = getAuthentication();
		if (authentication == null) {
			return null;
		}
		return getUser(authentication);
	}

	/**
	 * 获取用户名
	 */
	public static String getUserName() {
		AuthUser authUser = getUser();
		return authUser == null ? null : authUser.getUsername();
	}

	/**
	 * 退出
	 */
	public static void logout() {
		HttpServletRequest request = WebUtil.getRequest();
		new SecurityContextLogoutHandler().logout(request, null, null);
	}
}
