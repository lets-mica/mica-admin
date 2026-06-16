package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysJob;
import net.dreamlu.mica.admin.project.system.pojo.SysJobQuery;

/**
 * <p>
 * 数据库驱动定时任务 Service
 * </p>
 *
 * @author L.cm
 */
public interface ISysJobService extends IService<SysJob> {

	/**
	 * 根据查询条件构造 wrapper
	 *
	 * @param query 查询条件
	 * @return Wrapper
	 */
	Wrapper<SysJob> getQueryWrapper(SysJobQuery query);

	/**
	 * 根据 jobKey 获取任务
	 *
	 * @param jobKey 任务Key
	 * @return SysJob
	 */
	SysJob getByJobKey(String jobKey);
}
