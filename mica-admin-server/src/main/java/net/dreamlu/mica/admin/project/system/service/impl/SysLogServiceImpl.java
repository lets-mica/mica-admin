package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.admin.project.system.entity.SysLog;
import net.dreamlu.mica.admin.project.system.mapper.SysLogMapper;
import net.dreamlu.mica.admin.project.system.pojo.LogQuery;
import net.dreamlu.mica.admin.project.system.service.ISysLogService;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 系统日志 服务实现类
 * </p>
 *
 * @author L.cm
 * @since 2020-07-09
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements ISysLogService {

	@Override
	public Wrapper<SysLog> getQueryWrapper(LogQuery query) {
		LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(SysLog::getSuccessful, query.getSuccessful());
		String blurry = query.getBlurry();
		// 模糊查询字段 username,description,address,requestIp,method,params
		wrapper.and(StringUtil.isNotBlank(blurry), w -> w
			.like(SysLog::getUserName, blurry)
			.or().like(SysLog::getUserName, blurry)
			.or().like(SysLog::getDescription, blurry)
			.or().like(SysLog::getAddress, blurry)
			.or().like(SysLog::getRequestIp, blurry)
			.or().like(SysLog::getClassMethod, blurry)
			.or().like(SysLog::getParams, blurry));
		List<LocalDateTime> createTime = query.getCreateTime();
		if (createTime != null && createTime.size() > 1) {
			wrapper.between(SysLog::getCreatedAt, createTime.get(0), createTime.get(1));
		}
		wrapper.orderByDesc(SysLog::getCreatedAt);
		return wrapper;
	}

	@Override
	public void removeAllByInfo() {
		LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(SysLog::getSuccessful, Boolean.TRUE);
		super.remove(wrapper);
	}

	@Override
	public void removeAllByError() {
		LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(SysLog::getSuccessful, Boolean.FALSE);
		super.remove(wrapper);
	}
}
