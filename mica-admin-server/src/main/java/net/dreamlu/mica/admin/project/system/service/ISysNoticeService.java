package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysNotice;
import net.dreamlu.mica.admin.project.system.pojo.NoticeQuery;

/**
 * <p>
 * 通知公告表 服务类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
public interface ISysNoticeService extends IService<SysNotice> {

	/**
	 * 根据 query 组装查询条件
	 *
	 * @param query query
	 * @return Wrapper
	 */
	Wrapper<SysNotice> getQueryWrapper(NoticeQuery query);

}
