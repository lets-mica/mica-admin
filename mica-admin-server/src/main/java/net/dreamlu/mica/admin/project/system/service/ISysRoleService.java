package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysRole;
import net.dreamlu.mica.admin.project.system.pojo.RoleQuery;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 角色信息表 服务类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
public interface ISysRoleService extends IService<SysRole> {

	/**
	 * 根据 query 组装查询条件
	 *
	 * @param query DeptQuery
	 * @return Wrapper
	 */
	Wrapper<SysRole> getQueryWrapper(RoleQuery query);

	/**
	 * 根据用户 id 获取角色列表
	 *
	 * @param userId 用户id
	 * @return 角色列表
	 */
	List<SysRole> getListByUserId(Long userId);

	/**
	 * 更新角色菜单
	 *
	 * @param role    角色
	 * @param menuIds 菜单列表
	 * @return 是否成功
	 */
	boolean updateMenus(SysRole role, List<Long> menuIds);

	/**
	 * 如果没有使用，删除
	 *
	 * @param ids id 集合
	 * @return 是否成功
	 */
	boolean deleteIfUnusedByIds(Collection<Long> ids);

	/**
	 * 保存角色
	 *
	 * @param entity            实体
	 * @param isDataScopeCustom 是否自定义数据权限
	 * @param deptList          部门列表
	 * @return 是否成功
	 */
	boolean saveRole(SysRole entity, boolean isDataScopeCustom, List<Long> deptList);

	/**
	 * 更新角色
	 *
	 * @param entity            实体
	 * @param isDataScopeCustom 是否自定义数据权限
	 * @param deptList          部门列表
	 * @return 是否成功
	 */
	boolean updateRoleById(SysRole entity, boolean isDataScopeCustom, List<Long> deptList);

}
