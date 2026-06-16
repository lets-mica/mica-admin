package net.dreamlu.mica.admin.project.system.pojo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 手动执行一次（带参）请求体
 *
 * @author L.cm
 */
@Data
public class SysJobRunOnceForm implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 任务Key
	 */
	@NotBlank
	private String jobKey;

	/**
	 * 补数参数。可选字段：
	 * <pre>
	 *   "bizDate": "2026-06-01",
	 *   "force":   true
	 * </pre>
	 * 任务方法通过 {@code context.getParams()} 读取。
	 */
	private Map<String, Object> params;
}
