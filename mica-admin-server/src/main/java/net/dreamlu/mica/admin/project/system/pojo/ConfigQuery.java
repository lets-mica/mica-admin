package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 配置查询
 *
 * @author L.cm
 */
@Data
public class ConfigQuery {

	private String name;
	private String field;
	private Boolean isSystem;
	private List<LocalDateTime> createTime;

}
