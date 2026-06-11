package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysMessage;
import net.dreamlu.mica.admin.project.system.pojo.MessageQuery;

import java.util.List;

/**
 * <p>
 * 系统消息 服务类
 * </p>
 *
 * @author L.cm
 */
public interface ISysMessageService extends IService<SysMessage> {

	/**
	 * 根据 query 组装查询条件
	 *
	 * @param query query
	 * @return Wrapper
	 */
	Wrapper<SysMessage> getQueryWrapper(MessageQuery query);

	/**
	 * 发布消息：为所有启用用户创建 sys_user_message 记录
	 *
	 * @param messageId 消息ID
	 */
	void publish(Long messageId);

	/**
	 * 发布消息：为指定用户/部门创建 sys_user_message 记录
	 *
	 * @param messageId 消息ID
	 * @param userIds   目标用户ID列表，为空则不按用户筛选
	 * @param deptIds   目标部门ID列表，为空则不按部门筛选
	 *                  两者都为空时推送给所有启用用户
	 */
	void publish(Long messageId, List<Long> userIds, List<Long> deptIds);

}
