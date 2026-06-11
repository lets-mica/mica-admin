package net.dreamlu.mica.admin.project.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.core.utils.RandomType;
import net.dreamlu.mica.core.utils.StringUtil;
import net.dreamlu.mica.admin.project.system.pojo.EmailCodeVo;
import net.dreamlu.mica.admin.project.system.service.IMailService;
import net.dreamlu.mica.redis.cache.MicaRedisCache;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统：验证码管理
 *
 * @author L.cm
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/system/code")
@Tag(name = "系统：验证码管理")
public class SysVerifyController {
	private final MicaRedisCache redisCache;
	private final IMailService mailService;

	@PostMapping(value = "resetEmail")
	@Operation(summary = "重置邮箱，发送验证码")
	@PreAuthorize("@sec.isAuthenticated()")
	public void resetEmail(@Validated @RequestBody EmailCodeVo emailVo) {
		String email = emailVo.getEmail();
		String code = redisCache.get("email_reset_email_code_" + email, () -> StringUtil.random(6, RandomType.INT));
		mailService.send(email, code);
	}

}
