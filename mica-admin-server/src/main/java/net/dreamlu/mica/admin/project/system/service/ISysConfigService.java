package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.common.enums.ConfigKeyEnum;
import net.dreamlu.mica.admin.project.system.entity.SysConfig;
import net.dreamlu.mica.admin.project.system.pojo.ConfigQuery;

/**
 * <p>
 * 参数配置表 服务类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
public interface ISysConfigService extends IService<SysConfig> {

	/**
	 * 根据 query 组装查询条件
	 *
	 * @param query DeptQuery
	 * @return Wrapper
	 */
	Wrapper<SysConfig> getQueryWrapper(ConfigQuery query);

	/**
	 * 按 field 唯一获取配置项
	 *
	 * @param field 配置键名
	 * @return 配置项，无则返回 null
	 */
	SysConfig getByField(String field);

	/**
	 * 按 ConfigKeyEnum 获取配置项
	 *
	 * @param key 配置键枚举
	 * @return 配置项，无则返回 null
	 */
	default SysConfig getByField(ConfigKeyEnum key) {
		return key == null ? null : getByField(key.getField());
	}

}
