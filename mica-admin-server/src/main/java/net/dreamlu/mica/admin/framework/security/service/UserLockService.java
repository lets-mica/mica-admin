package net.dreamlu.mica.admin.framework.security.service;

import net.dreamlu.mica.admin.framework.security.auth.AuthUser;

/**
 * 锁定用户
 *
 * @author L.cm
 */
public interface UserLockService {

	/**
	 * 锁定用户
	 *
	 * @param authUser AuthUser
	 * @return {boolean}
	 */
	boolean updateLockUser(AuthUser authUser);

}
