package net.dreamlu.mica.admin.framework.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * @author L.cm
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiLog {

	/**
	 * 描述
	 *
	 * @return {String}
	 */
	String value();
}
