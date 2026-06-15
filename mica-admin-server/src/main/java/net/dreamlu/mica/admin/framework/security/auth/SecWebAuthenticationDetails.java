package net.dreamlu.mica.admin.framework.security.auth;

import lombok.Getter;
import net.dreamlu.mica.core.utils.StringPool;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 添加 验证码字段
 *
 * @author L.cm
 */
@Getter
public class SecWebAuthenticationDetails extends WebAuthenticationDetails {
	private static final long serialVersionUID = -5705520861298051410L;
	private final String validateCodeId;
	private final String validateCode;
	private final boolean rememberMe;

	SecWebAuthenticationDetails(HttpServletRequest request) {
		super(request);
		this.validateCodeId = request.getParameter("validateCodeId");
		this.validateCode = request.getParameter("validateCode");
		this.rememberMe = getRememberMe(request.getParameter("rememberMe"));
	}

	private static boolean getRememberMe(String value) {
		if (StringUtil.isBlank(value)) {
			return false;
		}
		if (StringPool.ONE.equals(value)) {
			return true;
		}
		return StringPool.TRUE.equalsIgnoreCase(value);
	}
}
