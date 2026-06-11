package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门查询
 *
 * @author L.cm
 */
@Data
public class DeptQuery {

	private String name;
	private Integer enabled;
	private Long pid;
	private List<LocalDateTime> createTime;

}
