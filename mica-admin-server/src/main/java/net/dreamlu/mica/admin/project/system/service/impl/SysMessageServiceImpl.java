package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.project.system.entity.SysMessage;
import net.dreamlu.mica.admin.project.system.entity.SysUser;
import net.dreamlu.mica.admin.project.system.entity.SysUserMessage;
import net.dreamlu.mica.admin.project.system.mapper.SysMessageMapper;
import net.dreamlu.mica.admin.project.system.pojo.MessageQuery;
import net.dreamlu.mica.admin.project.system.service.ISysMessageService;
import net.dreamlu.mica.admin.project.system.service.ISysUserMessageService;
import net.dreamlu.mica.admin.project.system.service.ISysUserService;
import net.dreamlu.mica.core.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统消息 服务实现类
 * </p>
 *
 * @author L.cm
 */
@Service
@RequiredArgsConstructor
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements ISysMessageService {
	private static final Logger log = LoggerFactory.getLogger(SysMessageServiceImpl.class);
	private final ISysUserService userService;
	private final ISysUserMessageService userMessageService;

	@Override
	public Wrapper<SysMessage> getQueryWrapper(MessageQuery query) {
		LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
		wrapper.like(StringUtil.isNotBlank(query.getTitle()), SysMessage::getTitle, query.getTitle());
		wrapper.like(StringUtil.isNotBlank(query.getCategory()), SysMessage::getCategory, query.getCategory());
		wrapper.eq(query.getEnabled() != null, SysMessage::getEnabled, query.getEnabled());
		List<LocalDateTime> createTime = query.getCreateTime();
		if (createTime != null && createTime.size() > 1) {
			wrapper.between(SysMessage::getCreatedAt, createTime.get(0), createTime.get(1));
		}
		wrapper.orderByDesc(SysMessage::getSeq).orderByDesc(SysMessage::getId);
		return wrapper;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void publish(Long messageId) {
		publish(messageId, null, null);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void publish(Long messageId, List<Long> userIds, List<Long> deptIds) {
		// 校验消息是否存在
		SysMessage message = getById(messageId);
		if (message == null) {
			throw new RuntimeException("消息不存在");
		}
		// 确定目标用户
		boolean hasUserFilter = (userIds != null && !userIds.isEmpty());
		boolean hasDeptFilter = (deptIds != null && !deptIds.isEmpty());
		List<Long> targetUserIds;
		if (!hasUserFilter && !hasDeptFilter) {
			// 未指定筛选条件，查询所有启用且未删除的用户
			targetUserIds = userService.list(new LambdaQueryWrapper<SysUser>()
					.eq(SysUser::getEnabled, true)
					.eq(SysUser::getDelFlag, false)
					.select(SysUser::getId))
				.stream()
				.map(SysUser::getId)
				.collect(Collectors.toList());
		} else {
			// 构建查询：启用 + 未删除 + (指定用户ID 或 指定部门ID)
			LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
				.eq(SysUser::getEnabled, true)
				.eq(SysUser::getDelFlag, false)
				.and(w -> {
					if (hasUserFilter) {
						w.in(SysUser::getId, userIds);
					}
					if (hasDeptFilter) {
						if (hasUserFilter) {
							w.or().in(SysUser::getDeptId, deptIds);
						} else {
							w.in(SysUser::getDeptId, deptIds);
						}
					}
				})
				.select(SysUser::getId);
			targetUserIds = userService.list(wrapper).stream()
				.map(SysUser::getId)
				.collect(Collectors.toList());
		}
		if (targetUserIds.isEmpty()) {
			return;
		}
		// 防重复分发：排除已有推送记录的用户
		List<Long> existUserIds = userMessageService.list(new LambdaQueryWrapper<SysUserMessage>()
				.eq(SysUserMessage::getMessageId, messageId)
				.select(SysUserMessage::getUserId))
			.stream()
			.map(SysUserMessage::getUserId)
			.collect(Collectors.toList());
		List<Long> finalUserIds = targetUserIds.stream()
			.filter(id -> !existUserIds.contains(id))
			.collect(Collectors.toList());
		if (finalUserIds.isEmpty()) {
			return;
		}
		// 批量创建用户消息记录
		List<SysUserMessage> userMessages = finalUserIds.stream().map(userId -> {
			SysUserMessage um = new SysUserMessage();
			um.setMessageId(messageId);
			um.setUserId(userId);
			um.setReadFlag(Boolean.FALSE);
			return um;
		}).collect(Collectors.toList());
		userMessageService.saveBatch(userMessages, 500);
		log.info("SysMessage publish: messageId={}, targets={}", messageId, finalUserIds.size());
	}

}
