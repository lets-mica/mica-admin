package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.admin.project.system.entity.SysUserMessage;
import net.dreamlu.mica.admin.project.system.mapper.SysUserMessageMapper;
import net.dreamlu.mica.admin.project.system.pojo.UserMessageVo;
import net.dreamlu.mica.admin.project.system.service.ISysUserMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 系统消息推送记录 服务实现类
 * </p>
 *
 * @author L.cm
 */
@Service
public class SysUserMessageServiceImpl extends ServiceImpl<SysUserMessageMapper, SysUserMessage> implements ISysUserMessageService {

	@Override
	public List<UserMessageVo> getUnreadList(Long userId) {
		return baseMapper.selectUnreadByUserId(userId);
	}

	@Override
	public void markRead(Long id, Long userId) {
		update(new LambdaUpdateWrapper<SysUserMessage>()
			.eq(SysUserMessage::getId, id)
			.eq(SysUserMessage::getUserId, userId)
			.set(SysUserMessage::getIsRead, Boolean.TRUE));
	}

	@Override
	public void markAllRead(Long userId) {
		update(new LambdaUpdateWrapper<SysUserMessage>()
			.eq(SysUserMessage::getUserId, userId)
			.eq(SysUserMessage::getIsRead, Boolean.FALSE)
			.set(SysUserMessage::getIsRead, Boolean.TRUE));
	}

	@Override
	public Page<UserMessageVo> getMyMessages(Long userId, Page<?> page, String blurry, List<LocalDateTime> createTime) {
		Page<UserMessageVo> resultPage = new Page<>(page.getCurrent(), page.getSize());
		IPage<UserMessageVo> result = baseMapper.selectByUserId(
			new Page<>(page.getCurrent(), page.getSize()),
			userId,
			blurry,
			createTime
		);
		resultPage.setRecords(result.getRecords());
		resultPage.setTotal(result.getTotal());
		return resultPage;
	}

}
