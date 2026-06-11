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

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.core.utils.BeanUtil;
import net.dreamlu.mica.core.utils.StringUtil;
import net.dreamlu.mica.admin.project.system.entity.SysLog;
import net.dreamlu.mica.admin.project.system.service.ISysLogService;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;

/**
 * 监听系统日志事件，系统日志入库
 *
 * @author L.cm
 */
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class SysLogListener {
	private final ISysLogService sysLogService;
	private final Ip2regionSearcher searcher;

	@Async
	@Order
	@EventListener(SysLogEvent.class)
	public void saveSysLog(SysLogEvent event) {
		SysLog sysLog = new SysLog();
		BeanUtil.copy(event, sysLog);
		// 获取操作系统和浏览器信息
		UserAgent userAgent = UserAgent.parseUserAgentString(event.getUserAgent());
		OperatingSystem system = userAgent.getOperatingSystem();
		sysLog.setOs(system.getName());
		Browser browser = userAgent.getBrowser();
		sysLog.setBrowser(browser.getName());
		String requestIp = event.getRequestIp();
		// ip 不为空，查找 ip 的地理信息
		if (StringUtil.isNotBlank(requestIp)) {
			sysLog.setAddress(searcher.getAddress(requestIp));
		}
		sysLogService.save(sysLog);
	}
}
