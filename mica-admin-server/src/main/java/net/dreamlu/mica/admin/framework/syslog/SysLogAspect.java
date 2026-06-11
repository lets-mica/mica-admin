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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.utils.ClassUtil;
import net.dreamlu.mica.core.utils.Exceptions;
import net.dreamlu.mica.core.utils.JsonUtil;
import net.dreamlu.mica.core.utils.StringPool;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 操作日志使用spring event异步入库
 *
 * @author L.cm
 */
@Slf4j
@Order
@Aspect
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class SysLogAspect {
	private final ApplicationEventPublisher publisher;

	/**
	 * 环绕融资
	 *
	 * @param point  ProceedingJoinPoint
	 * @param apiLog ApiLog
	 * @return Object
	 * @throws Throwable
	 */
	@Around("@annotation(apiLog)")
	public Object logAround(ProceedingJoinPoint point, ApiLog apiLog) throws Throwable {
		// 类和方法信息
		String strClassName = point.getTarget().getClass().getName();
		MethodSignature ms = (MethodSignature) point.getSignature();
		String strMethodName = ms.getName();
		log.info("[class]:{},[method]:{}", strClassName, strMethodName);
		SysLogEvent event = SysLogUtil.getSysLogDTO(SysLogType.Api);
		event.setDescription(apiLog.value());
		event.setClassMethod(strClassName + StringPool.HASH + strMethodName);
		event.setData(getPostJson(point, ms));
		// 执行时间
		long startNs = System.nanoTime();
		try {
			Object result = point.proceed();
			// 耗时
			event.setRequestTime(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs));
			event.setSuccessful(Boolean.TRUE);
			// 发送异步日志事件
			publisher.publishEvent(event);
			return result;
		} catch (Throwable e) {
			// 耗时
			event.setRequestTime(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs));
			// 异常详情
			event.setExceptionDetail(Exceptions.getStackTraceAsString(e));
			event.setSuccessful(Boolean.FALSE);
			// 发送异步日志事件
			publisher.publishEvent(event);
			throw e;
		}
	}

	private static String getPostJson(ProceedingJoinPoint point, MethodSignature ms) {
		Object[] args = point.getArgs();
		Method method = ms.getMethod();
		// 一次请求只能有一个 request body
		Object requestBodyValue = null;
		for (int i = 0; i < args.length; i++) {
			// 读取方法参数
			MethodParameter methodParam = ClassUtil.getMethodParameter(method, i);
			RequestBody requestBody = methodParam.getParameterAnnotation(RequestBody.class);
			// 如果是body的json则是对象
			if (requestBody != null) {
				requestBodyValue = args[i];
				break;
			}
		}
		return requestBodyValue == null ? null : JsonUtil.toJson(requestBodyValue);
	}
}
