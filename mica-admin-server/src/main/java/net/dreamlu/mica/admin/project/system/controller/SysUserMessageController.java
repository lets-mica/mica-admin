package net.dreamlu.mica.admin.project.system.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.project.system.pojo.UserMessageVo;
import net.dreamlu.mica.admin.project.system.service.ISysUserMessageService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户消息 前端控制器
 * </p>
 *
 * @author L.cm
 */
@Validated
@RestController
@RequestMapping("/api/system/user/message")
@Tag(name = "系统-用户消息")
@RequiredArgsConstructor
public class SysUserMessageController extends BaseController {
	private final ISysUserMessageService userMessageService;

	@Operation(summary = "未读消息列表")
	@GetMapping("unread")
	@PreAuthorize("@sec.isAuthenticated()")
	public List<UserMessageVo> unread(AuthUser authUser) {
		return userMessageService.getUnreadList(authUser.getUserId());
	}

	@Operation(summary = "我的消息分页")
	@GetMapping
	@PreAuthorize("@sec.isAuthenticated()")
	public Page<UserMessageVo> myMessages(AuthUser authUser,
										  Page<?> page,
										  @RequestParam(required = false) String blurry,
										  @RequestParam(required = false)
										  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
										  List<LocalDateTime> createTime) {
		return userMessageService.getMyMessages(authUser.getUserId(), page, blurry, createTime);
	}

	@Operation(summary = "标记消息已读")
	@ApiLog("标记消息已读")
	@PutMapping("read/{id}")
	@PreAuthorize("@sec.isAuthenticated()")
	public void markRead(AuthUser authUser, @PathVariable Long id) {
		userMessageService.markRead(id, authUser.getUserId());
	}

	@Operation(summary = "全部标记已读")
	@ApiLog("全部标记已读")
	@PutMapping("read-all")
	@PreAuthorize("@sec.isAuthenticated()")
	public void markAllRead(AuthUser authUser) {
		userMessageService.markAllRead(authUser.getUserId());
	}

}
