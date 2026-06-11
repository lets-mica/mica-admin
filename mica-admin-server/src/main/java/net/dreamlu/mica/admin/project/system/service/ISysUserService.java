package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.project.system.entity.SysUser;
import net.dreamlu.mica.admin.project.system.pojo.UserQuery;
import net.dreamlu.mica.admin.project.system.pojo.UserVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
public interface ISysUserService extends IService<SysUser> {

	/**
	 * 根据登录名查找用户
	 *
	 * @param userName 登录名
	 * @return 用户
	 */
	SysUser getByUserName(String userName);

	/**
	 * 根据 query 组装查询条件
	 *
	 * @param query UserQuery
	 * @return Wrapper
	 */
	Wrapper<SysUser> getQueryWrapper(UserQuery query);

	/**
	 * 更新头像
	 *
	 * @param file     头像文件
	 * @param authUser 当前用户
	 * @return Map<String, Object>
	 */
	Map<String, Object> updateAvatar(MultipartFile file, AuthUser authUser);

	/**
	 * 更新用户信息
	 *
	 * @param entity   SysUser
	 * @param userName userName
	 * @return 是否成功
	 */
	boolean updateById(SysUser entity, String userName);

	/**
	 * 用户分页
	 *
	 * @param page         分页
	 * @param queryWrapper wrapper
	 * @return 分页数据
	 */
	Page<UserVo> getUserPage(Page<SysUser> page, Wrapper<SysUser> queryWrapper);

	/**
	 * 保存用户信息
	 *
	 * @param sysUser 用户信息
	 * @param roleIds 角色列表
	 * @param postIds 岗位列表
	 * @return 是否成功
	 */
	boolean saveUserInfo(SysUser sysUser, List<Long> roleIds, List<Long> postIds);

	/**
	 * 更新用户信息
	 *
	 * @param sysUser 用户信息
	 * @param roleIds 角色列表
	 * @param postIds 岗位列表
	 * @return 是否成功
	 */
	boolean updateUserInfo(SysUser sysUser, List<Long> roleIds, List<Long> postIds);

	/**
	 * 批量删除用户
	 *
	 * @param userIds 用户id集合
	 * @return 是否成功
	 */
	boolean deleteByIds(Set<Long> userIds);

	/**
	 * 查找用户集合
	 *
	 * @param deptIds 岗位id集合
	 * @return 用户集合
	 */
	List<SysUser> findListByDeptIds(Collection<Long> deptIds);
}
