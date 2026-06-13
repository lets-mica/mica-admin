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

	/**
	 * 获取系统默认偏好（整存 JSON 字符串）
	 *
	 * @return 偏好 JSON 字符串，无配置时返回空对象 "{}"
	 */
	String getPreferenceJson();

	/**
	 * 保存系统默认偏好（整存 JSON 字符串，需管理员权限，由 Controller 控制）
	 *
	 * @param json 偏好 JSON 字符串
	 */
	void savePreferenceJson(String json);

}
