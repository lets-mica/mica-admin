package net.dreamlu.mica.admin.framework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.framework.config.MicaAdminSecurityProperties;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.framework.security.pojo.RoleInfo;
import net.dreamlu.mica.admin.framework.utils.MenuVoUtil;
import net.dreamlu.mica.admin.framework.vo.MenuVo;
import net.dreamlu.mica.admin.project.system.entity.SysMenu;
import net.dreamlu.mica.admin.project.system.service.ISysMenuService;
import net.dreamlu.mica.captcha.service.ICaptchaService;
import net.dreamlu.mica.captcha.vo.CaptchaVo;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 验证码操作处理
 *
 * @author L.cm
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "系统：认证")
public class AuthController extends BaseController {
	private final MicaAdminSecurityProperties properties;
	private final ICaptchaService captchaService;
	private final ISysMenuService menuService;

	@Operation(summary = "生成验证码")
	@GetMapping("captcha")
	public CaptchaVo captcha() {
		// 唯一标识
		String uuid = StringUtil.getUUID();
		return new CaptchaVo(uuid, captchaService.generateBase64(uuid));
	}

	@Operation(summary = "密码加密公钥")
	@GetMapping("public-key")
	public String getPublicKey() {
		return properties.getLoginKeyPair().getPublicBase64();
	}

	@Operation(summary = "获取用户信息")
	@GetMapping("info")
	@PreAuthorize("@sec.isAuthenticated()")
	public Map<String, Object> getUserInfo(AuthUser authUser) {
		Map<String, Object> data = new HashMap<>();
		// 用户信息
		data.put("userInfo", authUser.toJwtUser());
		// 响应用于加密的公钥
		data.put("publicKey", properties.getUserKeyPair().getPublicBase64());
		return data;
	}

	@GetMapping("menus")
	@Operation(summary = "用户菜单")
	@PreAuthorize("@sec.isAuthenticated()")
	public List<MenuVo> getMenus(AuthUser user) {
		// 1. 超级管理员
		if (Boolean.TRUE.equals(user.getIsAdmin())) {
			List<SysMenu> menuList = menuService.getAllMenu();
			return MenuVoUtil.transform(menuList);
		}
		// 2. 其他用户
		List<RoleInfo> roleList = user.getRoleList();
		if (roleList == null || roleList.isEmpty()) {
			return Collections.emptyList();
		}
		Set<Long> roleIds = roleList.stream()
			.map(RoleInfo::getId)
			.collect(Collectors.toSet());
		List<SysMenu> menuList = menuService.getNavByRoleIds(roleIds);
		return MenuVoUtil.transform(menuList);
	}

}
