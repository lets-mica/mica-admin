package net.dreamlu.mica.admin.framework.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.security.jwt.JwtTokenStore;
import net.dreamlu.mica.admin.framework.vo.TokenVo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

/**
 * 认证 token 管理
 *
 * @author L.cm
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/token")
@Tag(name = "系统：token管理")
public class AuthTokenController {
	private final JwtTokenStore tokenStore;

	@Operation(summary = "导出数据")
	@GetMapping("/download")
	@ResponseExcel(name = "认证token")
	@PreAuthorize("@sec.isAuthenticated()")
	public List<TokenVo> download(String filter) {
		return tokenStore.getAll(filter);
	}

	@Operation(summary = "查询列表")
	@GetMapping
	@PreAuthorize("@sec.isAuthenticated()")
	public Page<TokenVo> query(Page<TokenVo> page, String filter) {
		return tokenStore.page(page, filter);
	}

	@Operation(summary = "踢出用户")
	@DeleteMapping
	@PreAuthorize("@sec.isAdmin()")
	public void delete(@NotEmpty @RequestBody Set<String> keys) {
		tokenStore.remove(keys);
	}

}
