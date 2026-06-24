package net.dreamlu.mica.admin.project.im.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.project.system.entity.SysUser;
import net.dreamlu.mica.admin.project.system.service.ISysUserService;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * IM 模块的用户查询接口（轻量级，用于发起单聊、邀请入群等选择用户的场景）。
 * <p>
 * 与系统用户管理接口区别：
 * <ul>
 *     <li>无需 {@code system:user:list} 权限，仅校验登录</li>
 *     <li>不返回敏感字段（手机号、邮箱、密码等），仅返回 id/用户名/昵称/头像</li>
 *     <li>不返回已禁用的账号</li>
 *     <li>结果按 id 升序，最多 50 条</li>
 * </ul>
 *
 * @author L.cm
 */
@Tag(name = "IM：用户选择")
@RestController
@RequestMapping("/api/im/users")
@RequiredArgsConstructor
public class ImUserController extends BaseController {

	private final ISysUserService userService;

	/**
	 * 按用户名/昵称模糊搜索。
	 *
	 * @param keyword 关键字（可空；空时返回前 20 条活跃用户）
	 * @param limit   最大返回条数，默认 20，最大 50
	 */
	@Operation(summary = "搜索可聊天的用户")
	@GetMapping("/search")
	public List<ImUserVO> search(@RequestParam(required = false, defaultValue = "") String keyword,
								 @RequestParam(required = false, defaultValue = "20") Integer limit,
								 AuthUser authUser) {
		int max = Math.min(Math.max(limit == null ? 20 : limit, 1), 50);
		LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
		wrapper.select(SysUser::getId, SysUser::getUserName, SysUser::getNickName, SysUser::getAvatar);
		// 仅过滤明确禁用（enabled = false）的账号；enabled 为 null 视为启用（兼容老数据）
		wrapper.notInSql(SysUser::getId, "SELECT id FROM sys_user WHERE enabled = 0");
		wrapper.ne(SysUser::getId, authUser.getUserId());
		if (StringUtil.isNotBlank(keyword)) {
			wrapper.and(w -> w.like(SysUser::getUserName, keyword.trim())
				.or().like(SysUser::getNickName, keyword.trim()));
		}
		wrapper.orderByAsc(SysUser::getId);
		wrapper.last("LIMIT " + max);
		List<SysUser> users = userService.list(wrapper);
		if (users.isEmpty()) {
			return Collections.emptyList();
		}
		List<ImUserVO> result = new ArrayList<>(users.size());
		for (SysUser u : users) {
			ImUserVO vo = new ImUserVO();
			vo.setId(u.getId());
			vo.setUserName(u.getUserName());
			vo.setNickName(u.getNickName() != null ? u.getNickName() : u.getUserName());
			vo.setAvatar(u.getAvatar());
			result.add(vo);
		}
		return result;
	}

	/**
	 * 根据 id 集合批量查询用户简要信息（用于渲染会话列表里的群成员）。
	 */
	@Operation(summary = "批量查询用户简要信息")
	@GetMapping("/batch")
	public List<ImUserVO> batch(@RequestParam("ids") List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
		LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
		wrapper.select(SysUser::getId, SysUser::getUserName, SysUser::getNickName, SysUser::getAvatar);
		wrapper.in(SysUser::getId, ids);
		wrapper.orderByAsc(SysUser::getId);
		List<SysUser> users = userService.list(wrapper);
		List<ImUserVO> result = new ArrayList<>(users.size());
		for (SysUser u : users) {
			ImUserVO vo = new ImUserVO();
			vo.setId(u.getId());
			vo.setUserName(u.getUserName());
			vo.setNickName(u.getNickName() != null ? u.getNickName() : u.getUserName());
			vo.setAvatar(u.getAvatar());
			result.add(vo);
		}
		return result;
	}

	/**
	 * IM 模块的用户简要信息 VO。
	 */
	@Data
	public static class ImUserVO {
		private Long id;
		private String userName;
		private String nickName;
		private String avatar;
	}
}
