package net.dreamlu.mica.admin.framework.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import net.dreamlu.mica.admin.framework.mybatis.DemoBlockAttackInnerInterceptor;
import net.dreamlu.mica.admin.framework.mybatis.MybatisPlusMetaObjectHandler;
import net.dreamlu.mica.admin.framework.mybatis.SqlLogFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * mica admin mybatis plus 配置
 *
 * @author L.cm
 */
@Configuration(proxyBeanMethods = false)
public class MybatisPlusConfig {

	/**
	 * mybatis-plus 乐观锁拦截器
	 */
	@Bean
	public OptimisticLockerInnerInterceptor optimisticLockerInterceptor() {
		return new OptimisticLockerInnerInterceptor();
	}

	/**
	 * mybatis-plus分页插件
	 */
	@Bean
	public PaginationInnerInterceptor paginationInterceptor() {
		return new PaginationInnerInterceptor();
	}

	/**
	 * mybatis plus 插件
	 */
	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor(ObjectProvider<InnerInterceptor> interceptorObjectProvider) {
		final MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		interceptorObjectProvider.orderedStream().forEach(interceptor::addInnerInterceptor);
		return interceptor;
	}

	/**
	 * 自动填充
	 */
	@Bean
	public MetaObjectHandler metaObjectHandler() {
		return new MybatisPlusMetaObjectHandler();
	}

	/**
	 * sql 可执行日志
	 */
	@Bean
	public SqlLogFilter sqlLogFilter() {
		return new SqlLogFilter();
	}

	/**
	 * 数据权限处理器
	 */
	@Bean
	public DataPermissionInterceptor dataPermissionInterceptor(DataPermissionHandler dataPermissionHandler) {
		return new DataPermissionInterceptor(dataPermissionHandler);
	}

	/**
	 * 演示环境禁止更新和删除
	 */
	@Bean
	@Profile("prod")
	public DemoBlockAttackInnerInterceptor blockAttackInnerInterceptor() {
		return new DemoBlockAttackInnerInterceptor();
	}

}
