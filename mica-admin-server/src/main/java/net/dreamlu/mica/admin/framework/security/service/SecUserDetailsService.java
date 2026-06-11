package net.dreamlu.mica.admin.framework.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.utils.BeanUtil;
import net.dreamlu.mica.core.utils.StringUtil;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.framework.security.pojo.DeptInfo;
import net.dreamlu.mica.admin.framework.security.pojo.PostInfo;
import net.dreamlu.mica.admin.framework.security.pojo.RoleInfo;
import net.dreamlu.mica.admin.framework.security.utils.SecurityUtil;
import net.dreamlu.mica.admin.project.system.entity.*;
import net.dreamlu.mica.admin.project.system.service.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户详情服务
 *
 * @author L.cm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecUserDetailsService implements UserDetailsPasswordService, UserDetailsService, UserLockService {
	private final ISysUserService userService;
	private final ISysRoleService roleService;
	private final ISysMenuService menuService;
	private final ISysDeptService deptService;
	private final ISysPostService postService;

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		if (StringUtil.isBlank(userName)) {
			throw new UsernameNotFoundException("userName is blank!");
		}
		SysUser sysUser = userService.getByUserName(userName);
		if (sysUser == null) {
			throw new UsernameNotFoundException("User is not found!");
		}
		Long userId = sysUser.getId();
		Boolean isAdmin = sysUser.getIsAdmin();
		List<SysRole> roleList = roleService.getListByUserId(userId);
		Set<String> dbAuthSet = new HashSet<>();
		List<RoleInfo> roleInfoList = new ArrayList<>();
		if (roleList != null && !roleList.isEmpty()) {
			// 角色列表信息
			roleInfoList.addAll(BeanUtil.copy(roleList, RoleInfo.class));
			// 获取角色
			loadRoleAuthorities(roleList, dbAuthSet);
			// 获取资源，超级管理员有所有资源
			loadUserAuthorities(roleList, dbAuthSet, isAdmin);
		}
		String password = sysUser.getPassword();
		boolean enabled = sysUser.getEnabled();
		boolean accountNonLocked = !sysUser.getLocked();
		Collection<? extends GrantedAuthority> authorities
			= AuthorityUtils.createAuthorityList(dbAuthSet.toArray(new String[0]));
		Long deptId = sysUser.getDeptId();
		SysDept sysDept = deptService.getById(deptId);
		List<SysPost> sysPostList = postService.getListByUserId(userId);
		// 构造security用户
		AuthUser authUser = new AuthUser(userName, password, enabled, accountNonLocked, authorities);
		authUser.setUserId(sysUser.getId());
		authUser.setNickName(sysUser.getNickName());
		authUser.setIsAdmin(sysUser.getIsAdmin());
		authUser.setGender(sysUser.getGender());
		authUser.setEmail(sysUser.getEmail());
		authUser.setPhone(sysUser.getPhone());
		authUser.setAvatar(sysUser.getAvatar());
		authUser.setDept(BeanUtil.copy(sysDept, DeptInfo.class));
		authUser.setPostList(BeanUtil.copy(sysPostList, PostInfo.class));
		authUser.setRoleList(roleInfoList);
		return authUser;
	}

	@Override
	public UserDetails updatePassword(UserDetails user, String newPassword) {
		AuthUser authUser = (AuthUser) user;
		SysUser entity = new SysUser();
		entity.setId(authUser.getUserId());
		entity.setPassword(newPassword);
		userService.updateById(entity);
		return AuthUser.formMicaUser(authUser, newPassword);
	}

	@Override
	public boolean updateLockUser(AuthUser authUser) {
		SysUser entity = new SysUser();
		entity.setId(authUser.getUserId());
		entity.setLocked(Boolean.TRUE);
		return userService.updateById(entity);
	}

	private void loadRoleAuthorities(List<SysRole> roleList, Set<String> dbAuthsSet) {
		roleList.stream().map(SysRole::getTitle)
			.filter(StringUtil::isNotBlank)
			.forEach(x ->
				// 角色加前缀，Security 默认的规则
				dbAuthsSet.add(SecurityUtil.SECURITY_ROLE_PREFIX + x)
			);
	}

	private void loadUserAuthorities(List<SysRole> roleList, Set<String> dbAuthSet, Boolean isAdmin) {
		List<SysMenu> menuList;
		// 超级管理员有所有资源权限
		if (Boolean.TRUE.equals(isAdmin)) {
			menuList = menuService.list();
		} else {
			Set<Long> roleIds = roleList.stream().map(SysRole::getId).collect(Collectors.toSet());
			menuList = menuService.getListByRoleIds(roleIds);
		}
		menuList.stream()
			.map(SysMenu::getPermission)
			.filter(StringUtil::isNotBlank)
			.forEach(dbAuthSet::add);
	}
}
