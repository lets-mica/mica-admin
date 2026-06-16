package net.dreamlu.mica.admin.framework.job.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dreamlu.mica.admin.framework.job.annotation.SysJob;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * 任务执行上下文
 * <p>
 * 由调度器或手动触发器构造，传递给被 {@link SysJob} 标注的方法。
 * 任务方法内部可通过 {@code context.getParams()} 获取页面手动执行 / 补数场景下传入的业务参数，
 * 也可使用便捷方法 {@code getStrParam} / {@code getIntParam} / {@code getParam} 等按类型安全读取。
 * </p>
 *
 * <p>便捷方法一览：</p>
 * <ul>
 *   <li>{@link #getParam(String)} / {@link #getParam(String, Object)} — 泛型原始读取</li>
 *   <li>{@link #getStrParam(String)} / {@link #getStrParam(String, String)}</li>
 *   <li>{@link #getIntParam(String)} / {@link #getIntParam(String, Integer)}</li>
 *   <li>{@link #getBooleanParam(String)} / {@link #getBooleanParam(String, Boolean)}</li>
 *   <li>{@link #getLocalDateParam(String)} / {@link #getLocalDateParam(String, LocalDate)}</li>
 *   <li>{@link #getLocalDateTimeParam(String)} / {@link #getLocalDateTimeParam(String, LocalDateTime)}</li>
 *   <li>{@link #containsParam(String)}</li>
 * </ul>
 *
 * @author L.cm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobContext {

	/** 任务元信息 */
	private JobMeta meta;

	/** 执行参数（补数 / 页面手动执行），定时调度时为空 Map */
	private Map<String, Object> params;

	/**
	 * 创建一个空参数的任务上下文
	 *
	 * @param meta 任务元信息
	 * @return JobContext
	 */
	public static JobContext of(JobMeta meta) {
		return new JobContext(meta, Collections.emptyMap());
	}

	/**
	 * 根据元信息和参数构造任务上下文（参数允许为空）
	 *
	 * @param meta   任务元信息
	 * @param params 执行参数
	 * @return JobContext
	 */
	public static JobContext of(JobMeta meta, Map<String, Object> params) {
		return new JobContext(meta, params == null ? Collections.emptyMap() : params);
	}

	// ============================== 通用 ==============================

	/**
	 * 是否包含指定 key
	 */
	public boolean containsParam(String key) {
		return params != null && params.containsKey(key);
	}

	/**
	 * 获取指定 key 的原始参数（无类型转换；调用方需自行 cast）
	 * <p>key 不存在或 value 为 null 时返回 {@code null}。</p>
	 */
	public <T> T getParam(String key) {
		return getParam(key, null);
	}

	/**
	 * 获取指定 key 的原始参数（无类型转换），key 不存在或 value 为 null 时返回默认值
	 */
	@SuppressWarnings("unchecked")
	public <T> T getParam(String key, T defaultValue) {
		if (params == null) {
			return defaultValue;
		}
		Object value = params.get(key);
		return value == null ? defaultValue : (T) value;
	}

	// ============================== String ==============================

	/**
	 * 获取 String 参数；不存在或为 null 时返回 null
	 */
	public String getStrParam(String key) {
		return getStrParam(key, null);
	}

	/**
	 * 获取 String 参数；不存在或为 null 时返回默认值
	 */
	public String getStrParam(String key, String defaultValue) {
		Object value = params == null ? null : params.get(key);
		return value == null ? defaultValue : value.toString();
	}

	// ============================== Integer ==============================

	/**
	 * 获取 Integer 参数；不存在或为 null 时返回 null。
	 * <p>支持 {@link Number} 子类型以及数字字符串的自动转换。</p>
	 */
	public Integer getIntParam(String key) {
		return getIntParam(key, null);
	}

	/**
	 * 获取 Integer 参数；不存在或为 null 时返回默认值
	 */
	public Integer getIntParam(String key, Integer defaultValue) {
		Object value = params == null ? null : params.get(key);
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return Integer.parseInt(value.toString().trim());
	}

	// ============================== Boolean ==============================

	/**
	 * 获取 Boolean 参数；不存在或为 null 时返回 null。
	 * <p>支持 {@link Boolean} 原始值以及字符串 {@code "true"/"false"} 的解析。</p>
	 */
	public Boolean getBooleanParam(String key) {
		return getBooleanParam(key, null);
	}

	/**
	 * 获取 Boolean 参数；不存在或为 null 时返回默认值
	 */
	public Boolean getBooleanParam(String key, Boolean defaultValue) {
		Object value = params == null ? null : params.get(key);
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return Boolean.parseBoolean(value.toString().trim());
	}

	// ============================== LocalDate ==============================

	/**
	 * 获取 LocalDate 参数；不存在或为 null 时返回 null。
	 * <p>支持 {@link LocalDate} 原始值以及 ISO-8601 字符串（{@code yyyy-MM-dd}）的解析。</p>
	 */
	public LocalDate getLocalDateParam(String key) {
		return getLocalDateParam(key, null);
	}

	/**
	 * 获取 LocalDate 参数；不存在或为 null 时返回默认值
	 */
	public LocalDate getLocalDateParam(String key, LocalDate defaultValue) {
		Object value = params == null ? null : params.get(key);
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof LocalDate) {
			return (LocalDate) value;
		}
		return LocalDate.parse(value.toString().trim());
	}

	// ============================== LocalDateTime ==============================

	/**
	 * 获取 LocalDateTime 参数；不存在或为 null 时返回 null。
	 * <p>支持 {@link LocalDateTime} 原始值以及 ISO-8601 字符串（{@code yyyy-MM-ddTHH:mm:ss}）的解析。</p>
	 */
	public LocalDateTime getLocalDateTimeParam(String key) {
		return getLocalDateTimeParam(key, null);
	}

	/**
	 * 获取 LocalDateTime 参数；不存在或为 null 时返回默认值
	 */
	public LocalDateTime getLocalDateTimeParam(String key, LocalDateTime defaultValue) {
		Object value = params == null ? null : params.get(key);
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof LocalDateTime) {
			return (LocalDateTime) value;
		}
		return LocalDateTime.parse(value.toString().trim());
	}
}
