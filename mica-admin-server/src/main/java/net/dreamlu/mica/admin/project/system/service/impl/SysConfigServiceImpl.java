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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	private static final String PREFERENCE_FIELD_PREFIX = "preference.";

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
	public Map<String, String> listPreferenceDefaults() {
		LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
		wrapper.likeRight(SysConfig::getField, PREFERENCE_FIELD_PREFIX);
		List<SysConfig> list = this.list(wrapper);
		if (list == null || list.isEmpty()) {
			return Collections.emptyMap();
		}
		return list.stream()
			.filter(c -> StrUtil.isNotBlank(c.getField()))
			.collect(Collectors.toMap(SysConfig::getField, c -> StrUtil.nullToEmpty(c.getValue()), (a, b) -> a));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void savePreferenceBatch(Map<String, String> kv) {
		if (kv == null || kv.isEmpty()) {
			return;
		}

		// 仅处理合法字段：必须以 preference. 开头
		List<Map.Entry<String, String>> entries = kv.entrySet().stream()
			.filter(e -> StrUtil.isNotBlank(e.getKey()) && e.getKey().startsWith(PREFERENCE_FIELD_PREFIX))
			.collect(Collectors.toList());
		if (entries.isEmpty()) {
			return;
		}

		List<String> fields = entries.stream().map(Map.Entry::getKey).collect(Collectors.toList());
		LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
		wrapper.in(SysConfig::getField, fields);
		List<SysConfig> existing = this.list(wrapper);
		Map<String, SysConfig> existingMap = existing.stream()
			.collect(Collectors.toMap(SysConfig::getField, c -> c, (a, b) -> a));

		List<SysConfig> toSave = new ArrayList<>();
		List<SysConfig> toUpdate = new ArrayList<>();
		for (Map.Entry<String, String> e : entries) {
			String field = e.getKey();
			String value = StrUtil.nullToEmpty(e.getValue());
			SysConfig hit = existingMap.get(field);
			if (hit == null) {
				SysConfig cfg = new SysConfig();
				cfg.setField(field);
				cfg.setValue(value);
				cfg.setIsSystem(false);
				cfg.setName(field);
				toSave.add(cfg);
			} else {
				hit.setValue(value);
				toUpdate.add(hit);
			}
		}
		if (!toSave.isEmpty()) {
			this.saveBatch(toSave);
		}
		if (!toUpdate.isEmpty()) {
			this.updateBatchById(toUpdate);
		}
	}

}
