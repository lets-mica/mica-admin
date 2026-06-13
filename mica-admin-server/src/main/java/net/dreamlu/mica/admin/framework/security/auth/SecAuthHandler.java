package net.dreamlu.mica.admin.framework.security.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.framework.config.MicaAdminSecurityProperties;
import net.dreamlu.mica.admin.framework.security.jwt.JwtTokenService;
import net.dreamlu.mica.admin.framework.security.jwt.JwtTokenStore;
import net.dreamlu.mica.admin.framework.security.utils.SecurityUtil;
import net.dreamlu.mica.admin.framework.syslog.SysLogEvent;
import net.dreamlu.mica.admin.framework.syslog.SysLogType;
import net.dreamlu.mica.admin.framework.syslog.SysLogUtil;
import net.dreamlu.mica.core.constant.MicaConstant;
import net.dreamlu.mica.core.result.R;
import net.dreamlu.mica.core.result.SystemCode;
import net.dreamlu.mica.core.utils.Exceptions;
import net.dreamlu.mica.core.utils.WebUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 成功、失败的处理器
 *
 * @author L.cm
 */
@Slf4j
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class SecAuthHandler extends AccessDeniedHandlerImpl implements AuthenticationSuccessHandler, AuthenticationFailureHandler, LogoutSuccessHandler {
	private final JwtTokenService tokenService;
	private final JwtTokenStore tokenStore;
	private final MicaAdminSecurityProperties properties;
	private final ApplicationEventPublisher publisher;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) {
		if (response.isCommitted()) {
			return;
		}
		// 没有权限 403
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		WebUtil.renderJson(response, R.fail("没有权限访问"));
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) {
		log.error(e.getMessage(), e);
		// 转换异常并且抛出给统一异常工具处理
		R.throwFail(SystemCode.FAILURE, e.getMessage());
		// 记录登录日志
		SysLogEvent event = SysLogUtil.getSysLogDTO(SysLogType.Login);
		// 异常详情
		event.setExceptionDetail(Exceptions.getStackTraceAsString(e));
		event.setDescription("登录失败");
		event.setSuccessful(Boolean.FALSE);
		event.setRequestTime(getRequestTime(request));
		// 发送 spring event 事件
		publisher.publishEvent(event);
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		Map<String, Object> data = new HashMap<>(3);
		SecWebAuthenticationDetails details = (SecWebAuthenticationDetails) authentication.getDetails();
		// 用户信息
		AuthUser authUser = SecurityUtil.getUser(authentication);
		Date now = new Date();
		// 令牌有效期
		MicaAdminSecurityProperties.JwtToken jwtToken = properties.getJwtToken();
		Duration expireTime = details.isRememberMe() ? jwtToken.getRememberMeTime() : jwtToken.getExpireTime();
		String token = tokenService.createToken(authUser, now, expireTime);
		// token 管理
		tokenStore.save(request, authUser, token, expireTime);
		// 响应数据
		data.put("token", token);
		// 用户信息
		data.put("userInfo", authUser.toJwtUser());
		// 响应用于加密的公钥
		data.put("publicKey", properties.getUserKeyPair().getPublicBase64());
		WebUtil.renderJson(response, data);
		// 记录登录日志
		SysLogEvent event = SysLogUtil.getSysLogDTO(SysLogType.Login);
		event.setDescription("登录成功");
		event.setSuccessful(Boolean.TRUE);
		event.setRequestTime(getRequestTime(request));
		// 发送 spring event 事件
		publisher.publishEvent(event);
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		String token = tokenService.getToken(request);
		// 删除 token
		tokenStore.removeByToken(token);
		// 记录登出日志
		SysLogEvent event = SysLogUtil.getSysLogDTO(SysLogType.Logout);
		// 从 token 中解析出登陆用户，此时 authentication 已经清空了
		// token 有超时解析错误等问题
		try {
			event.setUserName(tokenService.getSubject(token));
		} catch (Throwable e) {
			log.error(e.getMessage());
		}
		event.setDescription("登出成功");
		event.setSuccessful(Boolean.TRUE);
		event.setRequestTime(getRequestTime(request));
		// 发送 spring event 事件
		publisher.publishEvent(event);
	}

	/**
	 * 获取请求时间
	 *
	 * @param request HttpServletRequest
	 * @return 请求时间
	 */
	private static long getRequestTime(HttpServletRequest request) {
		Long requestStartTime = (Long) request.getAttribute(MicaConstant.REQUEST_START_TIME);
		if (requestStartTime == null) {
			return 0L;
		}
		long nanos = System.nanoTime() - requestStartTime;
		return nanos / (1000 * 1000);
	}
}
