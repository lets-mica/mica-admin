package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysMenu;
import net.dreamlu.mica.admin.project.system.pojo.MenuQuery;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 菜单权限表 服务类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
public interface ISysMenuService extends IService<SysMenu> {

	/**
	 * 根据 query 组装查询条件
	 *
	 * @param query DeptQuery
	 * @return Wrapper
	 */
	Wrapper<SysMenu> getQueryWrapper(MenuQuery query);

	/**
	 * 超级管理员获取所有菜单
	 *
	 * @return 菜单列表
	 */
	List<SysMenu> getAllMenu();

	/**
	 * 根据角色 id 列表查找菜单
	 *
	 * @param roleIds 角色id
	 * @return 菜单列表
	 */
	List<SysMenu> getListByRoleIds(Collection<Long> roleIds);

	/**
	 * 根据角色 id 列表查找导航的菜单
	 *
	 * @param roleIds 角色id
	 * @return 菜单列表
	 */
	List<SysMenu> getNavByRoleIds(Collection<Long> roleIds);

	/**
	 * 获取所有的父类
	 *
	 * @param deptList     deptList
	 * @param superiorList 父类
	 * @return deptList
	 */
	List<SysMenu> getSuperior(List<SysMenu> deptList, List<SysMenu> superiorList);

	/**
	 * 如果没有试用删除
	 *
	 * @param ids id集合
	 * @return 是否成功
	 */
	boolean deleteIfUnusedByIds(Collection<Long> ids);
}
