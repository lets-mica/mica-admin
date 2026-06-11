package net.dreamlu.mica.admin.framework.config;

import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.exception.ServiceException;
import net.dreamlu.mica.core.result.R;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 自定义异常处理
 *
 * @author L.cm
 */
@Slf4j
@Order(1)
@RestControllerAdvice
@Configuration(proxyBeanMethods = false)
public class MicaAdminExceptionTranslator {

	@ExceptionHandler(MyBatisSystemException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public R<Object> handleError(MyBatisSystemException e) {
		log.error("业务异常", e);
		Throwable cause = e.getCause().getCause();
		if (cause instanceof ServiceException) {
			return ((ServiceException) cause).getResult();
		}
		return R.fail(cause.getMessage());
	}

}
