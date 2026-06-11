package net.dreamlu.mica.admin.framework.mybatis;

import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import net.dreamlu.mica.core.exception.ServiceException;
import net.dreamlu.mica.admin.common.code.ApiCode;

/**
 * 演示环境数据禁止删除
 *
 * @author L.cm
 */
public class DemoBlockAttackInnerInterceptor extends BlockAttackInnerInterceptor {

	@Override
	public String parserMulti(String sql, Object obj) {
		throw new ServiceException(ApiCode.DEMO_BLOCK);
	}

}
