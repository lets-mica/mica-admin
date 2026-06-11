package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.core.utils.StringUtil;
import net.dreamlu.mica.admin.project.system.mapper.SysConfigMapper;
import net.dreamlu.mica.admin.project.system.entity.SysConfig;
import net.dreamlu.mica.admin.project.system.pojo.ConfigQuery;
import net.dreamlu.mica.admin.project.system.service.ISysConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 参数配置表 服务实现类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

	@Override
	public Wrapper<SysConfig> getQueryWrapper(ConfigQuery query) {
		LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
		wrapper.like(StringUtil.isNotBlank(query.getName()), SysConfig::getName, query.getName());
		wrapper.eq(StringUtil.isNotBlank(query.getField()), SysConfig::getField, query.getField());
		wrapper.eq(query.getIsSystem() != null, SysConfig::getIsSystem, query.getIsSystem());
		List<LocalDateTime> createTime = query.getCreateTime();
		if (createTime != null && createTime.size() > 1) {
			wrapper.between(SysConfig::getCreatedAt, createTime.get(0), createTime.get(1));
		}
		return wrapper;
	}

}
