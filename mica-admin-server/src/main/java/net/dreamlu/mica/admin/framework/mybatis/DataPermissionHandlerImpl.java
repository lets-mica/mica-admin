package net.dreamlu.mica.admin.framework.mybatis;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import net.sf.jsqlparser.expression.Expression;
import org.springframework.context.annotation.Configuration;


/**
 * 数据权限处理器
 *
 * @author L.cm
 */
@Configuration(proxyBeanMethods = false)
public class DataPermissionHandlerImpl implements DataPermissionHandler {

	@Override
	public Expression getSqlSegment(Expression where, String mappedStatementId) {
		return null;
	}

}
