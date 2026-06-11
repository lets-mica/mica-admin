/*
 * Copyright (c) 2019-2029, Dreamlu 卢春梦 (596392912@qq.com & www.dreamlu.net).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dreamlu.mica.admin.framework.syslog;

import net.dreamlu.mica.core.utils.*;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.framework.security.utils.SecurityUtil;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 系统日志工具类
 *
 * @author L.cm
 */
public class SysLogUtil {

	/**
	 * 构造 SysLogEvent
	 *
	 * @param logType SysLogType
	 * @return SysLogEvent
	 */
	public static SysLogEvent getSysLogDTO(SysLogType logType) {
		SysLogEvent event = new SysLogEvent();
		event.setLogType(logType.name());
		HttpServletRequest request = WebUtil.getRequest();
		String method = request.getMethod();
		// 请求信息 GET /api/test/xx
		String requestInfo = method + StringPool.SPACE + request.getRequestURI();
		// paraMap
		Map<String, String[]> paraMap = request.getParameterMap();
		if (ObjectUtil.isEmpty(paraMap)) {
			event.setParams(requestInfo);
		} else {
			StringBuilder builder = new StringBuilder(requestInfo).append(CharPool.QUESTION_MARK);
			paraMap.forEach((key, values) -> {
				builder.append(key).append(CharPool.EQUALS);
				if ("password".equalsIgnoreCase(key)) {
					builder.append("******");
				} else {
					builder.append(StringUtil.join(values));
				}
				builder.append(CharPool.AMPERSAND);
			});
			builder.deleteCharAt(builder.length() - 1);
			event.setParams(builder.toString());
		}
		// 获取请求 ip 和 ua
		event.setRequestIp(WebUtil.getIP());
		event.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
		// 用户信息
		AuthUser authUser = SecurityUtil.getUser();
		if (authUser != null) {
			event.setUserId(authUser.getUserId());
			event.setUserName(authUser.getUsername());
		}
		return event;
	}

}
