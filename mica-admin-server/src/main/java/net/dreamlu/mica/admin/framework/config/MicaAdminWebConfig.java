package net.dreamlu.mica.admin.framework.config;

import net.dreamlu.mica.admin.framework.convert.StringToListConverter;
import net.dreamlu.mica.admin.framework.mybatis.PageArgumentResolver;
import net.dreamlu.mica.admin.framework.security.auth.AuthUserArgumentResolver;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * web配置
 *
 * @author L.cm
 */
@EnableCaching
@Configuration(proxyBeanMethods = false)
public class MicaAdminWebConfig implements WebMvcConfigurer {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new PageArgumentResolver());
		argumentResolvers.add(new AuthUserArgumentResolver());
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new StringToListConverter());
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns("*")
			.allowedMethods("*")
			.allowedHeaders("*")
			.allowCredentials(true)
			.maxAge(3600L);
	}

}
