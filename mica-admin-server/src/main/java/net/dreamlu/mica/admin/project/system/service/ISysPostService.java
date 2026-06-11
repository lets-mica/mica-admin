package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysPost;
import net.dreamlu.mica.admin.project.system.pojo.PostQuery;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 岗位信息表 服务类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
public interface ISysPostService extends IService<SysPost> {

	/**
	 * 根据 query 组装查询条件
	 *
	 * @param query PostQuery
	 * @return Wrapper
	 */
	Wrapper<SysPost> getQueryWrapper(PostQuery query);

	/**
	 * 查找用户岗位信息
	 *
	 * @param userId 用户id
	 * @return 集合
	 */
	List<SysPost> getListByUserId(Long userId);

	/**
	 * 如果没用使用的情况下删除
	 *
	 * @param ids 岗位id集合
	 * @return 是否成功
	 */
	boolean deleteIfUnusedByIds(Collection<Long> ids);
}
