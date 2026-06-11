package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysUserPost;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 用户与岗位关联表 服务类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
public interface ISysUserPostService extends IService<SysUserPost> {

	/**
	 * 根据用户id查找
	 *
	 * @param userId 用户id
	 * @return 集合
	 */
	List<SysUserPost> getListByUserId(Long userId);

	/**
	 * 保存用户角色
	 *
	 * @param userId  用户id
	 * @param postIds 岗位id列表
	 * @return 是否成功
	 */
	boolean saveByUserIdAndPostIds(Long userId, List<Long> postIds);

	/**
	 * 删除用户岗位
	 *
	 * @param userId 用户id
	 * @return 是否成功
	 */
	boolean deleteByUserId(Long userId);

	/**
	 * 批量删除用户岗位
	 *
	 * @param userIds 用户id
	 * @return 是否成功
	 */
	boolean deleteByUserIds(Set<Long> userIds);

	/**
	 * 查询用户岗位列表
	 *
	 * @param postIds 岗位id集合
	 * @return 用户岗位集合
	 */
	List<SysUserPost> getListByPostIds(Collection<Long> postIds);
}
