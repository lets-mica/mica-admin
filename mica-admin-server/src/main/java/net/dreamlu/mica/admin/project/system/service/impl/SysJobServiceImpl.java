package net.dreamlu.mica.admin.project.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.admin.project.system.entity.SysJob;
import net.dreamlu.mica.admin.project.system.mapper.SysJobMapper;
import net.dreamlu.mica.admin.project.system.pojo.SysJobQuery;
import net.dreamlu.mica.admin.project.system.service.ISysJobService;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 数据库驱动定时任务 服务实现类
 * </p>
 *
 * @author L.cm
 */
@Service
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements ISysJobService {

	@Override
	public Wrapper<SysJob> getQueryWrapper(SysJobQuery query) {
		LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<>();
		// 模糊查询 jobKey,jobName,description
		String blurry = query.getBlurry();
		wrapper.and(StringUtil.isNotBlank(blurry), w -> w
			.like(SysJob::getJobKey, blurry)
			.or().like(SysJob::getJobName, blurry)
			.or().like(SysJob::getDescription, blurry));
		wrapper.eq(query.getEnabled() != null, SysJob::getEnabled, query.getEnabled());
		List<LocalDateTime> createTime = query.getCreateTime();
		if (createTime != null && createTime.size() > 1) {
			wrapper.between(SysJob::getCreatedAt, createTime.get(0), createTime.get(1));
		}
		wrapper.orderByDesc(SysJob::getId);
		return wrapper;
	}

	@Override
	public SysJob getByJobKey(String jobKey) {
		if (StrUtil.isBlank(jobKey)) {
			return null;
		}
		LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(SysJob::getJobKey, jobKey);
		return super.getOne(wrapper, false);
	}
}
