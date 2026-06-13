package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysUserRole;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 用户和角色关联表 服务类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
public interface ISysUserRoleService extends IService<SysUserRole> {

	/**
	 * 根据用户id获取用户角色关系列表
	 *
	 * @param userId 用户id
	 * @return 用户角色关系列表
	 */
	List<SysUserRole> getListByUserId(Long userId);

	/**
	 * 保存用户角色
	 *
	 * @param userId  用户id
	 * @param roleIds 角色id列表
	 * @return 是否成功
	 */
	boolean saveByUserIdAndRoleIds(Long userId, List<Long> roleIds);

	/**
	 * 删除用户角色
	 *
	 * @param userId 用户id
	 * @return 是否成功
	 */
	boolean deleteByUserId(Long userId);

	/**
	 * 批量删除用户角色
	 *
	 * @param userIds 用户id
	 * @return 是否成功
	 */
	boolean deleteByUserIds(Set<Long> userIds);

	/**
	 * 获取用户角色列表
	 *
	 * @param ids 角色id集合
	 * @return 用户角色集合
	 */
	List<SysUserRole> getListByRoleIds(Collection<Long> ids);
}
