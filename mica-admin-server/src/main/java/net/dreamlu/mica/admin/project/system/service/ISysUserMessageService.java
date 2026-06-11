package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysUserMessage;
import net.dreamlu.mica.admin.project.system.pojo.UserMessageVo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 系统消息推送记录 服务类
 * </p>
 *
 * @author L.cm
 */
public interface ISysUserMessageService extends IService<SysUserMessage> {

	/**
	 * 获取用户未读消息列表
	 *
	 * @param userId 用户ID
	 * @return 未读消息列表
	 */
	List<UserMessageVo> getUnreadList(Long userId);

	/**
	 * 标记单条消息已读
	 *
	 * @param id     用户消息ID
	 * @param userId 用户ID
	 */
	void markRead(Long id, Long userId);

	/**
	 * 标记用户所有消息已读
	 *
	 * @param userId 用户ID
	 */
	void markAllRead(Long userId);

	/**
	 * 获取用户消息分页列表
	 *
	 * @param userId     用户ID
	 * @param page       分页参数
	 * @param blurry     模糊搜索关键字
	 * @param createTime 创建时间范围
	 * @return 分页结果
	 */
	Page<UserMessageVo> getMyMessages(Long userId, Page<?> page, String blurry, List<LocalDateTime> createTime);

}
