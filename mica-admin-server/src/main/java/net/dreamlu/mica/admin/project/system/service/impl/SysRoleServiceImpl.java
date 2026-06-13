package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.admin.project.system.entity.SysRole;
import net.dreamlu.mica.admin.project.system.entity.SysRoleMenu;
import net.dreamlu.mica.admin.project.system.entity.SysUserRole;
import net.dreamlu.mica.admin.project.system.mapper.SysRoleMapper;
import net.dreamlu.mica.admin.project.system.pojo.RoleQuery;
import net.dreamlu.mica.admin.project.system.service.ISysRoleDeptService;
import net.dreamlu.mica.admin.project.system.service.ISysRoleMenuService;
import net.dreamlu.mica.admin.project.system.service.ISysRoleService;
import net.dreamlu.mica.admin.project.system.service.ISysUserRoleService;
import net.dreamlu.mica.core.result.R;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色信息表 服务实现类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
	@Autowired
	private ISysUserRoleService userRoleService;
	@Autowired
	private ISysRoleMenuService roleMenuService;
	@Autowired
	private ISysRoleDeptService roleDeptService;

	@Override
	public Wrapper<SysRole> getQueryWrapper(RoleQuery query) {
		LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
		// 模糊查询 name,title,remark
		String blurry = query.getBlurry();
		wrapper.and(StringUtil.isNotBlank(blurry), w -> w
			.like(SysRole::getName, blurry)
			.or().like(SysRole::getTitle, blurry)
			.or().like(SysRole::getRemark, blurry));
		List<LocalDateTime> createTime = query.getCreateTime();
		if (createTime != null && createTime.size() > 1) {
			wrapper.between(SysRole::getCreatedAt, createTime.get(0), createTime.get(1));
		}
		return wrapper;
	}

	@Cacheable(value = "sys:role:user#10m", key = "#userId")
	@Override
	public List<SysRole> getListByUserId(Long userId) {
		// 获取关联列表
		List<SysUserRole> userRoleList = userRoleService.getListByUserId(userId);
		if (userRoleList.isEmpty()) {
			return Collections.emptyList();
		}
		// 获取角色id列表
		Set<Long> roleIds = userRoleList.stream()
			.map(SysUserRole::getRoleId)
			.collect(Collectors.toSet());
		// 获取角色列表
		Wrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
			.in(SysRole::getId, roleIds)
			.eq(SysRole::getStatus, Boolean.FALSE)
			.eq(SysRole::getDelFlag, Boolean.FALSE);
		return super.list(wrapper);
	}

	@Override
	public boolean updateMenus(SysRole role, List<Long> menuIds) {
		Long roleId = role.getId();
		// 1. 清空角色菜单
		roleMenuService.deleteByRoleId(roleId);
		// 2. 批量保存
		List<SysRoleMenu> entityList = new ArrayList<>(menuIds.size());
		for (Long menuId : menuIds) {
			SysRoleMenu roleMenu = new SysRoleMenu();
			roleMenu.setRoleId(roleId);
			roleMenu.setMenuId(menuId);
			entityList.add(roleMenu);
		}
		return roleMenuService.saveBatch(entityList);
	}

	@Override
	public boolean deleteIfUnusedByIds(Collection<Long> ids) {
		List<SysUserRole> userRoleList = userRoleService.getListByRoleIds(ids);
		if (userRoleList != null && !userRoleList.isEmpty()) {
			R.throwFail("存在用户角色关系");
		}
		List<SysRoleMenu> roleMenuList = roleMenuService.getListByRoleIds(ids);
		if (roleMenuList != null && !roleMenuList.isEmpty()) {
			R.throwFail("存在菜单角色关系");
		}
		return super.removeByIds(ids);
	}

	@Override
	public boolean saveRole(SysRole entity, boolean isDataScopeCustom, List<Long> deptList) {
		boolean result = super.save(entity);
		if (isDataScopeCustom) {
			Long roleId = entity.getId();
			roleDeptService.saveList(roleId, deptList);
		}
		return result;
	}

	@Override
	public boolean updateRoleById(SysRole entity, boolean isDataScopeCustom, List<Long> deptList) {
		boolean result = super.updateById(entity);
		Long roleId = entity.getId();
		roleDeptService.removeByRoleId(roleId);
		if (isDataScopeCustom) {
			roleDeptService.saveList(roleId, deptList);
		}
		return result;
	}

}
