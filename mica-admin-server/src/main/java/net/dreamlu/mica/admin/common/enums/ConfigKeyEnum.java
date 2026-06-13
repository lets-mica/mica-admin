package net.dreamlu.mica.admin.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配置 key 枚举
 *
 * @author L.cm
 */
@Getter
@AllArgsConstructor
public enum ConfigKeyEnum {

	/**
	 * 系统默认偏好设置（整存 JSON）
	 */
	PREFERENCE_DEFAULT("preference.default", "系统默认偏好");

	private final String field;
	private final String desc;
}