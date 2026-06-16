package net.dreamlu.mica.admin.framework.job.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务参数类型枚举
 * <p>
 * 用于解析 sys_job.param_schema JSON 配置，定义页面手动执行 / 补数时可传入的参数类型。
 * </p>
 *
 * @author L.cm
 */
@Getter
@AllArgsConstructor
public enum JobParamType {

	/** 字符串 */
	STRING("String"),
	/** 整数 */
	INTEGER("Integer"),
	/** 长整数 */
	LONG("Long"),
	/** 布尔 */
	BOOLEAN("Boolean"),
	/** 浮点 */
	DOUBLE("Double"),
	/** 日期 yyyy-MM-dd */
	DATE("Date"),
	/** 日期时间 yyyy-MM-dd HH:mm:ss */
	DATETIME("DateTime");

	private final String type;

	/**
	 * 将原始值转换为对应的 Java 类型
	 *
	 * @param rawValue 原始字符串值
	 * @return 转换后的对象
	 */
	public Object convert(String rawValue) {
		if (rawValue == null) {
			return null;
		}
		switch (this) {
			case STRING:
				return rawValue;
			case INTEGER:
				return Integer.parseInt(rawValue.trim());
			case LONG:
				return Long.parseLong(rawValue.trim());
			case BOOLEAN:
				return Boolean.parseBoolean(rawValue.trim());
			case DOUBLE:
				return Double.parseDouble(rawValue.trim());
			case DATE:
			case DATETIME:
				// 仅返回原始字符串，调用方按需自行解析为 java.time 类型
				return rawValue;
			default:
				return rawValue;
		}
	}

	/**
	 * 根据 type 字符串解析为枚举（大小写不敏感），找不到时返回 STRING
	 *
	 * @param type 类型字符串
	 * @return JobParamType
	 */
	public static JobParamType of(String type) {
		if (type == null) {
			return STRING;
		}
		for (JobParamType value : values()) {
			if (value.name().equalsIgnoreCase(type) || value.type.equalsIgnoreCase(type)) {
				return value;
			}
		}
		return STRING;
	}
}
