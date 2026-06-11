package net.dreamlu.mica.admin.project.system.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.project.system.entity.SysMessage;
import net.dreamlu.mica.admin.project.system.pojo.MessageQuery;
import net.dreamlu.mica.admin.project.system.service.ISysMessageService;
import net.dreamlu.mica.core.validation.CreateGroup;
import net.dreamlu.mica.core.validation.UpdateGroup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 系统消息 前端控制器
 * </p>
 *
 * @author L.cm
 */
@Validated
@RestController
@RequestMapping("/api/system/message")
@Tag(name = "系统-消息管理")
@RequiredArgsConstructor
public class SysMessageController extends BaseController {
	private final ISysMessageService messageService;

	@ApiLog("导出消息")
	@Operation(summary = "导出消息")
	@GetMapping("download")
	@ResponseExcel(name = "系统消息")
	@PreAuthorize("@sec.hasPermission('system:message:export')")
	public List<SysMessage> download(MessageQuery query) {
		return messageService.list(messageService.getQueryWrapper(query));
	}

	@Operation(summary = "消息列表")
	@GetMapping
	@PreAuthorize("@sec.hasPermission('system:message:list')")
	public Page<SysMessage> list(Page<SysMessage> page, MessageQuery query) {
		return messageService.page(page, messageService.getQueryWrapper(query));
	}

	@Operation(summary = "消息详情")
	@GetMapping("{messageId}")
	@PreAuthorize("@sec.hasPermission('system:message:query')")
	public SysMessage getInfo(@PathVariable Long messageId) {
		return messageService.getById(messageId);
	}

	@Operation(summary = "新增消息")
	@ApiLog("新增消息")
	@PostMapping
	@PreAuthorize("@sec.hasPermission('system:message:add')")
	public void add(@Validated(CreateGroup.class) @RequestBody SysMessage entity) {
		messageService.save(entity);
		// 如果状态为启用，自动发布分发
		if (Boolean.TRUE.equals(entity.getEnabled())) {
			messageService.publish(entity.getId());
		}
	}

	@Operation(summary = "修改消息")
	@ApiLog("修改消息")
	@PutMapping
	@PreAuthorize("@sec.hasPermission('system:message:edit')")
	public void edit(@Validated(UpdateGroup.class) @RequestBody SysMessage entity) {
		messageService.updateById(entity);
	}

	@Operation(summary = "删除消息")
	@ApiLog("删除消息")
	@DeleteMapping
	@PreAuthorize("@sec.hasPermission('system:message:remove')")
	public void remove(@NotEmpty @RequestBody Set<Long> ids) {
		messageService.removeByIds(ids);
	}

	@Operation(summary = "发布消息")
	@ApiLog("发布消息")
	@PutMapping("publish/{messageId}")
	@PreAuthorize("@sec.hasPermission('system:message:edit')")
	@SuppressWarnings("unchecked")
	public void publish(@PathVariable Long messageId, @RequestBody(required = false) Map<String, Object> body) {
		List<Long> userIds = null;
		List<Long> deptIds = null;
		if (body != null) {
			if (body.get("userIds") instanceof List) {
				userIds = ((List<Number>) body.get("userIds")).stream()
					.map(Number::longValue)
					.collect(java.util.stream.Collectors.toList());
			}
			if (body.get("deptIds") instanceof List) {
				deptIds = ((List<Number>) body.get("deptIds")).stream()
					.map(Number::longValue)
					.collect(java.util.stream.Collectors.toList());
			}
		}
		messageService.publish(messageId, userIds, deptIds);
	}

}
