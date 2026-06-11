package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysRoleDept;

import java.util.List;

/**
 * <p>
 * 角色和部门关联表 服务类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
public interface ISysRoleDeptService extends IService<SysRoleDept> {

	/**
	 * 批量保存
	 *
	 * @param roleId     角色id
	 * @param deptIdList 部门id集合
	 * @return 是否成功
	 */
	boolean saveList(Long roleId, List<Long> deptIdList);

	/**
	 * 根据角色 id 删除
	 *
	 * @param roleId 角色id
	 * @return 是否成功
	 */
	boolean removeByRoleId(Long roleId);

	/**
	 * 查找角色对应的部门
	 *
	 * @param roleId 角色 id
	 * @return 部门 id 集合
	 */
	List<Long> findDeptIdListByRoleId(Long roleId);

}
