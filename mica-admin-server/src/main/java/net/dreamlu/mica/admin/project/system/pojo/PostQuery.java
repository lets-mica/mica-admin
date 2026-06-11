package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位查询
 *
 * @author L.cm
 */
@Data
public class PostQuery {

	/**
	 * like
	 */
	private String name;
	private Integer enabled;
	private List<LocalDateTime> createTime;

}
