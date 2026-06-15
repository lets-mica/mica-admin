package net.dreamlu.mica.admin.framework.security.service;

import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.framework.security.utils.SecurityUtil;
import net.dreamlu.mica.core.utils.BeanUtil;
import net.dreamlu.mica.core.utils.ObjectUtil;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.PatternMatchUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * 权限判断
 * <p>
 * url: https://stackoverflow.com/questions/41434231/use-spring-security-in-thymeleaf-escaped-expressions-in-javascript
 *
 * @author l.cm
 */
@Service("sec")
public class SecService {

	/**
	 * 提供给页面输出当前用户
	 *
	 * @return {AuthUser}
	 */
	public AuthUser currentUser() {
		return SecurityUtil.getUser();
	}

	/**
	 * 获取用户属性
	 *
	 * @param propertyName 属性名
	 * @return 用户属性信息
	 */
	public Object authentication(String propertyName) {
		AuthUser micaUser = currentUser();
		if (micaUser == null) {
			return null;
		}
		return BeanUtil.getProperty(micaUser, propertyName);
	}

	/**
	 * 判断是否超级管理员
	 *
	 * @return 是否管理员
	 */
	public boolean isAdmin() {
		AuthUser authUser = this.currentUser();
		if (authUser == null) {
			return false;
		}
		return ObjectUtil.isTrue(authUser.getIsAdmin());
	}

	/**
	 * 已经授权的
	 *
	 * @return 是否授权
	 */
	public boolean isAuthenticated() {
		return this.currentUser() != null;
	}


	/**
	 * 判断请求是否有权限
	 *
	 * @param request HttpServletRequest
	 * @return 是否有权限
	 */
	public boolean hasPermission(HttpServletRequest request) {
		return hasPermission(request, SecurityUtil.getAuthentication());
	}

	/**
	 * 判断请求是否有权限
	 *
	 * @param request        HttpServletRequest
	 * @param authentication 认证信息
	 * @return 是否有权限
	 */
	public boolean hasPermission(HttpServletRequest request, Authentication authentication) {
		AuthUser authUser = SecurityUtil.getUser(authentication);
		if (authUser == null) {
			return false;
		}
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		if (authorities.isEmpty()) {
			return false;
		}
		// url 进行鉴权
		return true;
	}

	/**
	 * 判断按钮是否有xxx:xxx权限
	 *
	 * @param permissions 权限表达式
	 * @return {boolean}
	 */
	public boolean hasPermission(String... permissions) {
		if (StringUtil.isAnyBlank(permissions)) {
			return false;
		}
		Authentication authentication = SecurityUtil.getAuthentication();
		if (authentication == null) {
			return false;
		}
		AuthUser authUser = SecurityUtil.getUser(authentication);
		if (authUser == null) {
			return false;
		}
		// admin 有所有权限
		if (authUser.getIsAdmin()) {
			return true;
		}
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		return authorities.stream()
			.map(GrantedAuthority::getAuthority)
			.filter(StringUtil::isNotBlank)
			.anyMatch(x -> PatternMatchUtils.simpleMatch(permissions, x));
	}
}
