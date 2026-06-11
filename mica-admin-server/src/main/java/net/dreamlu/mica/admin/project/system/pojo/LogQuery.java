package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日志查询类
 *
 * @author L.cm
 */
@Data
public class LogQuery {

	/**
	 * 模糊查询：username,description,address,requestIp,method,params
	 */
	private String blurry;
	/**
	 * 是否成功[0失败,1成功]
	 */
	private Boolean successful;
	private List<LocalDateTime> createTime;

}
