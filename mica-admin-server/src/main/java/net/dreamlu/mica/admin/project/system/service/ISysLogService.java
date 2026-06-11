package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysLog;
import net.dreamlu.mica.admin.project.system.pojo.LogQuery;

/**
 * <p>
 * 系统日志 服务类
 * </p>
 *
 * @author L.cm
 * @since 2020-07-09
 */
public interface ISysLogService extends IService<SysLog> {

	/**
	 * 根据 query 组装查询条件
	 *
	 * @param query LogQuery
	 * @return Wrapper
	 */
	Wrapper<SysLog> getQueryWrapper(LogQuery query);

	/**
	 * 删除所有的 info 日志
	 */
	void removeAllByInfo();

	/**
	 * 删除所有的 error 日志
	 */
	void removeAllByError();

}
