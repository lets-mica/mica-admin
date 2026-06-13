package net.dreamlu.mica.admin.project.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.core.utils.StringUtil;
import net.dreamlu.mica.admin.project.system.mapper.SysConfigMapper;
import net.dreamlu.mica.admin.project.system.entity.SysConfig;
import net.dreamlu.mica.admin.project.system.pojo.ConfigQuery;
import net.dreamlu.mica.admin.project.system.service.ISysConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	private static final String PREFERENCE_FIELD = "preference.default";

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

	@Override
	public String getPreferenceJson() {
		SysConfig cfg = this.getOne(buildPreferenceWrapper(), false);
		if (cfg == null) {
			return "{}";
		}
		String value = cfg.getValue();
		return StrUtil.isBlank(value) ? "{}" : value;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void savePreferenceJson(String json) {
		if (StrUtil.isBlank(json)) {
			json = "{}";
		}
		SysConfig cfg = this.getOne(buildPreferenceWrapper(), false);
		if (cfg == null) {
			cfg = new SysConfig();
			cfg.setField(PREFERENCE_FIELD);
			cfg.setName("偏好-系统默认");
			cfg.setIsSystem(false);
			cfg.setRemark("全局默认偏好，整 JSON 存储");
			cfg.setValue(json);
			this.save(cfg);
		} else {
			cfg.setValue(json);
			this.updateById(cfg);
		}
	}

	private LambdaQueryWrapper<SysConfig> buildPreferenceWrapper() {
		return new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getField, PREFERENCE_FIELD);
	}

}