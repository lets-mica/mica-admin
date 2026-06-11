package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysRoleMenu;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色和菜单关联表 服务类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
public interface ISysRoleMenuService extends IService<SysRoleMenu> {

	/**
	 * 角色菜单列表
	 *
	 * @param roleIds 角色id集合
	 * @return 角色菜单
	 */
	List<SysRoleMenu> getListByRoleIds(Collection<Long> roleIds);

	/**
	 * 角色菜单 id 列表
	 *
	 * @param roleIds 角色id集合
	 * @return 角色菜单
	 */
	default List<Long> getIdListByRoleIds(Collection<Long> roleIds) {
		List<SysRoleMenu> roleMenuList = this.getListByRoleIds(roleIds);
		if (roleMenuList.isEmpty()) {
			return Collections.emptyList();
		}
		return roleMenuList.stream()
			.map(SysRoleMenu::getMenuId)
			.distinct()
			.collect(Collectors.toList());
	}

	/**
	 * 清空角色菜单
	 *
	 * @param roleId 角色id
	 * @return 是否成功
	 */
	boolean deleteByRoleId(Long roleId);

	/**
	 * 根据菜单id集合查询
	 *
	 * @param menuIds 菜单id集合
	 * @return 角色菜单集合
	 */
	List<SysRoleMenu> getListByMenuIds(Collection<Long> menuIds);
}
