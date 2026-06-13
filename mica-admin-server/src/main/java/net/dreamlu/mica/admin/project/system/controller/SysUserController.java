package net.dreamlu.mica.admin.project.system.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.common.code.ApiCode;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.framework.config.MicaAdminSecurityProperties;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.project.system.entity.SysUser;
import net.dreamlu.mica.admin.project.system.pojo.*;
import net.dreamlu.mica.admin.project.system.service.ISysUserService;
import net.dreamlu.mica.core.result.R;
import net.dreamlu.mica.core.tuple.KeyPair;
import net.dreamlu.mica.core.utils.BeanUtil;
import net.dreamlu.mica.core.utils.ObjectUtil;
import net.dreamlu.mica.core.utils.RsaUtil;
import net.dreamlu.mica.core.utils.StringUtil;
import net.dreamlu.mica.core.validation.CreateGroup;
import net.dreamlu.mica.core.validation.UpdateGroup;
import net.dreamlu.mica.redis.cache.MicaRedisCache;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/system/users")
@Tag(name = "系统：用户管理")
public class SysUserController extends BaseController {
	private final MicaAdminSecurityProperties properties;
	private final PasswordEncoder passwordEncoder;
	private final ISysUserService userService;
	private final MicaRedisCache redisCache;

	@ApiLog("导出用户数据")
	@Operation(summary = "导出用户数据")
	@GetMapping("download")
	@ResponseExcel(name = "用户数据")
	@PreAuthorize("@sec.hasPermission('system:user:export')")
	public List<UserVo> download(UserQuery query) {
		List<SysUser> userList = userService.list(userService.getQueryWrapper(query));
		return BeanUtil.copy(userList, UserVo.class);
	}

	@ApiLog("查询用户")
	@Operation(summary = "查询用户")
	@GetMapping
	@PreAuthorize("@sec.hasPermission('system:user:list')")
	public IPage<UserVo> query(Page<SysUser> page, UserQuery query) {
		return userService.getUserPage(page, userService.getQueryWrapper(query));
	}

	@ApiLog("新增用户")
	@Operation(summary = "新增用户")
	@PostMapping
	@PreAuthorize("@sec.hasPermission('system:user:add')")
	public void create(@Validated(CreateGroup.class) @RequestBody UserForm userReq) {
		// 判断用户名
		String userName = userReq.getUserName();
		if (StringUtil.isBlank(userName)) {
			R.throwFail(ApiCode.USER_NAME_ID_BLANK);
		}
		SysUser sysUser = new SysUser();
		BeanUtil.copy(userReq, sysUser);
		// 默认密码 123456
		sysUser.setPassword(passwordEncoder.encode("123456"));
		userService.saveUserInfo(sysUser, userReq.getRoleIds(), userReq.getPostIds());
	}

	@ApiLog("修改用户")
	@Operation(summary = "修改用户")
	@PutMapping
	@PreAuthorize("@sec.hasPermission('system:user:edit')")
	public void update(@Validated(UpdateGroup.class) @RequestBody UserForm userForm) {
		// 判断用户名
		String userName = userForm.getUserName();
		if (StringUtil.isBlank(userName)) {
			R.throwFail(ApiCode.USER_NAME_ID_BLANK);
		}
		SysUser sysUser = new SysUser();
		BeanUtil.copy(userForm, sysUser);
		userService.updateUserInfo(sysUser, userForm.getRoleIds(), userForm.getPostIds());
	}

	@ApiLog("修改用户：个人中心")
	@Operation(summary = "修改用户：个人中心")
	@PutMapping(value = "center")
	public void center(@Validated @RequestBody UserProfileForm profileForm, AuthUser authUser) {
		SysUser sysUser = new SysUser();
		BeanUtil.copy(profileForm, sysUser);
		sysUser.setId(authUser.getUserId());
		userService.updateById(sysUser, authUser.getUsername());
	}

	@ApiLog("删除用户")
	@Operation(summary = "删除用户")
	@DeleteMapping
	@PreAuthorize("@sec.hasPermission('system:user:del')")
	public void delete(@NotEmpty @RequestBody Set<Long> ids) {
		userService.deleteByIds(ids);
	}

	@Operation(summary = "修改密码")
	@PostMapping("updatePass")
	public void updatePass(@RequestBody UserPwdForm pwdForm, AuthUser authUser) {
		SysUser sysUser = userService.getById(authUser.getUserId());
		KeyPair keyPair = properties.getUserKeyPair();
		String oldPass = RsaUtil.decryptFromBase64(keyPair.getPrivate(), pwdForm.getOldPass());
		String newPass = RsaUtil.decryptFromBase64(keyPair.getPrivate(), pwdForm.getNewPass());
		if (!passwordEncoder.matches(oldPass, sysUser.getPassword())) {
			R.throwOnFail(R.fail("修改失败，旧密码错误"));
		}
		if (passwordEncoder.matches(newPass, sysUser.getPassword())) {
			R.throwOnFail(R.fail("新密码不能与旧密码相同"));
		}
		SysUser entity = new SysUser();
		entity.setId(sysUser.getId());
		entity.setPassword(passwordEncoder.encode(newPass));
		userService.updateById(entity, authUser.getUsername());
	}

	@Operation(summary = "修改头像")
	@PostMapping("avatar")
	public Map<String, Object> updateAvatar(@RequestParam MultipartFile file, AuthUser authUser) {
		return userService.updateAvatar(file, authUser);
	}

	@ApiLog("修改邮箱")
	@Operation(summary = "修改邮箱")
	@PostMapping("updateEmail")
	public void updateEmail(@RequestBody EmailUpdateVo emailUpdateVo, AuthUser authUser) {
		KeyPair keyPair = properties.getUserKeyPair();
		String password = RsaUtil.decryptFromBase64(keyPair.getPrivate(), emailUpdateVo.getPassword());
		SysUser sysUser = userService.getById(authUser.getUserId());
		if (!passwordEncoder.matches(password, sysUser.getPassword())) {
			throw new BadCredentialsException("密码错误");
		}
		String email = emailUpdateVo.getEmail();
		String code = redisCache.get("email_reset_email_code_" + email);
		if (ObjectUtil.nullSafeEquals(emailUpdateVo.getCode(), code)) {
			SysUser entity = new SysUser();
			entity.setEmail(email);
			entity.setId(authUser.getUserId());
			userService.updateById(entity, authUser.getUsername());
		} else {
			throw new BadCredentialsException("code 码已失效");
		}
	}

}

